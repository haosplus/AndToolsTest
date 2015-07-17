package com.auto.tools.instrumentation;


import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlSerializer;
import android.os.Bundle;
import android.os.Environment;
import android.test.AndroidTestRunner;
import android.test.InstrumentationTestRunner;
import android.util.Log;
import android.util.Xml;

import com.auto.tools.utils.FileHelper;

public class CustomInstrumentationTestRunner extends InstrumentationTestRunner {

	private static final String TESTSUITES = "testsuites";
	private static final String TESTSUITE = "testsuite";
	private static final String ERRORS = "errors";
	private static final String FAILURES = "failures";
	private static final String ERROR = "error";
	private static final String FAILURE = "failure";
	private static final String NAME = "name";
	private static final String PACKAGE = "package";
	private static final String TESTS = "tests";
	private static final String TESTCASE = "testcase";
	private static final String CLASSNAME = "classname";
	private static final String TIME = "time";
	private static final String TIMESTAMP = "timestamp";
	private static final String PROPERTIES = "properties";
	private static final String SYSTEM_OUT = "system-out";
	private static final String SYSTEM_ERR = "system-err";

	private static final String SPLIT_LEVEL_NONE = "none";
	private static final String SPLIT_LEVEL_CLASS = "class";
	private static final String SPLIT_LEVEL_PACKAGE = "package";

	private static final String TAG = "debug";
	private static final String DEFAULT_JUNIT_FILE_POSTFIX = "-TEST.xml";
	private static final String DEFAULT_NO_PACKAGE_PREFIX = "NO_PACKAGE";
	private static final String DEFAULT_SINGLE_FILE_NAME = "ALL-TEST.xml";
	private static final String DEFAULT_SPLIT_LEVEL = SPLIT_LEVEL_PACKAGE;
	private String junitOutputDirectory = null;
	private String junitOutputFilePostfix = null;
	private String junitNoPackagePrefix;
	private String junitSplitLevel;
	private String junitSingleFileName;

	private boolean junitOutputEnabled;
	private boolean justCount;
	private XmlSerializer currentXmlSerializer;
	private final LinkedHashMap<Package, TestCaseInfo> caseMap = new LinkedHashMap<Package, TestCaseInfo>();
	private boolean outputEnabled;
	private AndroidTestRunner runner;
	private boolean logOnly;
	private PrintWriter currentFileWriter;
	private final static String TESTDATA = "testdata";
	private Test runTest;
	/**
	 * Stores information about single test run.
	 * 
	 */
	public static class TestInfo {
		public Package thePackage;
		public Class<? extends TestCase> testCase;
		public String name;
		public Throwable error;
		public AssertionFailedError failure;
		public long time;

		@Override
		public String toString() {
			return name + "[" + testCase.getClass() + "] <" + thePackage
					+ ">. Time: " + time + " ms. E<" + error + ">, F <"
					+ failure + ">";
		}
	}

	/**
	 * Stores information about particular test case class - containing all
	 * tests for that class.
	 * 
	 */
	public static class TestCaseInfo {
		public Package thePackage;
		public Class<? extends TestCase> testCaseClass;
		public Map<String, TestInfo> testMap = new LinkedHashMap<String, TestInfo>();
	}

	/**
	 * Stores information about the whole package containing multiple test
	 * cases.
	 * 
	 */
	public static class TestPackageInfo {
		public Package thePackage;
		public Map<Class<? extends TestCase>, TestCaseInfo> testCaseList = new LinkedHashMap<Class<? extends TestCase>, TestCaseInfo>();
	}

	/**
	 * Listener for executing test cases. It has the following purposes:
	 * measures time of execution for each test, stores errors and failures that
	 * occur during test as well as it optimizes garbage collection of the test
	 * - after test is finished it cleans up all the static variables of the
	 * test case. The last one is pretty useful if many tests are executed.
	 * 
	 */
	private class JunitTestListener implements TestListener {
		

		/**
		 * The minimum time we expect a test to take.
		 */
		private static final int MINIMUM_TIME = 100;
		/**
		 * Just in case it ever happens that the tests are run in parallell
		 * (maybe future junit version?) we make sure that measured time is
		 * separate per each thread running the tests.
		 */
		private final ThreadLocal<Long> startTime = new ThreadLocal<Long>();

