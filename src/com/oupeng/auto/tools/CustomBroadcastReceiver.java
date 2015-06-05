package com.oupeng.auto.tools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CustomBroadcastReceiver extends BroadcastReceiver {
	public static final String TEST_FINISH = "test_finish";

	@Override
	public void onReceive(Context context, Intent intent) {
		if(TEST_FINISH.equals(intent.getAction())){
			
		}

	}

}
