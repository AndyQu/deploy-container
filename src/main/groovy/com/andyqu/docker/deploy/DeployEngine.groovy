package com.andyqu.docker.deploy

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.FileAppender
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy
import de.gesellix.docker.client.DockerClient
import de.gesellix.docker.client.DockerClientImpl
import de.gesellix.docker.client.DockerResponse

import groovy.json.internal.LazyMap
import groovy.json.JsonBuilder

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.LoggingEvent

import ch.qos.logback.core.util.StatusPrinter
import com.andyqu.docker.deploy.notused.PortMeta
import com.andyqu.docker.deploy.notused.ProjectMeta


class DeployEngine {
    static {
        Tool.extendSlf4j()
        DockerTool.extendDockerClientImpl()
    }
    def static logger = LoggerFactory.getLogger("DeployEngine")
    def DockerClient dClient
    def String host

    def DeployEngine(String dockerHost, int port) {
        //'http://172.27.2.94:4243'
        host = dockerHost
        dClient = DockerClientImpl(dockerHost: "${dockerHost}:${port}",)
    }

    def DeployEngine(){
        dClient = new DockerClientImpl()
        host="default-localhost"
    }

    def _deploy(String contextFolderPath, String containerId, ProjectMeta pMeta) {
		
		logger.info("\n====================部署项目${pMeta.projectName}-[开始]=======================")
        
		/**
         * 产生bash脚本,用于在docker容器内部部署Project:
         * 1. 创建环境变量
         * 2. 将代码从gitRepoUri上pull下来,切换到分支:GitbranchName
         * 3. 初始化subModule
         * 4. deployScriptFile的bash command
         */
        def deployScriptPath = "${contextFolderPath}/scripts/deploy_${pMeta.projectName}.sh"
        def deployFile = new File(deployScriptPath)
        deployFile.createNewFile()
        /**
         * 拉取主工程代码
         */
        deployFile << """
mkdir -p /src/
cd /src/
git clone ${pMeta.gitRepoUri} ${pMeta.projectName}
cd ${pMeta.projectName}
git checkout ${pMeta.gitbranchName}
git pull
"""
        /**
         * 拉取子工程代码
         * project/sub project之间的关系是其内部关系，不应该与部署系统耦合在一起。
         * 之前，部署系统会主动要求用户输入sub project的分支，然后去拉取，这是不对的。
         * RD应自己保证工程的可编译、可打包。
         */
        deployFile << """
git submodule update --init --recursive
"""
        /**
         * 个性化部署脚本
         */
		new File(ProjectMetaManager.getInstance().getProjectBashFile(pMeta.projectName)).withReader {
			deployFile << it
		}
		
		logger.info("Finnaly generated Bash Script Content:")
		deployFile.readLines().each {
			logger.info(it)
		}
		logger.info("\n\n")

        /**
         * 使用docker exec API接口, 执行自动产生的bash脚本
         */
        long startT=System.currentTimeMillis();
        DockerResponse response = dClient.exec(
                containerId,
                ['/bin/bash', "/scripts/deploy_${pMeta.projectName}.sh"],
                [
                        "Detach"     : false,
                        "AttachStdin": false,
                        "Tty"        : false
                ]
        )
        logger.info("event_name=exec_finish output=${response.stream.text}")
        long endT=System.currentTimeMillis();
        logger.info("[Time Cost] ${endT-startT}ms")
		logger.info("\n====================部署项目${pMeta.projectName}-[结束]=======================")
    }

