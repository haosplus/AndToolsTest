package com.auto.tools.utils;

public class TestCaseInfo {
	private String name;
	private String description;
	private boolean isrun;
	private boolean result;
	private String result_log;
	
	public static final String CLASS = "class";
	public static final String TESTSUITE = "testsuite";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String ISRUN = "isrun";
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public boolean getIsRun() {
		return isrun;
	}
	public void setIsRun(boolean isrun) {
		this.isrun = isrun;
	}
	
	public boolean getResult(){
		return result;
	}
	
	public void setResult(boolean result){
		this.result = result;
	}
	
	public String getResultLog(){
		return result_log;
	}
	
	public void setResultLog(String result_log){
		this.result_log = result_log;
	}
	@Override
	public String toString() {
		return "TestCaseInfo [name=" + name + ", description=" + description
				+ ", isRun=" + isrun + "]";
	}

}
