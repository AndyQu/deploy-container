package com.sankuai.srq.deploy

class DeployContext {
	def hostConfig=null
	def config=null
	
	def String getWorkFolder(){
		hostConfig.workFolder
	}
}
