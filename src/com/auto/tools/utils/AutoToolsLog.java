package com.auto.tools.utils;

import android.util.Log;
/**
 * @date 2015/4/13
 * @author haos
 *
 */
public class AutoToolsLog{
	
	public static int d(String msg){
		return Log.d(AutoToolsConfig.logFliterTag, msg);
	}
	
	public static int d(String tag, String msg){
		return Log.d(tag, msg);
	}
	
	public static int i(String msg){
		return Log.i(AutoToolsConfig.logFliterTag, msg);
	}
	
	public static int i(String tag, String msg){
		return Log.i(tag, msg);
	}
	
	public static int e(String msg){
		return Log.e(AutoToolsConfig.logFliterTag, msg);
	}
	
	public static int e(String tag, String msg){
		return Log.e(tag, msg);
	}
}
