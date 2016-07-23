package com.sankuai.srq.deploy
//import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

import org.slf4j.LoggerFactory

class ProjectMetaManager {
	def static final logger = LoggerFactory.getLogger("DeployEngine")
	
	def static ProjectMetaManager _ins=null
	
	def pFolder
	def metasFolder
	def nameMetaMap=[:]
	def nameBashMap=[:]
	
	def static void initInstance(DeployContext context){
		_ins=new ProjectMetaManager(context);
		_ins.updateData()
	}		
	
	def static ProjectMetaManager getInstance(){
		if(_ins==null){
			
		}
		_ins
	}
	
	ProjectMetaManager(DeployContext context){
		pFolder=context.getWorkFolder()
		metasFolder="${pFolder}/deploy_sys_project_meta/"		
	}
	
	def updateData(){
		new File(pFolder).mkdirs()
		
		def f = new File(metasFolder)
		if(! f.exists()){
			logger.info "${f} 不存在. DO: git clone https://github.com/AndyQu/deploy_sys_project_meta.git"
			logger.info "git clone https://github.com/AndyQu/deploy_sys_project_meta.git".execute(null, new File(pFolder)).text
		}else{
			logger.info "${f} 已存在. DO: git checkout master;git pull"
			logger.info "git checkout master".execute(null, new File(metasFolder)).text
			logger.info "git pull".execute(null, new File(metasFolder)).text
		}
		_parse()
	}
	
	def _parse(){
		def jsonSlurper = new JsonSlurper()
		new File("${metasFolder}/metas").eachDir {
			File projectFolder->
				if(!new File("${projectFolder.absolutePath}/meta.json").exists() ){
					logger.warn "文件 ${projectFolder.absolutePath}/meta.json 不存在"
				}else if(!new File("${projectFolder.absolutePath}/deploy.sh").exists() ){
					logger.warn "文件 ${projectFolder.absolutePath}/deploy.sh 不存在"
				}else{
					def metaJson = jsonSlurper.parse(new FileReader("${projectFolder.absolutePath}/meta.json"))
					nameMetaMap[metaJson.projectName.toLowerCase()]=metaJson
					nameBashMap[metaJson.projectName.toLowerCase()]="${projectFolder.absolutePath}/deploy.sh"
					logger.info "读取到Project Meta信息：${metaJson.projectName}"
				}
		}
	}
	
	def Collection<String> getAllProjectNames(){
		logger.info "获取所有项目名称:${nameBashMap.keySet()}"
		nameBashMap.keySet()
	}
	def Collection<ProjectMeta> getProjectMetas(Collection<String>projectNames){
		logger.info "获取项目Meta：${projectNames}"
		nameMetaMap.subMap(projectNames.collect {it->it.toLowerCase()}).values()
	}
	def String getProjectBashFile(String projectName){
		logger.info "获取项目Bash脚本：${projectName}"
		nameBashMap[projectName.toLowerCase()]
	}
}
