package com.andyqu.docker.deploy.history

import com.gmongo.GMongoClient
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import groovy.json.JsonSlurper

class HistoryManager {
	def mongoConfig
	def credentials
	def client
	def db
	HistoryManager(){
		def jsonSlurper = new JsonSlurper()
		this.mongoConfig = jsonSlurper.parse(new File(HistoryManager.class.getResource('/mongo.json').getPath()))
		credentials = MongoCredential.createMongoCRCredential(mongoConfig.username, mongoConfig.database, mongoConfig.password as char[])
		client = new GMongoClient(new ServerAddress(mongoConfig.host, mongoConfig.port), [credentials])
		db = client.getDB("deploy_system")	
	}
}
