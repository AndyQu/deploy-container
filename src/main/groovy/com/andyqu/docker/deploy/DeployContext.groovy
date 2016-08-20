package com.andyqu.docker.deploy

import groovy.json.JsonSlurper

class DeployContext {
	def hostConfig=null
	def config=null
	
	def String getWorkFolder(){
		hostConfig.workFolder
	}
	def setEnvConfigFile(String file){
		hostConfig = new JsonSlurper().parse(DeployContext.class.getResource(file))
	}
}
