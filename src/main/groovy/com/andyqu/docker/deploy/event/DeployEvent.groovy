package com.andyqu.docker.deploy.event

import com.andyqu.docker.BaseBean


class DeployEvent extends BaseBean{
	String containerName
	DeployStage stage	
}
