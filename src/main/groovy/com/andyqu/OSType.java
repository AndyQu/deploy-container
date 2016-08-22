package com.andyqu;

public enum OSType {
	Mac("mac"),Ubuntu("ubuntu"),Windows("windows"),Other("not set");
	private String osName;
	private OSType(String osName){
		this.osName=osName;
	}
	public static OSType queryOSType(){
		String osName = System.getProperty("os.name").toLowerCase();
		if(osName.contains("windows")){
			return Windows;
		}else if(osName.contains("mac")){
			return Mac;
		}else if(osName.contains("ubuntu")){
			return OSType.Ubuntu;
		}else{
			Other.setOsName(osName);
			return Other;
		}
	}
	public String getOsName() {
		return osName;
	}
	public void setOsName(String osName) {
		this.osName = osName;
	}
}
