package com.sankuai.srq.deploy

import groovy.json.JsonBuilder

def List<ProjectMeta> readInParametersAndConfig(List<ProjectMeta> pMetaList) {
    def console = new BufferedReader(new InputStreamReader(System.in))
    def ownerName = console.readLine('please input your name:').trim()
    if (ownerName.isEmpty()) {
        println("[Error]~~~your name is required~~~")
        System.exit(1)
    }
    pMetaList.each {
        def majorBranchName = console.readLine("${it.ProjectName} branch name:").trim()
        if (majorBranchName.isEmpty()) {
            majorBranchName = "dev"
        }

        def subBranchName = null
        if (it.SubModuleBranchName != null) {
            subBranchName = console.readLine("${it.ProjectName} sub module branch name:").trim()
            if (subBranchName.isEmpty()) {
                subBranchName = "dev"
            }
        }

        it.GitbranchName = majorBranchName
        it.SubModuleBranchName = subBranchName

        if (console.readLine("Do you want to change port configuration(y/n,default no)?").trim().equalsIgnoreCase("y")) {
            it.PortList.each {
                portMeta ->
                    println "For port: \n\t${portMeta}(-1 means randomly map a host port)"
                    if (console.readLine("\tAssign the same host port(mapping to host port ${portMeta.Port})(y/n):").trim().equalsIgnoreCase("y")) {
                        portMeta.HostPort = portMeta.Port
                    }
            }
        }
    }
    pMetaList
}

Tool.extendBufferedReader()
readInParametersAndConfig(InstanceConfig.srqserver)
println(new JsonBuilder(InstanceConfig.srqserver).toPrettyString())
