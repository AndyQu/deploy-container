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
	static String authenticationDatabase="admin"
	
	def mongoConfig
	def credentials
	GMongoClient client
	DB db
	
	def setMongoConfig(String configJsonFile){
		this.mongoConfig=new JsonSlurper().parse(HistoryManager.class.getResource(configJsonFile))
		credentials = MongoCredential.createMongoCRCredential(mongoConfig.username, authenticationDatabase, mongoConfig.password as char[])
		client = new GMongoClient(new ServerAddress(mongoConfig.serverAddress, mongoConfig.serverPort), [credentials])
		db = client.getDB(mongoConfig.database)
	}
	
	HistoryManager(){
	}
	def save(DeployHistory history){
		def tName=tableName(history.getProjectName())
		db[tName].insert(history.toMap())
		LOGGER.info "event_name=save_deployhistory history_item={}", history.toSimpleString()
	}
	
	/**
	 * 条件查询
	 * @param projectName
	 * @param condition
	 * @return
	 */
	def List<DeployHistory> fetchHistories(String projectName, condition=[:], sort=[startTimeStamp:-1],limit=java.lang.Integer.MAX_VALUE){
		List<DeployHistory> histories=[]
		def tableName=tableName(projectName)
		try{
			LOGGER.info("event_name=fetching_histories projectName={} condition={}", tableName, condition)
			//降序
			db."${tableName}".find(condition).limit(limit).sort(sort).each {
				//					LOGGER.info("event_name=show_history history={}",new JsonBuilder(it).toPrettyString())
				def tm=it.toMap()
				tm.remove("_id")
				DeployHistory his=new DeployHistory(tm)
				LOGGER.info "event_name=show_history key={} history={}",tableName, his.toSimpleString()
				histories.add(his)
			}
			histories
		}catch(MongoTimeoutException e){
			LOGGER.error "event_name=history_server_timeout projectName={} e={}", tableName, e
			histories
		}
	}
	
	static String tableName(String projectName){
		projectName.toLowerCase()
	}
}
