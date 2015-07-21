package com.auto.tools.service;

import java.io.File;
import java.io.FileOutputStream;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

import com.auto.tools.aidl.*;
import com.auto.tools.utils.AutoToolsLog;
import com.auto.tools.utils.AutoToolsConfig;

/**
 * 通过AIDL操作，解决被测程序不具备的权限操作问题，需要在测试工程的AndroidManiFestx.ml中添加相应的权限
 * @author haos
 *
 */
public class RemoteService extends Service {
	private Intent intent;
	@Override
	public IBinder onBind(Intent intent) {
		AutoToolsLog.d("onBind: "+this.getApplicationContext());
		this.intent = intent;
		return new RemoteBinder(this);
	}
	
	
	@Override
	public void onDestroy() {
		AutoToolsLog.d("onDestroy");
		super.onDestroy();
	}
	

	class RemoteBinder extends RemoteInterface.Stub{
		private WifiManager wifiManager;
		private KeyguardManager mKeyGuardManager;
		private PowerManager powerManager;
		private WakeLock wakeLock;
		private ConnectivityManager comConnectivityManager;
		private NetworkInfo networkInfo;
		RemoteBinder(Context context){
//			this.context = context;
			wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			comConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			networkInfo = comConnectivityManager.getActiveNetworkInfo();
			mKeyGuardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
			powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "OupengAutoTest");
		}

		@Override
		public void wakeScreen() throws RemoteException {
			wakeLock.acquire(); 
		}

		/**
		 * Releases the wake lock.
		 */
		@Override
		public void releaseWakeScreen() throws RemoteException {
			wakeLock.release(); 
		}

		@Override
		public void lockedScreen() throws RemoteException {
			AutoToolsLog.d("远程调用lockedScreen()");
		}

		@Override
		public void unLockedScreen() throws RemoteException {
			AutoToolsLog.d("远程调用解锁");
			mKeyGuardManager.newKeyguardLock("").disableKeyguard();
		}

		@Override
		public String getNetworkInfo() throws RemoteException {
			AutoToolsLog.d("获取网络状态");
			if(networkInfo == null)
				return "NOTCONNECTED";
			return networkInfo.getTypeName();
		}

		@Override
		public void takeScreenshot() throws RemoteException {
			Bundle bundle = intent.getExtras();
			byte[] bytes = bundle.getByteArray(AutoToolsConfig.REMOTE_SCREEN_BITMAP_BYTES);
	        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
			if(bitmap == null)
				return;
			//先默认到sd卡根目录, 需要判断是否存在sd卡
			FileOutputStream fos = null;
			String fileName = SystemClock.uptimeMillis()+".png";
			File fileToSave = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), fileName);
			try {
				fos = new FileOutputStream(fileToSave);
				if(fileName.endsWith(".png")){
					bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
				}else if(fileName.endsWith(".jpg")){
					bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
				}
		     	fos.flush();
			    fos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
		}

		@Override
		public void setMobileDataEnabled(boolean flag) throws RemoteException {
			
		}

		@Override
		public void saveFile(String path) throws RemoteException {
			
		}

	}
}
