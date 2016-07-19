package com.sankuai.srq.deploy.script

import java.nio.file.Path
import java.nio.file.StandardCopyOption;
import java.nio.file.FileSystems
import java.nio.file.Files

import com.sankuai.srq.deploy.CmdUserInput
import com.sankuai.srq.deploy.DeployEngine
import com.sankuai.srq.deploy.ProjectMeta
import com.sankuai.srq.deploy.ProjectMetaManager;

import groovy.json.JsonSlurper
import org.slf4j.MDC

import java.lang.management.ManagementFactory
import java.lang.management.RuntimeMXBean

class TopControl {
	static void main(String[] args){
		def projectRootFolder=args[0]
		def envConfFileName=args[1]

		//是否指定了工程名称
		ProjectMetaManager.getInstance().updateData()
		def validProjectNames = ProjectMetaManager.getInstance().getAllProjectNames()

		if (args.size() < 3) {
			println("请指定要部署的工程名称. 目前支持可部署的工程包括:${validProjectNames}")
			System.exit(1)
		}
		def targetProjectNames = args[2].split ";"

		println "ps axu".execute().text
		System.exit(1);

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
		 //准备：环境配置文件
		 Path source = FileSystems.getDefault().getPath("${projectRootFolder}/${envConfFileName}")
		 Path target = FileSystems.getDefault().getPath("${projectRootFolder}/src/main/resources/envConf.json")
		 Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING)
		 println "复制 环境配置文件 ${source.toFile().absolutePath} 到 ${target.toFile().absolutePath}"
		 */

		/*
		 * 读取用户输入，产生Conf文件
		 */
		def confFilePath = new CmdUserInput().work(targetProjectNames as List,"${projectRootFolder}/${envConfFileName}")
		//new File("/tmp/docker-deploy/config/").mkdirs()


		/*
		 * 开始部署
		 */
		/*
		 RuntimeMXBean rt = ManagementFactory.getRuntimeMXBean();
		 String pid = rt.getName();
		 MDC.put("PID", pid);
		 */
		def jsonSlurper = new JsonSlurper();
		def jsonData = jsonSlurper.parse(new FileReader(new File(confFilePath)))

		println "useDockerSock:${jsonData.useDockerSock}"
		DeployEngine engine=null
		if(jsonData.useDockerSock==1){
			engine = new DeployEngine()
		}else{
			engine = new DeployEngine(jsonData.dockerDaemon.host, jsonData.dockerDaemon.port)
		}
		ProjectMetaManager.getInstance().updateData()
		engine.deploy(jsonData.ownerName, jsonData.projects as List<ProjectMeta>, jsonData.imgName, jsonData)
	}
}
