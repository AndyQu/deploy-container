package com.sankuai.srq.deploy.script

import com.sankuai.srq.deploy.InstanceConfig
import com.sankuai.srq.deploy.ProjectMeta
import com.sankuai.srq.deploy.Tool
import groovy.json.JsonBuilder

def readInParametersAndConfig(List<ProjectMeta> pMetaList) {
    def console = new BufferedReader(new InputStreamReader(System.in))
    def ownerName = console.readLine('please input your name:').trim()
    if (ownerName.isEmpty()) {
        println("[Error]~~~your name is required~~~")
        System.exit(1)
    }
    pMetaList.each {
        def majorBranchName = console.readLine("${it.projectName} branch name:").trim()
        if (majorBranchName.isEmpty()) {
            majorBranchName = "dev"
        }

        def subBranchName = null
        if (it.subModuleBranchName != null) {
            subBranchName = console.readLine("${it.projectName} sub module branch name:").trim()
            if (subBranchName.isEmpty()) {
                subBranchName = "dev"
            }
        }

        it.gitbranchName = majorBranchName
        it.subModuleBranchName = subBranchName

        if (console.readLine("Do you want to change port configuration(y/n,default no)?").trim().equalsIgnoreCase("y")) {
            it.portList.each {
                portMeta ->
                    println "For port: \n\t${portMeta}(-1 means randomly map a host port)"
                    if (console.readLine("\tAssign the same host port(mapping to host port ${portMeta.port})(y/n):").trim().equalsIgnoreCase("y")) {
                        portMeta.hostPort = portMeta.port
                    }
            }
        }
    }
    [
            "ownerName": ownerName,
            "projects" : pMetaList
    ]
}

/**
 * 在/tmp/目录下面产生配置文件
 */
if (args.size() <= 0) {
    println("请指定要部署的工程名称.目前支持可部署的工程包括:${InstanceConfig.projectsConfig.keySet()}")
    System.exit(1)
}else {
    Tool.extendBufferedReader()
    def projectName = args[0]
    def config = readInParametersAndConfig(InstanceConfig.projectsConfig[projectName])
    def json = new JsonBuilder(config).toPrettyString()
    println("部署配置:${json}")
    def outputFile = new File("/tmp/${projectName}.json")
    outputFile.delete()
    outputFile.write(json, "utf-8")
}
