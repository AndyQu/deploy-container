package com.sankuai.srq.deploy

import java.util.Collection;

import com.sankuai.srq.deploy.InstanceConfig
import com.sankuai.srq.deploy.ProjectMeta
import com.sankuai.srq.deploy.ProjectMetaManager;
import com.sankuai.srq.deploy.Tool
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

class CmdUserInput {
	static{
		Tool.extendBufferedReader()
	}
	def String work(Collection<String> targetProjectNames, envConfFileName){
		/*
		 * 获取工程预定义Meta
		 */
		Collection<ProjectMeta> metas = ProjectMetaManager.getInstance().getProjectMetas(targetProjectNames)

		//读取用户输入，apply到Meta
		def config = readInParametersAndConfig(metas)

		//合并：环境配置
		def jsonSlurper = new JsonSlurper()
		def json = objsToJson(config, jsonSlurper.parse(new FileReader(new File(envConfFileName))))
		println("部署配置:${json}")

		//输出最终Conf文件
		def name="/tmp/"+targetProjectNames.join("_")+".json"
		def outputFile = new File(name)
		outputFile.delete()
		outputFile.write(json, "utf-8")
		
		name
	}

	def objsToJson(objA, objB){
		new JsonBuilder(objA+objB).toPrettyString()
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
