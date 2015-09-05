package com.sankuai.srq.deploy

import groovy.transform.EqualsAndHashCode
/**
 * Created by andy on 15/9/4.
 */
class ProjectMeta {
    def String Name
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
@EqualsAndHashCode(excludes=["Port"])
class PortMeta {
    def int Port
    def String Description
}