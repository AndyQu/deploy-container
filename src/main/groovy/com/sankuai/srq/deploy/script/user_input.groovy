package com.sankuai.srq.deploy.script

import com.sankuai.srq.deploy.InstanceConfig
import com.sankuai.srq.deploy.ProjectMeta
import com.sankuai.srq.deploy.ProjectMetaManager;
import com.sankuai.srq.deploy.Tool
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

def readInParametersAndConfig(Collection<ProjectMeta> pMetaList) {
    def console = new BufferedReader(new InputStreamReader(System.in))
    def ownerName = console.readLine('please input your name:').trim()
    if (ownerName.isEmpty()) {
		ownerName="anony"
    }
	println("please input your name:${ownerName}")
    pMetaList.each {
        def majorBranchName = console.readLine("${it.projectName} branch name:").trim()
        if(!majorBranchName.isEmpty()){
            it.gitbranchName = majorBranchName
        } else if(it.gitbranchName==null){
            it.gitbranchName="dev"
        }
        println("${it.projectName} branch name:${it.gitbranchName}")

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

def objsToJson(objA, objB){
    new JsonBuilder(objA+objB).toPrettyString()
}

/**
 * 在/tmp/目录下面产生配置文件
 */
println "args:${args}"

//获取Meta数据
ProjectMetaManager.getInstance().updateData()
def validProjectNames = ProjectMetaManager.getInstance().getAllProjectNames()

if (args.size() <= 1) {
    println("请指定要部署的工程名称、环境配置文件.目前支持可部署的工程包括:${validProjectNames}")
    System.exit(1)
}else {
	def targetProjectNames = args[0].split " "
	def envConfFileName = args[1]
	
	targetProjectNames.each {
		it->
			if(!validProjectNames.contains(it)){
				println "要部署的工程 ${it} 不存在。目前支持可部署的工程包括:${validProjectNames}"
				System.exit(1)
			}
	}

    Tool.extendBufferedReader()
	Collection<ProjectMeta> metas = ProjectMetaManager.getInstance().getProjectMetas(targetProjectNames as List)
    def config = readInParametersAndConfig(metas)
	
	def jsonSlurper = new JsonSlurper()
    def json = objsToJson(config, jsonSlurper.parse(new FileReader(new File(envConfFileName))))
    println("部署配置:${json}")
	
	def name=targetProjectNames.join("_")
    def outputFile = new File("/tmp/${name}.json")
    outputFile.delete()
    outputFile.write(json, "utf-8")
}
