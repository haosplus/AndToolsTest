package com.oupeng.auto.ui;


import java.util.List;

import com.nbapp.qunimei.test.R;
import com.oupeng.auto.tools.SystemInfo;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

public class UtilsActivity extends Activity {
	private String TAG = "UtilsActivity";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_utils);
		
		SystemInfo systemInfo = SystemInfo.getInstance();
		
		int[] lastPids = systemInfo.getPids(null);
		for(int pid : lastPids){
			if(pid <= 0)
				continue;
			
			int uid = systemInfo.getUidForPid(pid);
			
			String name = systemInfo.getUidName(uid, getPackageManager());
			Log.i(TAG, "name: "+name+"  pid: "+pid+"  uid: "+uid);
			
			
		}
		
		
		ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> runApps = am.getRunningAppProcesses();
		for (RunningAppProcessInfo runningAppInfo : runApps){
//			Log.i(TAG, "name: "+runningAppInfo.processName+"   pid: "+runningAppInfo.pid+"  uid: "+runningAppInfo.uid);
		}
		
		
	}

}
