package com.andyqu.docker.deploy.model

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includeNames=true, includeFields=true)
class ProjectMeta {
    def String projectName
    def String gitRepoUri
    def String gitbranchName

    def List<PortMeta> portList
    public void setPortList( pList) {
		this.portList = []
		pList.each {
			if(it instanceof PortMeta)
				this.portList.add(it)
			else
				this.portList.add(new PortMeta(it))
		}
	}
	def Boolean needJavaDebugPort

    def String logFolder
    def Boolean needMountNodeLib =false
    def Boolean needMountGradleLib =true

    def String deployScriptFile
}

@ToString(includeNames=true, includeFields=true)
@EqualsAndHashCode(excludes=["Port"])
class PortMeta {
    def int port
    def int hostPort = -1
    def String description
}