package com.auto.test;

import android.app.Activity;
import android.app.Instrumentation;
import android.os.Environment;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.auto.tools.service.AndToolsRemoteExec;
import com.auto.tools.utils.FileHelper;
import com.robotium.solo.Solo;

/**
 * @date 2015/4/13
 * @author haos
 *
 */
public class SmokeAutoTest extends ActivityInstrumentationTestCase2 {
	private static final String LAUNCHER_ACTIVITY_FULL_CLASSNAME = "com.example.android.actionbarcompat.MainActivity";
	private static Class<?> launcherActivityClass;
	private Solo solo;
	private Instrumentation inst;
	private Activity activity;
	private AndToolsRemoteExec andToolsRemoteExec;
	public final static String TAG = "debug";
	static {
		try {
			launcherActivityClass = Class
					.forName(LAUNCHER_ACTIVITY_FULL_CLASSNAME);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public SmokeAutoTest(){
		super(launcherActivityClass);
	}

	@Override
	protected void setUp() throws Exception {
		inst = getInstrumentation();
		activity = getActivity();
		solo = new Solo(inst, activity);
		solo.sleep(2000);
		andToolsRemoteExec = new AndToolsRemoteExec(inst);
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		activity.finish();
		solo.getCurrentActivity().finish();
		try {
			solo.finalize();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		super.tearDown();
	}
	


	/**
	 *@date 2015/4/13
	 */
	public void testDebug(){
		andToolsRemoteExec.wakeScreen();
		andToolsRemoteExec.unLockedScreen();
		andToolsRemoteExec.stopRemoteService();
		solo.clickOnText("test");
		solo.sleep(1000);
		andToolsRemoteExec.takeScreenshot(solo.getCurrentActivity());

		Log.i(TAG, "takeScreenshot");
		FileHelper.takeScreenshot(String.valueOf(SystemClock.uptimeMillis())+".png", 
				Environment.getExternalStorageDirectory().getAbsolutePath(), 1, inst);

//		Assert.fail("失败了");
		solo.sleep(2000);
	}
	
	
}
