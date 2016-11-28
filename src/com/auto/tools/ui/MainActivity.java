package com.auto.tools.ui;

import com.auto.tools.utils.AutoToolsConfig;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TabHost;
import android.widget.Toast;

/**
 * 
 * @author haos
 *
 */
public class MainActivity extends TabActivity {
	private long exitTime = 0;
	private Intent intent;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TabHost tabHost = getTabHost();
		TabHost.TabSpec tabSpec;
		
		intent = new Intent().setClass(this, SmokeCaseActivity.class);
		tabSpec = tabHost.newTabSpec("tab1").setIndicator("Smoke执行").setContent(intent);
    	tabHost.addTab(tabSpec);
    	
    	intent = new Intent().setClass(this, UtilsActivity.class);
		tabSpec = tabHost.newTabSpec("tab2").setIndicator("工具类").setContent(intent);
    	tabHost.addTab(tabSpec);
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN && 
				event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			if ((System.currentTimeMillis() - exitTime) > 2000) {
				Toast.makeText(this,
						AutoToolsConfig.string.exit_prompt, Toast.LENGTH_SHORT).show();
				exitTime = System.currentTimeMillis();
			} else {
				finish();
				System.exit(0);
			}
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

}
