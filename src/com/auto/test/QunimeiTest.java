package com.auto.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import junit.framework.Assert;

import com.oupeng.auto.service.OupengRemoteExec;
import com.oupeng.auto.tools.FileHelper;
import com.oupeng.auto.tools.OupengAutoLog;
import com.oupeng.auto.tools.OupengConfig;
import com.robotium.solo.Solo;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

/**
 * @date 2015/4/13
 * @author haos
 *
 */
public class QunimeiTest extends ActivityInstrumentationTestCase2 {
	private static final String LAUNCHER_ACTIVITY_FULL_CLASSNAME = "com.nbapp.qunimei.StartActivity";
	private static Class<?> launcherActivityClass;
	private Solo solo;
	private Instrumentation inst;
	private Activity activity;
	private OupengRemoteExec oupengRemoteExec;
	static {
		try {
			launcherActivityClass = Class
					.forName(LAUNCHER_ACTIVITY_FULL_CLASSNAME);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public QunimeiTest(){
		super(launcherActivityClass);
	}

	@Override
	protected void setUp() throws Exception {
		inst = getInstrumentation();
		activity = getActivity();
		solo = new Solo(inst, activity);
		solo.sleep(2000);
		oupengRemoteExec = new OupengRemoteExec(inst);
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
		oupengRemoteExec.wakeScreen();
		oupengRemoteExec.unLockedScreen();
//		oupengRemoteExec.stopRemoteService();
		solo.sleep(1000);
//		oupengRemoteExec.takeScreenshot(solo.getCurrentActivity());

		//		FileHelper.takeScreenshot(String.valueOf(SystemClock.uptimeMillis()), 
		//				Environment.getExternalStorageDirectory().getAbsolutePath(), 1, inst);

		Assert.fail("失败了");
		solo.sleep(2000);
	}
	
	
}
