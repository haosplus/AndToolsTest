package com.auto.tools.aidl;

interface RemoteInterface{
	void wakeScreen();
	void lockedScreen();
	void unLockedScreen();
	void releaseWakeScreen();
	String getNetworkInfo();
	void setMobileDataEnabled(boolean flag);
	void setWifiEnabled(boolean flag);
	void takeScreenshot();
	void saveFile(String path);
}
