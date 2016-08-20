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
import com.mongodb.ServerAddress
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

class HistoryManager {
	static final Logger logger = LoggerFactory.getLogger("DeployEngine")
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
		db[getCollectionName(projectNames)].insert(history.toMap())
		logger.info("event_name=save_deployhistory history_item={}", new JsonBuilder(history).toPrettyString())
	}
	
	/**
	 * 
	 * @param projectNames
	 * @param deployName 一次部署的名称，目前和container名称相同
	 * @return
	 */
	def fetchLatestHistory(def projectNames, String deployName){
		DBCursor cursor = db[getCollectionName(projectNames)].find([containerName:deployName])
		logger.info("event_name=fetchLatestHistory projectNames={} deployName={}", projectNames, deployName)
		if(cursor.hasNext()){
			DBObject result = cursor.next()
			logger.info("fetched_history={}", result)
			result
		}else{
			logger.warn("no_history")
			null
		}
	}
	
	static String getCollectionName(projectNames){
		projectNames.join('_')
	}
}
