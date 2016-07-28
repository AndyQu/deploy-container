package com.andyqu.docker.deploy

class DeployContext {
	def hostConfig=null
	def config=null
	
	def String getWorkFolder(){
		hostConfig.workFolder
	}
}
