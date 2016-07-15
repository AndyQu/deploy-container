package com.sankuai.srq.deploy
//import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

class ProjectMetaManager {
	
	def static ProjectMetaManager _ins=null
	
	def pFolder="/tmp/docker-deploy"
	def metasFolder="${pFolder}/deploy_sys_project_meta/"
	def nameMetaMap=[:]
	def nameBashMap=[:]
	
	def static ProjectMetaManager getInstance(){
		if(_ins==null){
			_ins=new ProjectMetaManager();
		}
		_ins
	}
	
	def updateData(){
		println "mkdir -p ${pFolder}".execute().text
		
		def f = new File(metasFolder)
		if(! f.exists()){
			println "${f} 不存在"
			println "git clone https://github.com/AndyQu/deploy_sys_project_meta.git".execute(null, new File(pFolder)).text
		}else{
			//TODO
			println "git checkout master;git pull".execute(null, new File(metasFolder)).text
		}
		_parse()
	}
	
	def _parse(){
		def jsonSlurper = new JsonSlurper()
		new File("${metasFolder}/metas").eachDir {
			File projectFolder->
				if(!new File("${projectFolder.absolutePath}/meta.json").exists() ){
					println "文件 ${projectFolder.absolutePath}/meta.json 不存在"
				}else if(!new File("${projectFolder.absolutePath}/deploy.sh").exists() ){
					println "文件 ${projectFolder.absolutePath}/deploy.sh 不存在"
				}else{
					def metaJson = jsonSlurper.parse(new FileReader("${projectFolder.absolutePath}/meta.json"))
					nameMetaMap[metaJson.projectName.toLowerCase()]=metaJson
					nameBashMap[metaJson.projectName.toLowerCase()]="${projectFolder.absolutePath}}/deploy.sh"
					println "读取到Project信息：${metaJson.projectName}"
				}
		}
	}
	
	def Collection<String> getAllProjectNames(){
		println "获取所有项目名称:${nameBashMap.keySet()}"
		nameBashMap.keySet()
	}
	def Collection<ProjectMeta> getProjectMetas(Collection<String>projectNames){
		println "获取项目Meta：${projectNames}"
		nameMetaMap.subMap(projectNames.collect {it->it.toLowerCase()}).values()
	}
	def String getProjectBashFile(String projectName){
		println "获取项目Bash脚本：${projectName}"
		nameBashMap[projectName.toLowerCase()]
	}
}