		public void startTest(final Test test) {
			Log.d(TAG, "Starting test: " + test);
			runTest = test;
			if (test instanceof TestCase) {
				Thread.currentThread().setContextClassLoader(
						test.getClass().getClassLoader());
				startTime.set(System.currentTimeMillis());
			}
		}

		public void endTest(final Test t) {
			if (t instanceof TestCase) {
				final TestCase testCase = (TestCase) t;
				cleanup(testCase);
				/*
				 * Note! This is copied from InstrumentationCoreTestRunner in
				 * android code
				 * 
				 * Make sure all tests take at least MINIMUM_TIME to complete.
				 * If they don't, we wait a bit. The Cupcake Binder can't handle
				 * too many operations in a very short time, which causes
				 * headache for the CTS.
				 */
				final long timeTaken = System.currentTimeMillis()
						- startTime.get();
				getTestInfo(testCase).time = timeTaken;
				if (timeTaken < MINIMUM_TIME) {
					try {
						Thread.sleep(MINIMUM_TIME - timeTaken);
					} catch (final InterruptedException ignored) {
						// We don't care.
					}
				}
			}
			Log.d(TAG, "Finished test: " + t);
		}

		public void addError(final Test test, final Throwable t) {
			if (test instanceof TestCase) {
				getTestInfo((TestCase) test).error = t;
			}
		}

		public void addFailure(final Test test, final AssertionFailedError f) {
			if (test instanceof TestCase) {
				getTestInfo((TestCase) test).failure = f;
			}
		}

		/**
		 * Nulls all non-static reference fields in the given test class. This
		 * method helps us with those test classes that don't have an explicit
		 * tearDown() method. Normally the garbage collector should take care of
		 * everything, but since JUnit keeps references to all test cases, a
		 * little help might be a good idea.
		 * 
		 * Note! This is copied from InstrumentationCoreTestRunner in android
		 * code
		 */
		private void cleanup(final TestCase test) {
			Class<?> clazz = test.getClass();

			while (clazz != TestCase.class) {
				final Field[] fields = clazz.getDeclaredFields();
				for (final Field field : fields) {
					final Field f = field;
					if (!f.getType().isPrimitive()
							&& !Modifier.isStatic(f.getModifiers())) {
						try {
							f.setAccessible(true);
							f.set(test, null);
						} catch (final Exception ignored) {
							// Nothing we can do about it.
						}
					}
				}

				clazz = clazz.getSuperclass();
			}
		}
	}

	private synchronized TestInfo getTestInfo(final TestCase testCase) {
		final Class<? extends TestCase> clazz = testCase.getClass();
		final Package thePackage = clazz.getPackage();
		final String name = testCase.getName();  
		Log.d("debug", "AutoTestCase: "+name);
		StringBuilder sb = new StringBuilder();
		sb.append(thePackage).append(".").append(clazz.getSimpleName())
				.append(".").append(name);
		final String mapKey = sb.toString();
		TestCaseInfo caseInfo = caseMap.get(thePackage);
		if (caseInfo == null) {
			caseInfo = new TestCaseInfo();
			caseInfo.testCaseClass = testCase.getClass();
			caseInfo.thePackage = thePackage;
			caseMap.put(thePackage, caseInfo);
		}
		TestInfo ti = caseInfo.testMap.get(mapKey);
		if (ti == null) {
			ti = new TestInfo();
			ti.name = name;
			ti.testCase = testCase.getClass();
			ti.thePackage = thePackage;
			caseInfo.testMap.put(mapKey, ti);
		}
		return ti;
	}

	private void startFile(final File outputFile) throws IOException {
		
		Log.i(TAG, "startFile");
		Log.d(TAG, "Writing to file " + outputFile);
		currentXmlSerializer = Xml.newSerializer();
		currentFileWriter = new PrintWriter(outputFile, "UTF-8");
		currentXmlSerializer.setFeature(
				"http://xmlpull.org/v1/doc/features.html#indent-output", true);
		currentXmlSerializer.setOutput(currentFileWriter);
		currentXmlSerializer.startDocument("UTF-8", null);
		currentXmlSerializer.startTag(null, TESTSUITES);
	}

