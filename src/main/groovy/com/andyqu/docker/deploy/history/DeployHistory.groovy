package com.andyqu.docker.deploy.history

class DeployHistory {
	def projectNames=[];
	
	int startTimeStamp;//seconds
	String startTime;
	int endTimeStamp;//seconds
	String endTime;
	
	String containerName;
	String containerId;
	String hostName;
	String hostIp;
	
	def contextConfig;
	def containerConfig;
	
	boolean status
	
	public void setStartTimeStamp(int startTimeStamp) {
		this.startTimeStamp = startTimeStamp;
		this.startTime=new Date(startTimeStamp*1000).getDateTimeString()
	}
	public void setStartTimeStamp(long startTimeStamp) {
		setStartTimeStamp(startTimeStamp.intValue())
	}

	public void setEndTimeStamp(int endTimeStamp) {
		this.endTimeStamp = endTimeStamp;
		this.endTime=new Date(endTimeStamp*1000).getDateTimeString()
	}
	
	public void setEndTimeStamp(long endTimeStamp) {
		setEndTimeStamp(endTimeStamp.intValue())
	}
}
