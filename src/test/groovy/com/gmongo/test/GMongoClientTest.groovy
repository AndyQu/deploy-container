package com.gmongo.test

import com.andyqu.docker.deploy.history.DeployHistory
import com.gmongo.GMongoClient
import com.mongodb.DB
import com.mongodb.DBCursor
import com.mongodb.DBObject;
import com.mongodb.ServerAddress
import com.mongodb.MongoCredential
import groovy.json.JsonBuilder

import org.testng.annotations.BeforeTest
import org.testng.annotations.Test
import org.testng.annotations.AfterTest
import org.apache.commons.lang3.builder.ReflectionToStringBuilder
import org.apache.commons.lang3.builder.ToStringStyle
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.apache.commons.beanutils.BeanUtils

class GMongoClientTest {
	def static final Logger logger = LoggerFactory.getLogger(GMongoClientTest)
	
	def username="unit_testor"
	def password="123456"
	def database="unit_test"
//	def host="localhost"
	def host="10.4.242.144"
	def port=27017
	
	def credentials
	def GMongoClient client
	def DB db
	
	@BeforeTest
	def void setup(){
		//notice:这里的"admin"指的是用于获取用户数据的database，不是用户拥有权限的database
		credentials = MongoCredential.createMongoCRCredential(username,  "admin",  password as char[])
//		client = new GMongoClient(new ServerAddress( "${host}:${port}"), [credentials])
//		credentials = MongoCredential.createScramSha1Credential(username, database, password as char[])
		client = new GMongoClient(new ServerAddress( host,  port), [credentials])

		db=client.getDB(database)
		logger.info("event_name=setup_mongodb_connection client={}", ReflectionToStringBuilder.toString(client, ToStringStyle.MULTI_LINE_STYLE))
	}
	
	@Test
	def void dog(){
		assert db.webhivesql instanceof com.mongodb.DBCollection
		assert db['webhivesql'] instanceof com.mongodb.DBCollection
		def time=System.currentTimeSeconds()
		[
			new DeployHistory(
				startTimeStamp:time,
				endTimeStamp:time+100,
				projectName:"webhivesql",
				containerName:"annoy-master-webhivesql",
				containerId:"e12eba6ee03b",
				hostName:"andyqu-dev",
				hostIp:"172.26.19.70"
			),
			new DeployHistory(
				startTimeStamp:time+2000,
				endTimeStamp:time+3000,
				projectName:"webhivesql",
				containerName:"andy-dev-webhivesql",
				containerId:"e12eba6ee03b",
				hostName:"andyqu-dev",
				hostIp:"172.26.19.70"
			),
		].each{ 
			def m = BeanUtils.describe(it)
			m.remove("metaClass")
			m.remove("class")
			db.webhivesql << m
		}
//		logger.info("event_name=find_andy-dev-webhivesql obj={}", db.webhivesql.findOne(containerName:"andy-dev-webhivesql"))
		logger.info("升序排列")
		db.webhivesql.find([projectName:"webhivesql"]).sort(startTimeStamp:1).each { 
			logger.info("event_name=show_record record={}",new JsonBuilder(it).toPrettyString())
		}
		logger.info("降序排列")
		db.webhivesql.find([projectName:"webhivesql"]).sort(startTimeStamp:-1).each {
			assert it instanceof DBObject
			logger.info("event_name=show_record record={}",new JsonBuilder(it).toPrettyString())
		}
		logger.info("找出最新的部署")
		DBCursor cursor = db.webhivesql.find([projectName:"webhivesql"]).sort(startTimeStamp:-1)
		if(cursor.hasNext()){
			logger.info("event_name=show_latest_deployment record={}",new JsonBuilder(cursor.next()).toPrettyString())
		}else{
			logger.info("event_name=没有任何部署")
		}
	}
	
	@AfterTest
	def void cleanup(){
		db.webhivesql.find().each {
			db.webhivesql.remove(it)
		}
	}
}
