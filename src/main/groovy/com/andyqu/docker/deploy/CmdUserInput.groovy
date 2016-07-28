package com.andyqu.docker.deploy

import java.util.Collection

import com.andyqu.docker.deploy.notused.ProjectMeta
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

class CmdUserInput {
	static{
		Tool.extendBufferedReader()
	}
	def nonInteractMode=false
	def String work(Collection<String> targetProjectNames, envConfFileName){
		println "============命令行输入模块================"
		println "要部署的工程：${targetProjectNames}"
		println "使用的环境配置文件：${envConfFileName}\n"
		/*
		 * 获取工程预定义Meta
		 */
		Collection<ProjectMeta> metas = ProjectMetaManager.getInstance().getProjectMetas(targetProjectNames)

		//读取用户输入，apply到Meta
		def config = null
		if(nonInteractMode){
			config=nonInteractiveConfig(metas)
		}else{
			config=readInParametersAndConfig(metas)
		}

		//输出最终Conf文件
		def name="/tmp/"+targetProjectNames.join("_")+".json"
		def outputFile = new File(name)
		outputFile.delete()
		outputFile.write(new JsonBuilder(config).toPrettyString(), "utf-8")
		
		name
	}

	
	
	def nonInteractiveConfig(Collection<ProjectMeta> pMetaList){
		def ownerName="anony"
		println("owner name:${ownerName}")
		pMetaList.each {
			it.gitbranchName = "master"
			println("${it.projectName} branch name:${it.gitbranchName}")
		}
		[
			"ownerName": ownerName,
			"projects" : pMetaList
		]
	}

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
				it.portList.each { portMeta ->
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
}
