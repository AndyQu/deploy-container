package com.sankuai.srq.deploy.script
import com.sankuai.srq.deploy.DeployEngine
import com.sankuai.srq.deploy.ProjectMeta
import com.sankuai.srq.deploy.ProjectMetaManager;

import groovy.json.JsonSlurper
import org.slf4j.MDC

import java.lang.management.ManagementFactory
import java.lang.management.RuntimeMXBean

if (args.size() <= 0) {
    println("请指定要部署的工程配置文件")
    System.exit(1)
}
RuntimeMXBean rt = ManagementFactory.getRuntimeMXBean();
String pid = rt.getName();
MDC.put("PID", pid);
def jsonSlurper = new JsonSlurper();
def jsonData = jsonSlurper.parse(new FileReader(new File(args[0])))

println "useDockerSock:${jsonData.useDockerSock}"
DeployEngine engine=null
if(jsonData.useDockerSock==1){
    engine = new DeployEngine()
}else{
    engine = new DeployEngine(jsonData.dockerDaemon.host, jsonData.dockerDaemon.port)
}
ProjectMetaManager.getInstance().updateData()
engine.deploy(jsonData.ownerName, jsonData.projects as List<ProjectMeta>, jsonData.imgName, jsonData)