package com.auto.tools.utils;

import java.io.File;

import android.os.Environment;

/**
 * @date 2015/4/13
 * @description config
 * @author haos
 *
 */
public class AutoToolsConfig {
	/**
	 * log status
	 */
	public static boolean logStatus = true;
	
	public static String logFliterTag = "debug";
	
	public static String frameworkLogTag = "andtools";
	
	public static String resultPath = "";
	
	public static boolean DEBUG = true;
	
	/**
	 * 远程服务截图bitmap传值参数
	 */
	public static final String REMOTE_SCREEN_BITMAP_BYTES = "bitmap_bytes";
	
	public static final String TAKESCREEN_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
	
	/**
	 * 报告生成目录
	 */
	public static String reportFileFir = ""; 
	
	
	public static final class string{
		public static final String exit_prompt = "再按一次，退出程序";
		public static final String testcase_assets_name = "testcases.json";
	}
	

}
