package com.auto.tools.instrumentation;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import org.xmlpull.v1.XmlSerializer;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.test.InstrumentationTestRunner;
import android.util.Xml;

import com.auto.tools.utils.AutoToolsLog;
import com.auto.tools.utils.AutoToolsConfig;

/**
 * 
 * @author haos
 *
 */
public class AndToolsInstrumentationTestRunner extends InstrumentationTestRunner {
	
	private Bundle arguments;
	private int PASS = 1;
	private final int FAIL = -2;
	
	private final String TESTSUITE = "testsuite";
	private final String TESTCASE = "testcase";
	private final String CLASSNAME = "classname";
	private final String NAME = "name";
	private final String FAILURE = "failure";
	private final String TIME = "time";	
	private final String CLASS = "class";	
	private final String STREAM = "stream";	
	private final String TEST = "test";	
	
	@Override
	public void onCreate(Bundle arguments) {
		this.arguments = arguments;
		super.onCreate(arguments);
	}
	
	
	long startTime;
	long endTime;
	TestResult testResult;
	@Override
	public void sendStatus(int resultCode, Bundle results) {
		boolean argLog = Boolean.parseBoolean(arguments.getString("log"));
		AutoToolsLog.e("argLog: "+argLog);
		if(!argLog){
			AutoToolsLog.i("resultCode: "+resultCode);
			switch (resultCode) {
			case 1:  //Do nothing , init
				startTime = SystemClock.uptimeMillis();
				break;
			default:
				endTime = SystemClock.uptimeMillis();
				testResult = getTestResult(resultCode, results);
				AutoToolsLog.e(testResult.toString());
				saveResultToXMl(testResult);
				break;
			}
		}
		super.sendStatus(resultCode, results);
	}
	
	/**
	 * 将结果保存到xml文件中,需要被测进程开启权限，修改成远程调用方式保存？
	 * @param testResult
	 */
	private void saveResultToXMl(TestResult testResult){
		String reportFileFir;
		if("".equals(AutoToolsConfig.reportFileFir)){
			// 目前还没有判断是否存在sd卡
			reportFileFir = Environment.getExternalStorageDirectory().getAbsolutePath() + 
					File.separator+"automation" + File.separator + getContext().getPackageName();
		}else {
			reportFileFir = AutoToolsConfig.reportFileFir;
		}
		File reportFile = new File(reportFileFir); 
		if(!reportFile.exists())
			reportFile.mkdirs();
		
		AutoToolsLog.i(reportFileFir);
		File outFile = new File(reportFile, 
				testResult.getClassname()+"."+testResult.getTestname()+".xml");
		if(outFile.exists()){
			outFile.delete();     
		}
		XmlSerializer xmlSerializer = Xml.newSerializer();
		PrintWriter fileWriter;
		try {
			fileWriter = new PrintWriter(outFile, "UTF-8");
			xmlSerializer.setFeature(
					"http://xmlpull.org/v1/doc/features.html#indent-output", true);
			xmlSerializer.setOutput(fileWriter);
			xmlSerializer.startDocument("UTF-8", null);
			xmlSerializer.startTag(null, TESTSUITE);
			xmlSerializer.startTag(null, TESTCASE);
			xmlSerializer.attribute(null, CLASSNAME, testResult.getClassname());
			xmlSerializer.attribute(null, NAME, testResult.getTestname());
			xmlSerializer.attribute(null, TIME, testResult.getTime());
			if(testResult.getResultCode() == FAIL){  //case failed, add failure
				xmlSerializer.startTag(null, FAILURE);
				xmlSerializer.text(testResult.getFailure());
				xmlSerializer.endTag(null, FAILURE);
			}
			xmlSerializer.endTag(null, TESTCASE);
			xmlSerializer.endTag(null, TESTSUITE);
			xmlSerializer.endDocument();
			fileWriter.flush();
			fileWriter.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 将结果保存为XML
	 * @param testResult
	 */
	private void saveResultToHtml(TestResult testResult){
		
	}
	
	private TestResult getTestResult(int resultCode, Bundle results){
		TestResult testResult = new TestResult();
		String className = results.getString(CLASS);
		String testName = results.getString(TEST);
		long time = endTime - startTime;
		testResult.setResultCode(resultCode);
		testResult.setClassname(className);
		testResult.setName(testName);
		testResult.setTime(Double.toString(time / 1000.0));
		if(resultCode == FAIL){  // 1 表示成功， -2 表示失败
			String stream = results.getString(STREAM);
			testResult.setFailure(stream);
		}
		return testResult;
	}
	
	
	class TestResult{
		private int resultCode;
		private String classname;
		private String name;
		private String time;
		private String failure;
		public int getResultCode() {
			return resultCode;
		}
		public void setResultCode(int resultCode) {
			this.resultCode = resultCode;
		}
		public String getClassname() {
			return classname;
		}
		public void setClassname(String classname) {
			this.classname = classname;
		}
		public String getTestname() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getTime() {
			return time;
		}
		public void setTime(String time) {
			this.time = time;
		}
		public String getFailure() {
			return failure;
		}
		public void setFailure(String failure) {
			this.failure = failure;
		}
		@Override
		public String toString() {
			return "TestResult [resultCode=" + resultCode + ", classname="
					+ classname + ", name=" + name + ", time=" + time
					+ ", failure=" + failure + "]";
		}
		
	}
	
	
	@Override
	public void finish(int resultCode, Bundle results) {
		String stream = results.getString("stream");
		StringBuffer sb = new StringBuffer();
		sb.append("\n");
		sb.append(""+arguments.getString("class"));
		sb.append("\n");
		sb.append(stream);
		stream = sb.toString();
		results.putString("stream", stream);
		super.finish(resultCode, results);
	}
	
}