	private void endFile(File f) throws IOException {
		FileHelper.chmod(f.getAbsolutePath(), 0777);
		Log.d(TAG, "closing file");
		currentXmlSerializer.endTag(null, TESTSUITES);
		currentXmlSerializer.endDocument();
		currentFileWriter.flush();
		currentFileWriter.close();
	}

	private String getTimestamp() {
		final long time = System.currentTimeMillis();
		final SimpleDateFormat sdf = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss");
		return sdf.format(time);
	}

	private void writeClassToFile(final TestCaseInfo tci)
			throws IllegalArgumentException, IllegalStateException, IOException {
		final Package thePackage = tci.thePackage;
		final Class<? extends TestCase> clazz = tci.testCaseClass;
		final int tests = tci.testMap.size();
		final String timestamp = getTimestamp();
		int errors = 0;
		int failures = 0;
		int time = 0;
		for (final TestInfo testInfo : tci.testMap.values()) {
			if (testInfo.error != null) {
				errors++;
			}
			if (testInfo.failure != null) {
				failures++;
			}
			time += testInfo.time;
		}
		currentXmlSerializer.startTag(null, TESTSUITE);
		currentXmlSerializer.attribute(null, ERRORS, Integer.toString(errors));
		currentXmlSerializer.attribute(null, FAILURES,
				Integer.toString(failures));
		currentXmlSerializer.attribute(null, NAME, clazz.getName());
		currentXmlSerializer.attribute(null, PACKAGE, thePackage == null ? ""
				: thePackage.getName());
		currentXmlSerializer.attribute(null, TESTS, Integer.toString(tests));
		currentXmlSerializer.attribute(null, TIME,
				Double.toString(time / 1000.0));
		currentXmlSerializer.attribute(null, TIMESTAMP, timestamp);
		for (final TestInfo testInfo : tci.testMap.values()) {
			writeTestInfo(testInfo);
		}
		currentXmlSerializer.startTag(null, PROPERTIES);
		currentXmlSerializer.endTag(null, PROPERTIES);
		currentXmlSerializer.startTag(null, SYSTEM_OUT);
		currentXmlSerializer.endTag(null, SYSTEM_OUT);
		currentXmlSerializer.startTag(null, SYSTEM_ERR);
		currentXmlSerializer.endTag(null, SYSTEM_ERR);
		currentXmlSerializer.endTag(null, TESTSUITE);
	}
	
	
	private String testGetDataFromAssets(String name){
		StringBuffer sb = new StringBuffer();
		try {
			InputStream in = getContext().getAssets().open(name);
			BufferedReader input =  new BufferedReader(new InputStreamReader(in));
			String line = null;
			while (( line = input.readLine()) != null){
				sb.append(line);
				sb.append("\n");
			}
			input.close();
		} catch (IOException e) {
//			throw new RuntimeException(e)
		}
		Log.d("debug", "content: "+sb.toString());
		return sb.toString();
	}

