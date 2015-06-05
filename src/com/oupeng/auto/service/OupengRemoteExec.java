package com.oupeng.auto.service;

import java.io.ByteArrayOutputStream;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;
import android.test.ActivityTestCase;
import android.util.Log;
import android.view.View;

import com.oupeng.auto.aidl.RemoteInterface;
import com.oupeng.auto.tools.OupengAutoLog;
import com.oupeng.auto.tools.OupengConfig;
import com.robotium.solo.Condition;

public class OupengRemoteExec {
	private Instrumentation inst;
//	private String serviceAction = "oupeng.auto.remoteService";
	private RemoteInterface remoteInterface;
	private Intent intentService;
	private RemoteServiceConnection remoteServiceConnection;

	public OupengRemoteExec(Instrumentation inst){
		this.inst = inst;
		intentService = new Intent();
//		Intent service = new Intent(serviceAction); 
	}

	
/*	*//**
	 * 获取远程服务intentService
	 * @return
	 *//*
	public Intent getIntentService() {
		return intentService;
	}


	*//**
	 * 设置远程服务，主要用于传值，{@link #getRemoteInterface() 之前调用}
	 * @param intentService
	 *//*
	public void setIntentService(Intent intentService) {
		this.intentService = intentService;
	}
*/


	/**
	 * 返回远程接口
	 * @return
	 */
	private RemoteInterface getRemoteInterface(){
		OupengAutoLog.d("getRemoteInterface");
		if(remoteInterface != null)
			return remoteInterface;
		
		intentService.setClass(inst.getContext(), RemoteService.class);
		remoteServiceConnection = new RemoteServiceConnection();
		
		inst.getContext().bindService(intentService, remoteServiceConnection, Context.BIND_AUTO_CREATE);
		waitForCondition(new Condition() {
			@Override
			public boolean isSatisfied() {
				OupengAutoLog.d("获取远程接口: "+remoteInterface);
				return remoteInterface != null;
			}
		}, 20000);
		return remoteInterface;
	}
	
	class  RemoteServiceConnection implements ServiceConnection {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			OupengAutoLog.d("onServiceDisconnected");
			remoteInterface = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			OupengAutoLog.d("onServiceConnected");
			remoteInterface = RemoteInterface.Stub.asInterface(service);
		}
	}
	
	public void stopRemoteService(){
		OupengAutoLog.d("stopRemoteService");
		inst.getContext().unbindService(remoteServiceConnection);
		remoteInterface = null;
	}
	
	
	
	/**
	 * Waits for a condition to be satisfied.
	 * 
	 * @param condition the condition to wait for
	 * @param timeout the amount of time in milliseconds to wait
	 * @return {@code true} if condition is satisfied and {@code false} if it is not satisfied before the timeout
	 */
	public boolean waitForCondition(Condition condition, int timeout){
		final long endTime = SystemClock.uptimeMillis() + timeout;

		while (true) {
			final boolean timedOut = SystemClock.uptimeMillis() > endTime;
			if (timedOut){
				return false;
			}

			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (condition.isSatisfied()){
				return true;
			}
		}
	}

	/**
	 * 唤醒屏幕
	 */
	public void wakeScreen(){
		try {
			getRemoteInterface().wakeScreen();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 释放唤醒屏幕
	 */
	public void releaseWakeScreen(){
		try {
			getRemoteInterface().releaseWakeScreen();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}


	/**
	 * 锁屏
	 */
	public void lockedScreen(){
		try {
			getRemoteInterface().lockedScreen();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 锁屏
	 */
	public void unLockedScreen(){
		try {
			getRemoteInterface().unLockedScreen();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取网络状态
	 */
	public void getNetworkInfo(){
		try {
			getRemoteInterface().getNetworkInfo();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 截图操作，被测应用不存在<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />调用此远程截图方法
	 * @param activity
	 */
	public void takeScreenshot(Activity currentActivity){
		if(remoteInterface != null)
			stopRemoteService();
		View view  = currentActivity.getWindow().getDecorView();
		view.buildDrawingCache(false);
		Bitmap bitmap = view.getDrawingCache();
		Bundle b = new Bundle();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bytes = stream.toByteArray(); 
        b.putByteArray(OupengConfig.REMOTE_SCREEN_BITMAP_BYTES, bytes);
        intentService.putExtras(b);
		try {
			getRemoteInterface().takeScreenshot();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
}
