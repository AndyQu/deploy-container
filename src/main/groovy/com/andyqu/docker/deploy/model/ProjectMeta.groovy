package com.andyqu.docker.deploy.model

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

class ProjectMeta {
    def String projectName
    def String gitRepoUri
    def String gitbranchName

    def List<PortMeta> portList
    def Boolean needJavaDebugPort

    def String logFolder
    def Boolean needMountNodeLib =false
    def Boolean needMountGradleLib =true

    def String deployScriptFile
}

@ToString
@EqualsAndHashCode(excludes=["Port"])
class PortMeta {
    def int port
    def int hostPort = -1
    def String description
}