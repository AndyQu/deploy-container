package com.andyqu.docker.deploy.event;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public enum DeployStage {
	Start(1, null),
	CheckSameNameContainer(2,null),
	StopAndRemoveContainer(3,null),
	ApplyPortsFromHost(4,null),
	CalcMountPoints(5,null),
	BuildFolders(6,null),
	SetupContainerConfig(7,null),
	CreateContainer(8,null),
	StartContainer(9,null),
		SaveFailedHistory(10,null),
	DeployProjectInContainer(10,null),
	CreateDeployFile(11,null),
	ExecDeployBashFile(12,null),
	SaveSuccessHistory(13,null),
	
	END(0,null)
	;
	private int number;
	private String description;

	private DeployStage(int n, String desc) {
		number = n;
		if(desc==null){
			description=this.name();
		}
		else{
			description = desc;
		}
		
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
