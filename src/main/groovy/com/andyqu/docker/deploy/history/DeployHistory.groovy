package com.andyqu.docker.deploy.history

class DeployHistory {
	String projectName;
	int startTimeStamp;//seconds
	String startTime;
	int endTimeStamp;//seconds
	String endTime;
	
	String containerName;
	String containerId;
	String hostName;
	String hostIp;
	
	def deployConfig;
	
	public void setStartTimeStamp(int startTimeStamp) {
		this.startTimeStamp = startTimeStamp;
		this.startTime=new Date(startTimeStamp*1000).getDateTimeString()
	}

	public void setEndTimeStamp(int endTimeStamp) {
		this.endTimeStamp = endTimeStamp;
		this.endTime=new Date(endTimeStamp*1000).getDateTimeString()
	}
}
