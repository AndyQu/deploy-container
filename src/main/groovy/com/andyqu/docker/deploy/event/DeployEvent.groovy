package com.andyqu.docker.deploy.event

import groovy.transform.ToString

@ToString
class DeployEvent {
	String containerName
	DeployStage stage	
}
