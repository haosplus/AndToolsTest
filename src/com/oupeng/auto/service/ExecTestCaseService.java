package com.oupeng.auto.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.nbapp.qunimei.test.R;
import com.oupeng.auto.tools.FileHelper;
import com.oupeng.auto.tools.OupengAutoLog;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.InstrumentationInfo;
import android.os.IBinder;

@SuppressLint("NewApi")
public class ExecTestCaseService extends Service {
	public static final String CASENAME = "casename";
	public static final String CLASSNAME = "classname";
	
	
	@Override
	public int onStartCommand(final Intent intent, int flags, int startId) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				String caseName = intent.getStringExtra(CASENAME);
				String className = intent.getStringExtra(CLASSNAME);
				if("".equals(caseName) || "".equals(className))
					return;
				Process p;
				InputStream inputStream;
				String result = null;
				String cmd = "am instrument --user 0 -e class "+className+"#"
						+ caseName
						+ " -w "+getPackageName()+"/"+getInstrumentationInfo().name;   
				OupengAutoLog.i(cmd);
				try {
					p = Runtime.getRuntime().exec(cmd);
					inputStream = p.getInputStream();
					result = FileHelper.getString(inputStream); // 获取执行的结果以便于以后的结果展示
					if (result.length() == 0) {
						cmd = "am instrument -e class "+className+"#"
								+ caseName
								+ " -w "+getPackageName()+"/"+getInstrumentationInfo().name;   
						
						p = Runtime.getRuntime().exec(cmd);
						inputStream = p.getInputStream();
						result = FileHelper.getString(inputStream);
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
				
				OupengAutoLog.i(result);
				
				/*Intent broadcast = new Intent();
				broadcast.putExtra("result", result);
				broadcast.putExtra("testname", caseName);
				broadcast.setAction(CustomBroadcastReceiver.TEST_FINISH);
				sendBroadcast(broadcast);*/
				showNotification(caseName+"测试完成", result);
				
			}
		}).start();
		return super.onStartCommand(intent, flags, startId);
	}
	
	
	private void showNotification(String title, String result){
		int id = 1;
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Notification.Builder builder = new Notification.Builder(this);
		builder.setSmallIcon(R.drawable.ic_launcher);
		builder.setContentTitle(title);
		builder.setContentText(result);
		builder.setAutoCancel(true);
		notificationManager.notify(id, builder.build());
	}
	
	/**
	 * 获取InstrumentationInfo
	 * @return
	 */
	private InstrumentationInfo getInstrumentationInfo(){
		List<InstrumentationInfo> list = getPackageManager().queryInstrumentation(null, 0);
		for(InstrumentationInfo info : list){
			if(info.packageName.equals(getPackageName()))
				return info;
		}
		return null;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
}
