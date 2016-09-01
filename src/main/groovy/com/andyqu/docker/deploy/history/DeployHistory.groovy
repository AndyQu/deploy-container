package com.andyqu.docker.deploy.history
import groovy.transform.ToString
import org.apache.commons.lang3.builder.ReflectionToStringBuilder
import org.apache.commons.lang3.builder.ToStringStyle

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
	public void setStartTimeStamp(long startTimeStamp) {
		this.startTimeStamp = startTimeStamp;
		this.startTime=new Date(startTimeStamp*1000).getDateTimeString()
	}

	/**
	 *
	 * @param startTimeStamp 单位：秒
	 */
	public void setEndTimeStamp(long endTimeStamp) {
		this.endTimeStamp = endTimeStamp;
		this.endTime=new Date(endTimeStamp*1000).getDateTimeString()
	}
	
	def String toSimpleString(){
		ReflectionToStringBuilder builder = new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
		builder.setExcludeFieldNames("contextConfig", "containerConfig")
		builder.toString()
	}
}