    def deploy(String ownerName, List<ProjectMeta> pMetaList, String imgName, contextConfig) {
        def contextFolderPath = null
        def deployScriptPath = null
        /**
         * 根据分支名称,产生md5,与ownerName一起作为docker的名称
         *
         * */
        String dockerName = generateContainerName(ownerName,pMetaList) 
		
        contextFolderPath = "${contextConfig.workFolder}/${dockerName}/"
		configureLogger(contextFolderPath)

        /**
         * 检查当前是否已经存在同名的docker实例
         */
        Boolean dockerExists = false
        def container = dClient.queryContainerName(dockerName)
		logger.info "event_name=检查是否存在同名Docker_Container name=${dockerName}"
		
		/**
		 * 停止/删除已存在的container
		 */
		if (container != null) {
			logger.info("event_name=发现同名container_停止并删除它 name=${dockerName}");
			dClient.stopAndRemoveContainer(container.Id)
		}else{
			logger.info "event_name=不存在同名container name=${dockerName}"
		}
		
        List<Object> configedPortList  = allocateNewPorts(pMetaList, dClient.queryAllContainerPorts())

        /**
         * 需要挂载的目录:
         */
        def (allMountPoints, nonLibMountPoints) = calcMountPoints(pMetaList, dockerName, contextFolderPath, contextConfig)
        
        /**
         * 创建/docker-deploy目录
         * 在/docker-deploy目录下面创建属于本次部署的私有目录: /docker-deploy/${docker-name}*/
        buildContextFolder(contextFolderPath, nonLibMountPoints)

        /**
         * 再创建docker container
         * 1. container名称
         * 1. 端口映射
         * 2. 目录挂载
         */
        def containerConfig = [
                "Cmd"         : ["/sbin/init"],
                "Image"       : imgName,
                "Mounts"      : allMountPoints,
                "ExposedPorts": configedPortList.inject(new LinkedHashMap<String, Object>()) {
                    map, it ->
                        map.put("${it.PrivatePort}/tcp", new LinkedHashMap<String, Object>())
                        map
                },
                "HostConfig"  : [
                        "Binds"       : allMountPoints.collect {
                            it ->
                                "${it.Source}:${it.Destination}"
                        },
                        "PortBindings": configedPortList.inject(new LinkedHashMap<String, Object>()) {
                            map, it ->
                                map.put("${it.PrivatePort}/tcp",
                                        [[
                                                 "HostPort": "${it.PublicPort}"
                                         ]]
                                )
                                map
                        }
                ],
				"Tty": true,
        ]
		def confString=new JsonBuilder(containerConfig).toPrettyString()
        logger.info("event_name=将要创建的container信息 container_config=\n${confString}")
		
		
		logger.info("\n====================创建并启动container-[开始]=======================")
        def response = dClient.createContainer(containerConfig, [name: dockerName])
		def containerId = response.content.Id
        if (response.status.success) {
			logger.info "event_name=创建container成功 container_id=${containerId}"
            response = dClient.startContainer(containerId)
			if(response.status.success){
				logger.info("event_name=启动container成功 container_id=${containerId}")
			}else{
				logger.error("event_name=启动container失败 response=${response}")
				System.exit(1)
			}
        } else {
            logger.error("event_name=创建container失败 docker_name=${dockerName} response=${response}")
            throw new Exception("创建container失败:${dockerName}.")
        }
		logger.info("\n====================创建并启动container-[结束]=======================\n")

        /**
         * 对于每一个ProjectMeta, 进行部署工作
         */
        pMetaList.each {
            pMeta ->
                _deploy(contextFolderPath, containerId, pMeta as ProjectMeta)
        }

        logger.info("\n\n\n部署完毕.")
        logger.info("主机IP:${host}")
        configedPortList.each {
            logger.info("主机端口:${it.PublicPort}  <->  Docker端口:${it.PrivatePort}")
        }
    }

    /**
     * 查询其绑定的端口
     * @param dClient
     * @param container
     * @return
     */
    def static List<Object> queryExistingPorts(DockerClient dClient, container) {

        logger.info("容器端口：${container.Ports}")
        return container.Ports
    }

