package com.sankuai.srq.deploy

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

class ProjectMeta {
    def String ProjectName
    def String GitRepoUri
    def String GitbranchName
    def String SubModuleBranchName

    def List<PortMeta> PortList
    def Boolean NeedJavaDebugPort

    def String LogFolder
    def Boolean NeedMountNodeLib =false
    def Boolean NeedMountGradleLib =true

    def String DeployScriptFile
}

@ToString
@EqualsAndHashCode(excludes=["Port"])
class PortMeta {
    def int Port
    def int HostPort = -1
    def String Description
}