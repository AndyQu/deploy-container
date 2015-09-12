package com.sankuai.srq.deploy.script
import com.sankuai.srq.deploy.DeployEngine
import com.sankuai.srq.deploy.ProjectMeta
import groovy.json.JsonSlurper

if (args.size() <= 0) {
    println("请指定要部署的工程配置文件")
    System.exit(1)
}
def jsonSlurper = new JsonSlurper();
def jsonData = jsonSlurper.parse(new FileReader(new File(args[0])))

DeployEngine engine = new DeployEngine('http://172.27.2.94:4243')
engine.deploy(jsonData.ownerName, jsonData.projects as List<ProjectMeta>)