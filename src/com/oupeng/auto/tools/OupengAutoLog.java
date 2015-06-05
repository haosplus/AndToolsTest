package com.oupeng.auto.tools;

import android.util.Log;
/**
 * @date 2015/4/13
 * @author haos
 *
 */
public class OupengAutoLog{
	
	public static int d(String msg){
		return Log.d(OupengConfig.logFliterTag, msg);
	}
	
	public static int d(String tag, String msg){
		return Log.d(tag, msg);
	}
	
	public static int i(String msg){
		return Log.i(OupengConfig.logFliterTag, msg);
	}
	
	public static int i(String tag, String msg){
		return Log.i(tag, msg);
	}
	
	public static int e(String msg){
		return Log.e(OupengConfig.logFliterTag, msg);
	}
	
	public static int e(String tag, String msg){
		return Log.e(tag, msg);
	}
}
