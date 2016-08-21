package com.andyqu.docker.deploy.history

import org.apache.commons.beanutils.BeanUtils
import org.slf4j.Logger;
import org.slf4j.LoggerFactory

import com.andyqu.docker.deploy.Tool;
import com.gmongo.GMongoClient
import com.mongodb.DB
import com.mongodb.DBCursor
import com.mongodb.DBObject
import com.mongodb.MongoCredential
import com.mongodb.MongoTimeoutException
import com.mongodb.ServerAddress
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

class HistoryManager {
	static final Logger LOGGER = LoggerFactory.getLogger("DeployEngine")
	static HistoryManager _ins = null
	static String authenticationDatabase="admin"
	
	def mongoConfig
	def credentials
	GMongoClient client
	DB db
	
	
	
	static HistoryManager getInstance(){
		if(_ins==null){
			_ins=new HistoryManager()
			_ins.setMongoConfig()
		}
		return _ins
	}
	
	def setMongoConfig(configJson){
		this.mongoConfig=new JsonSlurper().parse(HistoryManager.class.getResource(configJson))
		credentials = MongoCredential.createMongoCRCredential(mongoConfig.username, authenticationDatabase, mongoConfig.password as char[])
		client = new GMongoClient(new ServerAddress(mongoConfig.serverAddress, mongoConfig.serverPort), [credentials])
		db = client.getDB(mongoConfig.database)
	}
	
	HistoryManager(){
	}
	def save(DeployHistory history){
		def projectNames=history.getProjectNames()
		db[getRealName(projectNames)].insert(history.toMap())
		LOGGER.info("event_name=save_deployhistory history_item={}", new JsonBuilder(history).toPrettyString())
	}
	
	/**
	 * 
	 * @param projectNames
	 * @param deployName 一次部署的名称，目前和container名称相同
	 * @return
	 */
	def DBObject fetchLatestHistory(def projectNames, String deployName){
		DBCursor cursor = db[getRealName(projectNames)].find([containerName:deployName])
		LOGGER.info("event_name=fetchLatestHistory projectNames={} deployName={}", projectNames, deployName)
		if(cursor.hasNext()){
			DBObject result = cursor.next()
			LOGGER.info("fetched_history={}", result)
			result
		}else{
			LOGGER.warn("no_history")
			null
		}
	}
	
	/**
	 * 
	 * @param projectName
	 * @return 所有的部署历史
	 */
	def List<DBObject> fetchHistories(String projectName){
		List<DBObject> histories=[]
		try{
			LOGGER.info("event_name=fetching_histories projectName={}", projectName)
			//降序
			db."${projectName}".find().sort(startTimeStamp:-1).each {
				//					LOGGER.info("event_name=show_history history={}",new JsonBuilder(it).toPrettyString())
				LOGGER.info "event_name=show_history key={} history={}",projectName, it.toString()
				histories.add(it)
			}
			histories
		}catch(MongoTimeoutException e){
			LOGGER.error "event_name=history_server_timeout projectName={} e={}", projectName, e
			histories
		}
	}
	
	static String getRealName(projectNames){
		projectNames.join('_')
	}
}
