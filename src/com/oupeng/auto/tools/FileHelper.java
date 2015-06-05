package com.oupeng.auto.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import android.annotation.SuppressLint;
import android.app.Instrumentation;
import android.app.UiAutomation;
import android.content.Context;
import android.graphics.Bitmap;

@SuppressLint("NewApi")
public class FileHelper {
	
	/**
	 * 将流转换为字符串
	 * @param inputStream
	 * @return
	 */
	public static String getString(InputStream inputStream){
		BufferedReader bufferedReader;
		StringBuffer strBuffer = new StringBuffer();
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				strBuffer.append(line);
				strBuffer.append("\n");
			}
			bufferedReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return strBuffer.toString();
	}
	
	/**
	 * 截图，包括输入法和状态栏,  如果被测应用有android.permission.WRITE_EXTERNAL_STORAGE权限启API>18?
	 * @param fileName
	 * @param directory
	 * @param quality
	 * @param inst
	 */
	public static void takeScreenshot(String fileName, String directory,
			int quality, Instrumentation inst) {
		UiAutomation uiAutomation = inst.getUiAutomation();
		Bitmap bitmap = uiAutomation.takeScreenshot();
		FileOutputStream fos = null;
		File parentDirectory = new File(directory);
		if(!parentDirectory.exists())
			parentDirectory.mkdir();
		File fileToSave = new File(directory, fileName);
		try {
			fos = new FileOutputStream(fileToSave);
			if(fileName.endsWith(".png")){
				bitmap.compress(Bitmap.CompressFormat.PNG, quality, fos);
			}else if(fileName.endsWith(".jpg")){
				bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos);
			}
	     	fos.flush();
		    fos.close();
		} catch (Exception e) {
			OupengAutoLog.d("Can't save the screenshot! Requires write permission (android.permission.WRITE_EXTERNAL_STORAGE) in AndroidManifest.xml of the application under test.");
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 根据文件名获取文件 
	 * @param c
	 * @param path
	 * @param fileName
	 * @return
	 */
	public static File getFile(Context c, String path, String fileName) {
		File file = new File(path);
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File f : files) {
				if (f.isFile() && f.getName().equals(fileName)) {
					OupengAutoLog.d("fileName: " + f.getName()
							+ "  size: " + f.length());
					return f;
				}
			}
		}
		return null;
	}
	
	
	/**
	 * 取指定目录下的所有文件，但是不包括文件夹
	 * @param c
	 * @param path 目录的路径
	 * @return
	 */
	public static List<File> getFiles(Context c, String path) {
		ArrayList<File> fileList = new ArrayList<File>();
		File file = new File(path);
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File f : files) {
				if (f.isFile())
					OupengAutoLog.d("fileName: " + f.getName()
							+ "  size: " + f.length());
				fileList.add(f);
			}
		}
		return fileList;
	}
	
	public static void chmod(String filename, int permissions) {
		Class<?> fileUtils = null;
		try {
			fileUtils = Class.forName("android.os.FileUtils");
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		Method setPermissions = null;
		int a;
		try {
			setPermissions = fileUtils.getMethod("setPermissions", 
					new Class[]{String.class, int.class, int.class, int.class});
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}

		try {
			a = (Integer) setPermissions.invoke(null, filename, permissions, -1, -1);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

}
