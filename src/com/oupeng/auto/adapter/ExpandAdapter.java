package com.oupeng.auto.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import com.nbapp.qunimei.test.R;
import com.oupeng.auto.tools.TestCaseInfo;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class ExpandAdapter extends BaseExpandableListAdapter {

	private LayoutInflater layoutInflater;
	private Context context;
	private Set<String> sets;
	private HashMap<String, ArrayList<TestCaseInfo>> hashMaps;
	private ViewCache viewCache;
	
	private ArrayList<String> groupNames;
	
	private TestCaseInfo testCaseInfo;
	// 设置组视图的显示文字
	public ExpandAdapter(Context context, HashMap<String, ArrayList<TestCaseInfo>> hashMaps) {
		this.context = context;
		this.hashMaps = hashMaps;
		groupNames = new ArrayList<String>();
		sets = hashMaps.keySet();
		for(String s : sets){
			Log.e("debug", s);
			groupNames.add(s);
		}
	}
	
	private TextView getTextView() {
		AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT, 64);
		TextView textView = new TextView(context);
		textView.setLayoutParams(lp);
		textView.setGravity(Gravity.CENTER_VERTICAL);
		textView.setPadding(36, 0, 0, 0);
		textView.setTextSize(20);
		textView.setTextColor(Color.BLACK);
		return textView;
	}

	@Override
	public int getGroupCount() {
		return hashMaps.size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return hashMaps.get(groupNames.get(groupPosition));
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return hashMaps.get(groupNames.get(groupPosition)).size();
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return hashMaps.get(groupNames.get(groupPosition)).get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		LinearLayout groupView = new LinearLayout(context);
		groupView.setOrientation(0);
		groupView.setBackgroundColor(Color.parseColor("#EDEDED"));
		TextView textView = getTextView();
		textView.setTextColor(Color.parseColor("#ADADAD"));
		textView.setTextSize(17);
		textView.setText(groupNames.get(groupPosition));
		groupView.addView(textView);
		return groupView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		if (convertView == null) {
			layoutInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layoutInflater.inflate(R.layout.case_item, null);
			viewCache = new ViewCache();
			viewCache.radiobutton_off_view = (ImageView) convertView
					.findViewById(R.id.radiobutton_off);
			viewCache.result_view = (ImageView) convertView
					.findViewById(R.id.result_log);
			viewCache.test_name_view = (TextView) convertView
					.findViewById(R.id.test_name);
			viewCache.run_view = (Button) convertView
					.findViewById(R.id.item_start);
			viewCache.test_description_view = (TextView) convertView.findViewById(R.id.test_description);
			convertView.setTag(viewCache);
		} else {
			viewCache = (ViewCache) convertView.getTag();
		}
		testCaseInfo = hashMaps.get(groupNames.get(groupPosition)).get(childPosition);
		viewCache.test_name_view.setText(testCaseInfo.getName());
		viewCache.test_description_view.setText(testCaseInfo.getDescription());
		if (testCaseInfo.getIsRun()) {
			// viewCache.radiobutton_off_view.setImageDrawable(
			// context.getResources().getDrawable(R.drawable.btn_radio_to_on_mtrl));
			viewCache.radiobutton_off_view.setImageDrawable(context
					.getResources()
					.getDrawable(R.drawable.check_on));
		} else {
			// viewCache.radiobutton_off_view.setImageDrawable(
			// context.getResources().getDrawable(R.drawable.btn_radio_to_off_mtrl));
			viewCache.radiobutton_off_view.setImageDrawable(context
					.getResources().getDrawable(
							R.drawable.check_off));
		}
	    
/*			if (testCaseInfo.getResult()) {
			viewCache.result_view.setImageDrawable(context.getResources()
					.getDrawable(R.drawable.result_pass));
		}else{
			viewCache.result_view.setImageDrawable(context.getResources()
					.getDrawable(R.drawable.result_fail));
		}*/
//		viewCache.result_view.setOnClickListener(new OnClickListener() { 
//					@Override
//					public void onClick(View v) {
//						Log.d("debug","position: "+position);
//						Log.d("debug", list.get(position).getDescription());
//						Log.d("debug","testcasename: "+list.get(position).getName());
//						Log.d("debug","testresultlog:"+list.get(position).getResultLog());
//						Log.d("debug","testresult:"+list.get(position).getResult());
//						//if(results.size() == 0 || results.get(list.get(position).getName()) == null){
//						//	Toast.makeText(context, "暂无测试结果", Toast.LENGTH_SHORT).show();
//						//}else{
//						//Log.d("debug", "testcaselog: "+results.get(list.get(position).getName()));
//						//Toast.makeText(context,list.get(position).getResultLog(),
//						//		Toast.LENGTH_SHORT).show();
//						//}
//						AlertDialog.Builder builder = new AlertDialog.Builder(context);
//						builder.setTitle("测试结果");
//						builder.setMessage(list.get(position).getResultLog());
//						builder.show();
//					}
//				});
		return convertView;
		
	}

	class ViewCache {
		public ImageView result_view;
		public ImageView radiobutton_off_view;
		public TextView test_name_view;
		public Button run_view;
		public TextView test_description_view;
	}
	
	
	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

}
