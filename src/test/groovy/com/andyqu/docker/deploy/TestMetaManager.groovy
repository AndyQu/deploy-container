package com.andyqu.docker.deploy

import org.testng.annotations.BeforeTest
import org.testng.annotations.Test

class TestMetaManager {
	GlobalContext gContext
	ProjectMetaManager metaManager
	@BeforeTest
	def void setup(){
		gContext=new GlobalContext()
		def envFolder=ConfigUtil.getOSFolderName()
		gContext.setEnvConfigFile("/${envFolder}/envConf.json")
		metaManager=new ProjectMetaManager()
		metaManager.setContext(gContext)
	}
	@Test
	def void tiger(){
		def metas=metaManager.getProjectMetas(["webhivesql"])
		metas.each {
			println it
		}
	}
}