    /**
     * 搜集并分配所有需要被映射的端口,包括debug端口
     * @param pMetaList
     * @return
     */
    def static List<Object> allocateNewPorts(List<ProjectMeta> pMetaList, List<Integer> allContainerPorts) {
        /**
         * 搜集所有需要被映射的端口
         */
        logger.info("\n====================配置网络端口-[开始]=======================")
		logger.info("\n\t====================收集需要配置的网络端口-[开始]=======================")
        Set<PortMeta> portSet = pMetaList.inject(new HashSet<PortMeta>()) {
            portSet, pMeta ->
                pMeta.portList.each {
                    portMeta ->
                        if (portMeta.port == 8000) {
                            def eMsg = "project ${pMeta.projectName} 申请 8000 端口, 这个端口是Java Debug专用的"
                            logger.warn "event_name=申请了Java Debug专用端口 msg=${eMsg}"
                        } else if (portSet.contains(portMeta)) {
                            def eMsg = "两个项目 申请了同一个端口: ${portMeta.port}."
                            logger.error "event_name=不同Project申请了同一个端口 msg=${eMsg}"
                            throw new Exception(eMsg)
                        } else {
							logger.info "event_name=待分配的端口 project=${pMeta.projectName} port=${portMeta}"
                            portSet.add(portMeta)
                        }
                }
                portSet
        }
        Boolean needDebugPort = pMetaList.inject(false) {
            needDebugPort, it -> needDebugPort || it.needJavaDebugPort
        }
        if (needDebugPort) {
			logger.info "event_name=需要申请Java Debug端口8000"
            portSet.add(new PortMeta(port: 8000, description: "Java Debug Port"))
        }
		logger.info("\n\t====================收集需要配置的网络端口-[结束]=======================\n")
        /**
         * 分配可用端口
         */
		logger.info("\n\t====================申请网络端口-[开始]=======================")
        int nextPort = 20000
        List<Object> newPortList = portSet.collect {
            portMeta ->
                if(portMeta.hostPort>0){
                    if(Tool.isPortInUse("0.0.0.0",portMeta.hostPort)){
                        logger.error("event_name=申请的目标host端口已经被占用 port=${portMeta.hostPort}")
                        System.exit(-1)
                    }else{
                        def p = [IP: '0.0.0.0', PrivatePort: portMeta.port, PublicPort: portMeta.hostPort, Type: "tcp"] as LazyMap
                        logger.info("event_name=申请到固定端口 port=${p}")
                        p
                    }
                }else {
					while(true){
						boolean inUse=false
						if(Tool.isPortInUse("0.0.0.0", nextPort)){
							logger.info("event_name=端口正在被系统使用 port=${nextPort} ")
							inUse=true
						}
						if(allContainerPorts.contains(nextPort)){
							logger.info("event_name=端口被某个Docker Container占有 port=${nextPort} ")
							inUse=true
						}
						if(inUse){
							nextPort++
							logger.info("event_name=检查下一个端口 port=${nextPort}")
							continue
						}else{
							break
						}
					}
                    def p = [IP: '0.0.0.0', PrivatePort: portMeta.port, PublicPort: nextPort, Type: "tcp"] as LazyMap
					logger.info("event_name=随机分配到端口 port=${p}")
					nextPort++
					//logger.info("event_name=下一个待检查的端口 port=${nextPort}")
                    p
                }
        }
		logger.info("\n\t====================申请网络端口-[结束]=======================")
		logger.info("\n====================配置网络端口-[结束]=======================\n\n")
        newPortList
    }

    /**
     * 需要挂载的目录信息处理:
     * 1. node
     * 2. gradle
     * 3. ~/.ssh
     * 4. 日志目录
     * @param pMetaList
     * @return
     */
    def static calcMountPoints(List<ProjectMeta> pMetaList, dockerName, contextFolderPath, jsonData) {
        Set<String> containerInnerVolumnSet = pMetaList.inject(new HashSet<>()) {
            volumnSet, pMeta ->
                if (pMeta.logFolder != null) {
                    if (volumnSet.contains(pMeta.logFolder)) {
                        def eMsg = "2 project apply for the same log folder:${pMeta.logFolder}."
                        logger.info "event_name=conflict_log_folder msg=${eMsg}"
                    } else {
                        volumnSet.add(pMeta.logFolder)
                    }
                }
                volumnSet
        }
        containerInnerVolumnSet.add("/scripts")
        List<Object> mounts = containerInnerVolumnSet.collect {
            path ->
                [
                        "Source"     : "${contextFolderPath}/" + path.split("/").last(),
                        "Destination": path,
                        //"Mode"       : "ro,Z",
                        "RW"         : true
                ]
        }
		// 暂时不挂在node目录
        /*if (pMetaList.find { pMeta -> pMeta.needMountNodeLib }) {
            mounts.add([
                    "Source"     : "/home/sankuai/fe-paidui/node_modules/",
                    "Destination": "/src/node_modules/",
                    "RW"         : true
            ])
        }*/
        if (pMetaList.find { pMeta -> pMeta.needMountGradleLib }) {
            mounts.add(
                    [
                            "Source"     : "${jsonData.userFolder}/.gradle/",
                            "Destination": "/root/.gradle/",
                            "RW"         : true
                    ]
            )
        }
		
		/*
		 * Notice：Project的git repo地址必须要配置为走Http方式。如果是走ssh方式，则Host主机必须加ssh key，且这里的挂载点不能够去掉。
		 * 经验证，对于美团自己的git repo，使用http方式走不通。因此，这个挂载点不能去掉。
		 */
        mounts.add(
                [
                        "Source"     : "${jsonData.userFolder}/.ssh/",
                        "Destination": "/root/.ssh",
                        "RW"         : true
                ]
        )
		logger.info "event_name=挂载点计算完毕"
		mounts.each {
			it->
				logger.info "event_name=挂载点 desc=${it}"
		}
        [mounts, containerInnerVolumnSet]
    }

