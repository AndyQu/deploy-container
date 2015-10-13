package com.sankuai.srq.deploy

import de.gesellix.docker.client.DockerClient
import de.gesellix.docker.client.DockerClientImpl
import de.gesellix.docker.client.DockerResponse
import groovy.json.internal.LazyMap
import org.slf4j.LoggerFactory

class DeployEngine {
    static {
        Tool.extendLog4j()
        DockerTool.extendDockerClientImpl()
    }
    def static final logger = LoggerFactory.getLogger("DeployEngine")
    def DockerClient dClient
    def String host

    def DeployEngine(String dockerHost, int port) {
        //'http://172.27.2.94:4243'
        host = dockerHost
        dClient = new DockerClientImpl(dockerHost: "${dockerHost}:${port}",)
    }

    def _deploy(String contextFolderPath, String containerId, ProjectMeta pMeta) {
        /**
         * 产生bash脚本,用于在docker容器内部部署Project:
         * 1. 创建环境变量
         * 2. 将代码从gitRepoUri上pull下来,切换到分支:GitbranchName
         * 3. 初始化subModule,并且切换到分支:SubModuleBranchName(如果subModuleBranchName为空,则不需要作这一步)
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
         */
        if (pMeta.subModuleBranchName != null) {
            deployFile << """
git submodule init
git submodule update
git submodule foreach git checkout ${pMeta.subModuleBranchName}
"""
        }
        /**
         * 个性化部署脚本
         */
        if (pMeta.deployScriptFile != null) {
            DeployEngine.class.getResource("/${pMeta.deployScriptFile}").withReader {
                deployFile << it
            }
        } else {
            throw new Exception("${pMeta.projectName} 没有指定文件DeployScriptFile")
        }

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
        logger.info(response.stream.text)
        long endT=System.currentTimeMillis();
        logger.info("[Time Cost] ${endT-startT}ms")
    }

    def deploy(String ownerName, List<ProjectMeta> pMetaList) {
        def contextFolderPath = null
        def deployScriptPath = null
        /**
         * 根据分支名称,产生md5,与ownerName一起作为docker的名称
         *
         * */
        String dockerName = "${ownerName}-" + Tool.generateMD5(
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
        contextFolderPath = "/docker-deploy/${dockerName}/"

        /**
         * 端口映射信息处理:
         * 检查当前是否已经存在同名的docker实例
         */
        Boolean dockerExists = false
        def container = dClient.queryContainerName(dockerName)
        List<Object> configedPortList = null
        if (container != null) {
            logger.info("docker container ${dockerName} 已存在. 使用它已申请的端口")
            configedPortList = queryExistingPorts(dClient, container)
        } else {
            configedPortList = allocateNewPorts(pMetaList)
        }

        /**
         * 需要挂载的目录:
         */
        def (allMountPoints, nonLibMountPoints) = calcMountPoints(pMetaList, dockerName, contextFolderPath)
        /**
         * 停止/删除已存在的container
         */
        if (container != null) {
            dClient.stopAndRemoveContainer(container.Id)
        }
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
                "Image"       : "srq/ubuntu:1.1",
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
                ]
        ]
        logger.info("将要创建的container信息:")
        logger.info(containerConfig)
        def response = dClient.createContainer(containerConfig, [name: dockerName])
        if (response.status.success) {
            dClient.startContainer(response.content.Id)
            logger.info("创建并启动container成功")
        } else {
            logger.error("创建container失败:${dockerName}. 返回信息如下:")
            logger.error(response)
            throw new Exception("创建container失败:${dockerName}.")
        }

        /**
         * 对于每一个ProjectMeta, 进行部署工作
         */
        pMetaList.each {
            pMeta ->
                _deploy(contextFolderPath, response.content.Id, pMeta as ProjectMeta)
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

        logger.info(container.Ports.toString())
        return container.Ports
    }

