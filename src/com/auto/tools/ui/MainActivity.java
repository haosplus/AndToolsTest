package com.auto.tools.ui;

import com.auto.andtools.R;
import com.auto.tools.utils.AutoToolsConfig;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
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
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.select_all:
			Toast.makeText(this, "未实现-全选", Toast.LENGTH_LONG).show();
			break;
		case R.id.cancel_select_all:
			Toast.makeText(this, "未实现-取消全选", Toast.LENGTH_LONG).show();
			break;
		case R.id.run_select_case:
			Toast.makeText(this, "未实现-执行选中", Toast.LENGTH_LONG).show();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
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
