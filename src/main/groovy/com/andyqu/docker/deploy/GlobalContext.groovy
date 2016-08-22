package com.andyqu.docker.deploy

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.json.JsonSlurper

class GlobalContext {
	def static final Logger LOGGER = LoggerFactory.getLogger("GlobalContext")
	
	def hostConfig=null
	
	def String getWorkFolder(){
		hostConfig.workFolder
	}
	def setEnvConfigFile(String file){
		hostConfig = new JsonSlurper().parse(GlobalContext.class.getResource(file))
		LOGGER.info "event_name=set_GlobalContext_envfile file={} hostConfig={}",file,hostConfig
		
	}
}