	private void writeTestInfo(final TestInfo testInfo)
			throws IllegalArgumentException, IllegalStateException, IOException {
		currentXmlSerializer.startTag(null, TESTCASE);
		currentXmlSerializer.attribute(null, CLASSNAME,
				testInfo.testCase.getName());
		
		String jsonData = testGetDataFromAssets("testcases.json");
		String description = "";
		Log.d("debug", "jsonData: "+jsonData);
		try {
//			JSONObject jsonObj = new JSONObject(jsonData);
			JSONArray testsuites = new JSONArray(jsonData);
//			JSONArray testsuites = jsonObj.getJSONArray("testsuite");
			int count = testsuites.length();
			JSONObject testObj;
			for(int i=0; i<count; i++){
				testObj = testsuites.getJSONObject(i);
				if((testInfo.name).equals(testObj.getString("name"))){
					description = testObj.getString("description");
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		currentXmlSerializer.attribute(null, NAME, testInfo.name+": "+description);
		currentXmlSerializer.attribute(null, TIME,
				Double.toString(testInfo.time / 1000.0));
		if (testInfo.error != null) {
			currentXmlSerializer.startTag(null, ERROR);
			final StringWriter sw = new StringWriter();
			final PrintWriter pw = new PrintWriter(sw, true);
			testInfo.error.printStackTrace(pw);
			currentXmlSerializer.text(sw.toString());
			currentXmlSerializer.endTag(null, ERROR);
		}
		if (testInfo.failure != null) {
			currentXmlSerializer.startTag(null, FAILURE);
			final StringWriter sw = new StringWriter();
			final PrintWriter pw = new PrintWriter(sw, true);
			testInfo.failure.printStackTrace(pw);
			currentXmlSerializer.text(sw.toString());
			currentXmlSerializer.endTag(null, FAILURE);
		}
		currentXmlSerializer.endTag(null, TESTCASE);
	}

//	private File getJunitOutputFile(final Package p) {
	private File getJunitOutputFile(final Package p, TestCaseInfo caseInfo) {

		ArrayList<TestInfo> list = new ArrayList<CustomInstrumentationTestRunner.TestInfo>();
		for(TestInfo ti : caseInfo.testMap.values()){
			list.add(ti);
		}
		String fileName = list.get(0).name;
		return new File(junitOutputDirectory, (p == null ? junitNoPackagePrefix
//				: p.getName()) + junitOutputFilePostfix);
				: p.getName()) + "."+fileName+".xml");
	}

	private File getJunitOutputFile() {
		return new File(junitOutputDirectory, junitSingleFileName);
	}

	private File getJunitOutputFile(final Class<? extends TestCase> clazz) {
		return new File(junitOutputDirectory, clazz.getName()
				+ junitOutputFilePostfix);
	}

	private void setDefaultParameters() {
		
		if (junitOutputDirectory == null) {
			
			//保存在sdcard的指定目录下面    2014/01/03 by haos
			junitOutputDirectory = Environment.getExternalStorageDirectory().getAbsolutePath()+
					getTargetContext().getFilesDir().getAbsolutePath();
		}
		
		
		if (junitOutputFilePostfix == null) {
//			junitOutputFilePostfix = DEFAULT_JUNIT_FILE_POSTFIX+"123";
			junitOutputFilePostfix = "-TEST"+""+".xml";
		}
		if (junitNoPackagePrefix == null) {
			junitNoPackagePrefix = DEFAULT_NO_PACKAGE_PREFIX;
		}
		if (junitSplitLevel == null) {
			junitSplitLevel = DEFAULT_SPLIT_LEVEL;
		}
		if (junitSingleFileName == null) {
			junitSingleFileName = DEFAULT_SINGLE_FILE_NAME;
		}
	}

	private boolean getBooleanArgument(final Bundle arguments,
			final String tag, final boolean defaultValue) {
		final String tagString = arguments.getString(tag);
		if (tagString == null) {
			return defaultValue;
		}
		return Boolean.parseBoolean(tagString);
	}

	@Override
	public void onCreate(final Bundle arguments) {
		
		Log.d(TAG,
				"Creating the Test Runner with arguments: "
						+ arguments.keySet());
		if (arguments != null) {
			junitOutputEnabled = getBooleanArgument(arguments,
					"junitXmlOutput", true);
			junitOutputDirectory = arguments.getString("junitOutputDirectory");
			junitOutputFilePostfix = arguments
					.getString("junitOutputFilePostfix");
			junitNoPackagePrefix = arguments.getString("junitNoPackagePrefix");
			junitSplitLevel = arguments.getString("junitSplitLevel");
			junitSingleFileName = arguments.getString("junitSingleFileName");
			justCount = getBooleanArgument(arguments, "count", false);
			logOnly = getBooleanArgument(arguments, "log", false);
			
		}
		setDefaultParameters();
		createDirectoryIfNotExist();
		super.onCreate(arguments);
	}

	private boolean createDirectoryIfNotExist() {
		boolean created = false;
		Log.d(TAG, "Creating output directory if it does not exist");
		File directory = new File(junitOutputDirectory);
		if (!directory.exists()) {
			created = directory.mkdirs();
		}
		Log.d(TAG, "Created directory? " + created);
		return created;
	}

	private void deleteOldFiles() {
		Log.d(TAG, "Deleting old files");
		final File[] filesToDelete = new File(junitOutputDirectory)
				.listFiles(new FilenameFilter() {
					public boolean accept(final File dir, final String filename) {
						return filename.endsWith(junitOutputFilePostfix)
								|| filename.equals(junitSingleFileName);
					}
				});
		if (filesToDelete != null) {
			Log.d(TAG, "Deleting: " + Arrays.toString(filesToDelete));
			for (final File f : filesToDelete) {
				f.delete();
			}
		}
	}
	
	/**
	 * 删除旧的xml， 
	 */
	private void delOldFilesXML(){
		File f = new File(junitOutputDirectory);
		File[] files;
		if(f.exists()){//存在就去删除下面的文件
			files = f.listFiles();
			for(File def : files){
				if(def.getName().endsWith(".xml"))
					def.delete();
			}
		}
	}

	@Override
	public void finish(final int resultCode, final Bundle results) {
		if (outputEnabled) {
			Log.d(TAG, "Post processing");
			if (SPLIT_LEVEL_PACKAGE.equals(junitSplitLevel)) {
				processPackageLevelSplit();
			} else if (SPLIT_LEVEL_CLASS.equals(junitSplitLevel)) {
				processClassLevelSplit();
			} else if (SPLIT_LEVEL_NONE.equals(junitSplitLevel)) {
				processNoSplit();
			} else {
				Log.d(TAG, "Invalid split level " + junitSplitLevel
						+ ", falling back to package level split.");
				processPackageLevelSplit();
			}
		}
		
		String stream = results.getString("stream");
		StringBuffer sb = new StringBuffer();
		sb.append("\n");
		sb.append("Test: "+runTest);
		sb.append("\n");
		sb.append(stream);
		
		
		stream = sb.toString();
		results.putString("stream", stream);
		/*Set<String> sets = results.keySet();
		for(String s : sets){
			Log.d("debug", "finish-results: "+s+": "+results.getString(s));
		}*/
		super.finish(resultCode, results);
	}

	private void processNoSplit() {
		try {
			final File f = getJunitOutputFile();
			startFile(f);
			try {
				for (final Package p : caseMap.keySet()) {
					try {
						final TestCaseInfo tc = caseMap.get(p);
						writeClassToFile(tc);
					} catch (final IOException e) {
						Log.e(TAG, "Error: " + e, e);
					}
				}
			} finally {
				endFile(f);
			}
		} catch (final IOException e) {
			Log.e(TAG, "Error: " + e, e);
		}
	}

	private void processPackageLevelSplit() {
		Log.d(TAG, "Packages: " + caseMap.size());
		for (final Package p : caseMap.keySet()) {
			Log.d(TAG, "Processing package " + p);
			try {
//				final File f = getJunitOutputFile(p);
				final File f = getJunitOutputFile(p, caseMap.get(p));
				startFile(f);
				try {
					final TestCaseInfo tc = caseMap.get(p);
					writeClassToFile(tc);
				} finally {
					endFile(f);
				}
			} catch (final IOException e) {
				Log.e(TAG, "Error: " + e, e);
			}
		}
	}

	private void processClassLevelSplit() {
		for (final Package p : caseMap.keySet()) {
			try {
				final TestCaseInfo tc = caseMap.get(p);
				final File f = getJunitOutputFile(tc.testCaseClass);
				startFile(f);
				try {
					writeClassToFile(tc);
				} finally {
					endFile(f);
				}
			} catch (final IOException e) {
				Log.e(TAG, "Error: " + e, e);
			}
		}
	}

	@Override
	protected AndroidTestRunner getAndroidTestRunner() {
		Log.d(TAG, "Getting android test runner");
		runner = super.getAndroidTestRunner();
		if (junitOutputEnabled && !justCount && !logOnly) {
			Log.d(TAG, "JUnit test output enabled");
			outputEnabled = true;
			runner.addTestListener(new JunitTestListener());
		} else {
			outputEnabled = false;
			Log.d(TAG, "JUnit test output disabled: [ junitOutputEnabled : "
					+ junitOutputEnabled + ", justCount : " + justCount
					+ ", logOnly : " + logOnly + " ]");
		}
		return runner;
	}
	
	
	
	
	
}