    /**
     * 在context文件夹下面,建立工程所需要的目录
     * @param contextDir
     * @param subFolders
     * @return
     */
    def static buildContextFolder(contextDir, subFolders) {
        def contextFile = new File(contextDir)
		logger.info "\n============================创建Context文件夹-[开始]==========================="
        
		logger.info(contextFile.mkdirs())
        logger.info("event_name=创建文件夹 folder=${contextDir}")
        
        subFolders.each {
            path ->
                def subpath = "${contextDir}/" + path.split("/").last()
				logger.info(new File(subpath).deleteDir())
				logger.info("event_name=删除文件夹 folder=${subpath}")
                logger.info(new File(subpath).mkdirs())
				logger.info("event_name=创建子文件夹 folder=${subpath}")
        }
		logger.info "\n============================创建Context文件夹-[结束]===========================\n"
    }
	
	def static generateContainerName(ownerName,pMetaList){
		/*
		TimeZone.setDefault(TimeZone.getTimeZone('UTC'))
		def now = new Date()
		now.format("yyyyMMdd-HH:mm:ss.SSS")
		*/
		
		"${ownerName}-" + pMetaList.collect(){
				pMeta->"${pMeta.projectName}_${pMeta.gitbranchName}"
			}.join("-")
		
		/*
		"${ownerName}-" + Tool.generateMD5(
			pMetaList.collect(){
				pMeta->pMeta.projectName
			}.join("-")
			+
			"_"
			+
			pMetaList.collect {
				pMeta ->pMeta.gitbranchName
			}.join("-")
		)
		*/
	}
	
	def ch.qos.logback.classic.Logger configureLogger(String logFileFolder){
		//https://github.com/tony19/logback-android/wiki/Appender-Notes#configuration-in-code
		logger.info "\n========================重新配置Logback-[开始]=================================="
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		/*
		 * Notice: dont reset. reset() will discard all loggers/appenders, so root logger will not function any more
		 */
//		lc.reset()
		
		RollingFileAppender<ILoggingEvent> rollingFileAppender = new RollingFileAppender<ILoggingEvent>();
		rollingFileAppender.setName("appenderForDeploy")
		rollingFileAppender.setAppend(true);
		rollingFileAppender.setContext(lc);
		rollingFileAppender.setFile("${logFileFolder}/deploy.log");
		
		SizeAndTimeBasedRollingPolicy rollingPolicy = new SizeAndTimeBasedRollingPolicy()
		rollingPolicy.setFileNamePattern("${logFileFolder}/deploy.%d{yyyy-MM-dd}.%i.log");
		rollingPolicy.setMaxFileSize("300KB")
		rollingPolicy.setMaxHistory(2)
		rollingPolicy.setParent(rollingFileAppender)
		rollingPolicy.setContext(lc)
		rollingPolicy.start()
		rollingFileAppender.setRollingPolicy(rollingPolicy)
		
		SizeBasedTriggeringPolicy triggerPolicy = new SizeBasedTriggeringPolicy()
		triggerPolicy.setMaxFileSize("300KB")
		triggerPolicy.setContext(lc)
		triggerPolicy.start()
		rollingFileAppender.setTriggeringPolicy(triggerPolicy)
		
		PatternLayoutEncoder encoder = new PatternLayoutEncoder();
		encoder.setPattern("%d{HH:mm:ss.SSS} %-5level %logger{0} %class{0} %line - %msg%n");
		encoder.setContext(lc);
		encoder.start();
		rollingFileAppender.setEncoder(encoder);
		
		rollingFileAppender.start();
		
		ch.qos.logback.classic.Logger newLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("DeployEngine");
		newLogger.setAdditive(true)
		newLogger.setLevel(ch.qos.logback.classic.Level.DEBUG)
//		logger.setLevel(ch.qos.logback.classic.Level.INFO)
		newLogger.addAppender(rollingFileAppender)
		
		StatusPrinter.print(lc);
		logger.info "\n========================重新配置Logback-[结束]==================================\n"
	}
}