    /**
     * 搜集并分配所有需要被映射的端口,包括debug端口
     * @param pMetaList
     * @return
     */
    def static List<Object> allocateNewPorts(List<ProjectMeta> pMetaList) {
        /**
         * 搜集所有需要被映射的端口
         */
        logger.info("[Collects Ports]Begins")
        Set<PortMeta> portSet = pMetaList.inject(new HashSet<PortMeta>()) {
            portSet, pMeta ->
                pMeta.portList.each {
                    portMeta ->
                        if (portMeta.port == 8000) {
                            def eMsg = "project ${pMeta.projectName} applies for 8000 port, which is used by Java Debug"
                            logger.warn eMsg
                        } else if (portSet.contains(portMeta)) {
                            def eMsg = "2 project apply for the same port:${portMeta.port}."
                            logger.error eMsg
                            throw new Exception(eMsg)
                        } else {
                            logger.info("collect port from project ${pMeta.projectName}: ${portMeta}")
                            portSet.add(portMeta)
                        }
                }
                portSet
        }
        Boolean needDebugPort = pMetaList.inject(false) {
            needDebugPort, it -> needDebugPort || it.needJavaDebugPort
        }
        if (needDebugPort) {
            portSet.add(new PortMeta(port: 8000, description: "Java Debug Port"))
        }
        logger.info("[Collects Ports]Ends")
        /**
         * 分配可用端口
         */
        logger.info("[Find available ports]Begins")
        int nextPort = 20000
        List<Object> newPortList = portSet.collect {
            portMeta ->
                if(portMeta.hostPort>0){
                    if(Tool.isPortInUse("0.0.0.0",portMeta.hostPort)){
                        logger.error("申请的目标host端口已经被占用:${portMeta.hostPort}")
                        System.exit(-1)
                    }else{
                        def p = [IP: '0.0.0.0', PrivatePort: portMeta.port, PublicPort: portMeta.hostPort, Type: "tcp"] as LazyMap
                        logger.info("固定端口:${p}")
                        p
                    }
                }else {
                    for (nextPort++; Tool.isPortInUse("0.0.0.0", nextPort); nextPort++) {
                    }
                    def p = [IP: '0.0.0.0', PrivatePort: portMeta.port, PublicPort: nextPort, Type: "tcp"] as LazyMap
                    logger.info("发现端口:${p}")
                    p
                }
        }
        logger.info("[Find available ports]Ends")
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
    def static calcMountPoints(List<ProjectMeta> pMetaList, dockerName, contextFolderPath) {
        Set<String> containerInnerVolumnSet = pMetaList.inject(new HashSet<>()) {
            volumnSet, pMeta ->
                if (pMeta.logFolder != null) {
                    if (volumnSet.contains(pMeta.logFolder)) {
                        def eMsg = "2 project apply for the same log folder:${pMeta.logFolder}."
                        logger.info eMsg
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
        if (pMetaList.find { pMeta -> pMeta.needMountNodeLib }) {
            mounts.add([
                    "Source"     : "/home/sankuai/fe-paidui/node_modules/",
                    "Destination": "/src/node_modules/",
                    "RW"         : true
            ])
        }
        if (pMetaList.find { pMeta -> pMeta.needMountGradleLib }) {
            mounts.add(
                    [
                            "Source"     : "/home/sankuai/.gradle/",
                            "Destination": "/root/.gradle/",
                            "RW"         : true
                    ]
            )
        }
        mounts.add(
                [
                        "Source"     : "/home/sankuai/.ssh/",
                        "Destination": "/root/.ssh",
                        "RW"         : true
                ]
        )
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
        logger.info("删除文件夹:${contextDir}")
        logger.info(contextFile.deleteDir())
        logger.info("创建文件夹:${contextDir}")
        logger.info(contextFile.mkdirs())
        subFolders.each {
            path ->
                def subpath = "${contextDir}/" + path.split("/").last()
                logger.info("\t创建子文件夹:${subpath}")
                logger.info(new File(subpath).mkdirs())

        }
    }
}
