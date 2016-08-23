package com.andyqu.docker.deploy
import org.testng.annotations.BeforeTest
import org.testng.annotations.Test

import com.andyqu.docker.deploy.history.DeployHistory
import com.andyqu.docker.deploy.history.HistoryManager
import com.mongodb.DBCollection
import groovy.json.JsonSlurper

import org.slf4j.Logger;
import org.testng.annotations.AfterTest

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class TestHistoryManager {
	def static final Logger logger = LoggerFactory.getLogger(TestHistoryManager)
	
	HistoryManager manager
	Tool tool = new Tool()
	
	@BeforeTest
	def void setup(){
		manager = new HistoryManager()
		manager.setMongoConfig("/mongodb.json")
	}
	
	@Test
	def void cat(){
		int time = System.currentTimeSeconds().intValue()
		[
			new DeployHistory(
				startTimeStamp:time,
				endTimeStamp:time+100,
				projectNames:["srqserver","webhivesql"],
				containerName:"annoy-master-webhivesql",
				containerId:"e12eba6ee03b",
				hostName:"andyqu-dev",
				hostIp:"172.26.19.70"
			),
			new DeployHistory(
				startTimeStamp:time+2000,
				endTimeStamp:time+3000,
				projectNames:["webhivesql","crm"],
				containerName:"andy-dev-webhivesql",
				containerId:"e12eba6ee03b",
				hostName:"andyqu-dev",
				hostIp:"172.26.19.70"
			),
		].each {
			manager.save(it)
		}
		manager.fetchLatestHistory(["webhivesql","crm"], "andy-dev-webhivesql")
	}
	
	@AfterTest
	def void cleanup(){
		[
			'webhivesql_crm',
			'srqserver_webhivesql'
		].each {
		name->
			manager.db[name].find().each {
				manager.db[name].remove it
				logger.info "removed_record={}", it 
			}
		}
	}
}
