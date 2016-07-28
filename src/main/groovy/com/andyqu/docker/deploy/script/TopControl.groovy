package com.andyqu.docker.deploy.script

import java.nio.file.Path
import java.nio.file.StandardCopyOption;
import java.nio.file.FileSystems
import java.nio.file.Files

import com.andyqu.docker.deploy.CmdUserInput
import com.andyqu.docker.deploy.DeployEngine
import com.andyqu.docker.deploy.ProjectMetaManager
import com.andyqu.docker.deploy.model.ProjectMeta;
import com.andyqu.docker.deploy.DeployContext

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.slf4j.MDC

import java.lang.management.ManagementFactory
import java.lang.management.RuntimeMXBean

class TopControl {
	static void main(String[] args){
		def envConfFileName=args[0]
		DeployContext context = new DeployContext()
		
		//建立部署的Context
		def jsonSlurper = new JsonSlurper()
		context.hostConfig = jsonSlurper.parse(new FileReader(new File(envConfFileName)))
		ProjectMetaManager.initInstance(context)

		//是否指定了工程名称
		def validProjectNames = ProjectMetaManager.getInstance().getAllProjectNames()

		if (args.size() < 2) {
			println("请指定要部署的工程名称. 目前支持可部署的工程包括:${validProjectNames}")
			System.exit(1)
		}
		def targetProjectNames = args[1].split ";"

		/*
		 * 校验：工程名是有效
		 */
		targetProjectNames.each { it->
			if(!validProjectNames.contains(it)){
				println "要部署的工程 ${it} 不存在。目前支持可部署的工程包括:${validProjectNames}"
				System.exit(1)
			}
		}

		/*
		 * 读取用户输入，产生Conf文件
		 */
		def confFilePath = new CmdUserInput().work(targetProjectNames as List,"${context.getWorkFolder()}/${envConfFileName}")

		//合并：环境配置
		def json = objsToJson(jsonSlurper.parse(new File(confFilePath)), context.hostConfig)
		println("部署配置:${json}")
		/*
		 * 开始部署
		 */
		context.config = jsonSlurper.parseText(json)
		
		println "useDockerSock:${context.config.useDockerSock}"
		DeployEngine engine=null
		if(context.config.useDockerSock==1){
			engine = new DeployEngine()
		}else{
			engine = new DeployEngine(context.config.dockerDaemon.host, context.config.dockerDaemon.port)
		}
		ProjectMetaManager.getInstance().updateData()
		engine.deploy(context.config.ownerName, context.config.projects as List<ProjectMeta>, context.config.imgName, context.config)
	}
	
	def static objsToJson(objA, objB){
		new JsonBuilder(objA+objB).toPrettyString()
	}
}
