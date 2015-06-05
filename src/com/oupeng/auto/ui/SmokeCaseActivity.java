package com.oupeng.auto.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.nbapp.qunimei.test.R;
import com.oupeng.auto.adapter.ExpandAdapter;
import com.oupeng.auto.service.ExecTestCaseService;
import com.oupeng.auto.tools.FileHelper;
import com.oupeng.auto.tools.TestCaseInfo;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.Toast;

public class SmokeCaseActivity extends Activity implements OnChildClickListener{
	private TestCaseInfo testCaseInfo;
	//	private SmokeCaseAdapter smokeCaseAdapter;
	//	private ArrayList<TestCaseInfo> testCaseInfos;

	private ExpandAdapter expandAdapter;

	private HashMap<String, ArrayList<TestCaseInfo>> testCaseMaps;

	private ExpandableListView expandListView;
	private Intent runcaseIntent;

	private Set<String> sets;
	private ArrayList<String> groupNames = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		expandListView = (ExpandableListView) findViewById(R.id.expandableListView_testcase);
		testCaseMaps = getTestCaseInfos();
		sets = testCaseMaps.keySet();
		for(String s : sets){
			Log.e("debug", s);
			groupNames.add(s);
		}

		expandAdapter = new ExpandAdapter(this, testCaseMaps);
		expandListView.setAdapter(expandAdapter);
		expandListView.setGroupIndicator(null);
		expandListView.expandGroup(0);
		expandListView.setOnChildClickListener(this);
		registerForContextMenu(expandListView);
	}



	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		ExpandableListView.ExpandableListContextMenuInfo expandableListmenuInfo = (ExpandableListContextMenuInfo) menuInfo;
		int positionChild = ExpandableListView.getPackedPositionChild(
				expandableListmenuInfo.packedPosition);
		if(positionChild == -1)
			return;

		menu.setHeaderTitle("Case操作");
		menu.add(0, 0, Menu.NONE, "执行");
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		ExpandableListView.ExpandableListContextMenuInfo menuInfo = (ExpandableListContextMenuInfo) item.getMenuInfo();
		long packedPosition = menuInfo.packedPosition;
		int positionGroup = ExpandableListView.getPackedPositionGroup(packedPosition);

		int positionChild = ExpandableListView.getPackedPositionChild(packedPosition);
		String className = groupNames.get(positionGroup);
		TestCaseInfo testcase = testCaseMaps.get(className).get(positionChild);
		switch (item.getItemId()) {
		case 0: // 执行用例
			Toast.makeText(this, "开始执行: "+testcase.getName(), Toast.LENGTH_SHORT).show();
			runcaseIntent = new Intent();
			runcaseIntent.setClass(this, ExecTestCaseService.class);
			runcaseIntent.putExtra(ExecTestCaseService.CASENAME, testcase.getName());
			runcaseIntent.putExtra(ExecTestCaseService.CLASSNAME, className);
			startService(runcaseIntent);
			//			Toast.makeText(this, "className:" +className+" "+testcase.getName(), 1).show();

			break;
			/**
		case 1: // 描述
			Toast.makeText(this, testcase.getDescription(), Toast.LENGTH_LONG)
					.show();
			break;

		case 2: // 执行结果
			String r = results.get(testcase.getName());
			Log.d("debug", "results-name: " + r);
			String msg = r != null ? r : "暂无结果";
			// Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("测试结果");
			builder.setMessage(msg);
			builder.show();

			break;  **/

		default:
			break;
		}
		return super.onContextItemSelected(item);
	}


	/**
	 * 获取TestCase
	 * @return
	 */
	private HashMap<String, ArrayList<TestCaseInfo>> getTestCaseInfos(){
		HashMap<String, ArrayList<TestCaseInfo>> hashMaps = new HashMap<String, ArrayList<TestCaseInfo>>();
		String json = null;
		ArrayList<TestCaseInfo> testcases;
		try {
			json = FileHelper.getString(getResources().getAssets().open(
					getString(R.string.testcase_assets_name)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		JSONArray arrays = null;
		JSONArray testsuite = null;
		JSONObject testCaseObj;
		try {
			arrays = new JSONArray(json);
			int arraysLength = arrays.length();
			for(int i = 0; i < arraysLength; i++){
				testcases = new ArrayList<TestCaseInfo>();
				testCaseObj = arrays.getJSONObject(i);
				testsuite = testCaseObj.getJSONArray(TestCaseInfo.TESTSUITE);
				String className = testCaseObj.getString(TestCaseInfo.CLASS);
				int count = testsuite.length();
				JSONObject testObj;
				for (int j = 0; j < count;) {
					testCaseInfo = new TestCaseInfo();
					try {
						testObj = testsuite.getJSONObject(j++);
						testCaseInfo.setName(testObj.getString(TestCaseInfo.NAME));
						testCaseInfo.setDescription(testObj.getString(TestCaseInfo.DESCRIPTION));
						testCaseInfo.setIsRun(testObj.getBoolean(TestCaseInfo.ISRUN));
						testcases.add(testCaseInfo);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				hashMaps.put(className, testcases);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return hashMaps;
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		TestCaseInfo testCaseInfo = testCaseMaps.get(groupNames.get(groupPosition)).get(childPosition);
		if (testCaseInfo.getIsRun()) {
			testCaseMaps.get(groupNames.get(groupPosition)).get(childPosition).setIsRun(false);
		} else {
			testCaseMaps.get(groupNames.get(groupPosition)).get(childPosition).setIsRun(true);
		}
		expandAdapter.notifyDataSetChanged();
		return false;
	}


}

