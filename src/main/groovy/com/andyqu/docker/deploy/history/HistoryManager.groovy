package com.andyqu.docker.deploy.history

import org.apache.commons.beanutils.BeanUtils
import org.slf4j.Logger;
import org.slf4j.LoggerFactory

import com.gmongo.GMongoClient
import com.mongodb.DB
import com.mongodb.DBCursor
import com.mongodb.DBObject
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

class HistoryManager {
	def static final Logger logger = LoggerFactory.getLogger(HistoryManager)
	
	def mongoConfig
	def credentials
	GMongoClient client
	DB db
	static String authenticationDatabase="admin"
	HistoryManager(){
		def jsonSlurper = new JsonSlurper()
		this.mongoConfig = jsonSlurper.parse(new File(HistoryManager.class.getResource('/mongo.json').getPath()))
		credentials = MongoCredential.createMongoCRCredential(mongoConfig.username, authenticationDatabase, mongoConfig.password as char[])
		client = new GMongoClient(new ServerAddress(mongoConfig.host, mongoConfig.port), [credentials])
		db = client.getDB("deploy_system")	
	}
	def save(DeployHistory history){
		String projectName=history.getProjectName()
		db[projectName].insert(BeanUtils.describe(history))
		logger.info("event_name=save_deployhistory history_item={}", new JsonBuilder(history).toPrettyString())
	}
	
	/**
	 * 
	 * @param projectName
	 * @param deployName 一次部署的名称，目前和container名称相同
	 * @return
	 */
	def fetchLatestHistory(String projectName, String deployName){
		DBCursor cursor = db[projectName].find([containerName:deployName])
		logger.info("event_name=fetchLatestHistory projectName={} deployName={}", projectName, deployName)
		if(cursor.hasNext()){
			DBObject result = cursor.next()
			logger.info("fetched_history={}", result)
			result
		}else{
			logger.warn("no_history")
			null
		}
	}
}
