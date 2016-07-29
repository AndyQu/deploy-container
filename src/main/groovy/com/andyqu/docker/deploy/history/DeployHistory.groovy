package com.andyqu.docker.deploy.history

class DeployHistory {
	String projectName;
	int startTimeStamp;
	String startTime;
	int endTimeStamp;
	String endTime;
	
	String containerName;
	String containerId;
	String hostName;
	String hostIp;
	
	def deployConfig;
}
