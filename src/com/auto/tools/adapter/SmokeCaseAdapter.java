package com.auto.tools.adapter;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.auto.andtools.R;
import com.auto.tools.utils.TestCaseInfo;

public class SmokeCaseAdapter extends BaseAdapter{
	private ArrayList<TestCaseInfo> list;
	private LayoutInflater layoutInflater;
	public TestCaseInfo testCase;
	private Context context;
	private ViewCache viewCache;
	private TestCaseInfo testCaseInfo;
	
	public SmokeCaseAdapter(Context context, ArrayList<TestCaseInfo> listcases){
		this.list = listcases;
		this.context = context;
		layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
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
			viewCache.test_description_view = (TextView) convertView.findViewById(R.id.test_description);
			convertView.setTag(viewCache);
		} else {
			viewCache = (ViewCache) convertView.getTag();
		}
		testCaseInfo = list.get(position);
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
		viewCache.result_view.setOnClickListener(new OnClickListener() { 
					@Override
					public void onClick(View v) {
						Log.d("debug","position: "+position);
						Log.d("debug", list.get(position).getDescription());
						Log.d("debug","testcasename: "+list.get(position).getName());
						Log.d("debug","testresultlog:"+list.get(position).getResultLog());
						Log.d("debug","testresult:"+list.get(position).getResult());
						//if(results.size() == 0 || results.get(list.get(position).getName()) == null){
						//	Toast.makeText(context, "暂无测试结果", Toast.LENGTH_SHORT).show();
						//}else{
						//Log.d("debug", "testcaselog: "+results.get(list.get(position).getName()));
						//Toast.makeText(context,list.get(position).getResultLog(),
						//		Toast.LENGTH_SHORT).show();
						//}
						AlertDialog.Builder builder = new AlertDialog.Builder(context);
						builder.setTitle("测试结果");
						builder.setMessage(list.get(position).getResultLog());
						builder.show();
					}
				});
		return convertView;
	}

	class ViewCache {
		public ImageView result_view;
		public ImageView radiobutton_off_view;
		public TextView test_name_view;
		public TextView test_description_view;
	}
}