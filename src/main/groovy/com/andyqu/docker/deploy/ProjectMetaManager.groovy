package com.andyqu.docker.deploy
//import groovy.json.JsonBuilder
import com.andyqu.docker.deploy.model.ProjectMeta
import groovy.json.JsonSlurper

import org.slf4j.LoggerFactory

import org.eclipse.jgit.api.CloneCommand
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder

class ProjectMetaManager {
	def static final LOGGER = LoggerFactory.getLogger("DeployEngine")
	
	def pFolder
	def metasFolder
	def nameMetaMap=[:]
	def nameBashMap=[:]
	def branch="master"
	def context
//	def branch="testWindows"
//	def branch="testI"
	
	ProjectMetaManager(){
	}
	def setContext(GlobalContext context){
		this.context=context
		pFolder=context.getWorkFolder()
		metasFolder="${pFolder}/deploy_sys_project_meta/"
		updateData()
	}
	
	def updateData(){
		new File(pFolder).mkdirs()
		
		def f = new File(metasFolder)
		if(f.exists()){
//			f.deleteDir()
			LOGGER.info "已存在 ${f}. pull"
			FileRepositoryBuilder builder = new FileRepositoryBuilder();
			Repository repository = builder.setWorkTree(f).readEnvironment().build();
			Git git = new Git(repository);
			PullCommand pull = git.pull();
			pull.call();
		}else{
			LOGGER.info "${f} 不存在. "
			LOGGER.info "DO: git clone https://github.com/AndyQu/deploy_sys_project_meta.git"
			CloneCommand cloneCmd=new CloneCommand()
			cloneCmd.setURI("https://github.com/AndyQu/deploy_sys_project_meta.git")
			cloneCmd.setDirectory(new File(metasFolder))
			cloneCmd.setBranch("master")
			LOGGER.info "event_name=checkout_master result={}",cloneCmd.call()
		}
		_parse()
	}
	
	def _parse(){
		def jsonSlurper = new JsonSlurper()
		new File("${metasFolder}/metas").eachDir {
			File projectFolder->
				if(!new File("${projectFolder.absolutePath}/meta.json").exists() ){
					LOGGER.warn "文件 ${projectFolder.absolutePath}/meta.json 不存在"
				}else if(!new File("${projectFolder.absolutePath}/deploy.sh").exists() ){
					LOGGER.warn "文件 ${projectFolder.absolutePath}/deploy.sh 不存在"
				}else{
					def metaJson = jsonSlurper.parse(new FileReader("${projectFolder.absolutePath}/meta.json"))
					nameMetaMap[metaJson.projectName.toLowerCase()]=new ProjectMeta(metaJson)
//					nameMetaMap[metaJson.projectName.toLowerCase()]=metaJson as ProjectMeta
					
					nameBashMap[metaJson.projectName.toLowerCase()]="${projectFolder.absolutePath}/deploy.sh"
					LOGGER.info "读取到Project Meta信息：${metaJson.projectName}"
				}
		}
	}
	
	def Collection<String> getAllProjectNames(){
		LOGGER.info "获取所有项目名称:${nameBashMap.keySet()}"
		nameBashMap.keySet()
	}
	def Collection<ProjectMeta> getProjectMetas(Collection<String>projectNames){
		LOGGER.info "获取项目Meta：${projectNames}"
		nameMetaMap.subMap(projectNames.collect {it->it.toLowerCase()}).values()
	}
	
	def ProjectMeta getProjectMeta(String projectName){
		def meta=nameMetaMap[projectName.toLowerCase()]
		LOGGER.info "获取项目Meta：${projectName} meta={}", meta
		meta
	}
	
	def String getProjectBashFile(String projectName){
		LOGGER.info "获取项目Bash脚本：${projectName}"
		nameBashMap[projectName.toLowerCase()]
	}
}
