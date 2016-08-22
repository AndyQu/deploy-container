package com.andyqu.docker.deploy.history
import groovy.transform.ToString

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
	
	/**
	 * 
	 * @param startTimeStamp 单位：秒
	 */
	public void setStartTimeStamp(int startTimeStamp) {
		this.startTimeStamp = startTimeStamp;
		this.startTime=new Date(startTimeStamp*1000).getDateTimeString()
	}
	/**
	 *
	 * @param startTimeStamp 单位：秒
	 */
	public void setStartTimeStamp(long startTimeStamp) {
		setStartTimeStamp(startTimeStamp.intValue())
	}

	/**
	 *
	 * @param startTimeStamp 单位：秒
	 */
	public void setEndTimeStamp(int endTimeStamp) {
		this.endTimeStamp = endTimeStamp;
		this.endTime=new Date(endTimeStamp*1000).getDateTimeString()
	}
	
	/**
	 *
	 * @param startTimeStamp 单位：秒
	 */
	public void setEndTimeStamp(long endTimeStamp) {
		setEndTimeStamp(endTimeStamp.intValue())
	}
}
