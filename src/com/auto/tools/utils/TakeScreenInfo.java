package com.auto.tools.utils;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 截图的信息
 * @author haos
 *
 */
public class TakeScreenInfo implements Parcelable{
	private String name;
	private String type;
	private String path;
	private int quality;
	
	
	
	

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
	}

}
