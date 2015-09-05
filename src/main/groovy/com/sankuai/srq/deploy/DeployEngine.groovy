package com.sankuai.srq.deploy

import de.gesellix.docker.client.DockerClient
import de.gesellix.docker.client.DockerClientImpl
import groovy.json.JsonBuilder
import groovy.json.internal.LazyMap
import org.slf4j.LoggerFactory
import groovy.json.JsonOutput

class DeployEngine {
    static {
        Tool.extendLog4j()
        DockerTool.extendDockerClientImpl()
    }
    def static final logger = LoggerFactory.getLogger("DeployEngine")
    def DockerClient dClient
    def String host

    def DeployEngine(String dockerHost) {
        //'http://172.27.2.94:4243'
        host = dockerHost
        dClient = new DockerClientImpl(dockerHost: dockerHost,)
    }

    def _deploy(String containerId, ProjectMeta pMeta) {
        /**
         * 产生bash脚本,用于在docker容器内部部署Project:
         * 1. 创建环境变量
         * 2. 将代码从gitRepoUri上pull下来,切换到分支:GitbranchName
         * 3. 初始化subModule,并且切换到分支:SubModuleBranchName(如果subModuleBranchName为空,则不需要作这一步)
         * 4. deployScriptFile的bash command
         */

        /**
         * 使用docker exec API接口, 执行自动产生的bash脚本
         */
    }

    def deploy(String ownerName, List<ProjectMeta> pMetaList) {
        /**
         * 根据分支名称,产生md5,与ownerName一起作为docker的名称
         *
         * */
        String dockerName = "${ownerName}-" + Tool.generateMD5(pMetaList.collect {
            pMeta ->
                pMeta.GitbranchName
        }.join("-"))

        /**
         * 端口映射信息处理:
         * 检查当前是否已经存在同名的docker实例
         */
        Boolean dockerExists = false
        def container = dClient.queryContainerName(dockerName)
        List<Object> configedPortList = null
        if (container != null) {
            /**
             * 如果存在:
             *      1. 则检查其绑定的端口,复用它正在使用的端口. 包括debug端口
             *      2. stop,remove docker实例(container)
             */
            logger.info("docker container ${dockerName} exists. Use its ports")
            logger.info(container.Ports.toString())
            configedPortList = container.Ports
            dockerExists = true
        } else {
            /**
             * 不存在:
             *      集合所有需要被映射的端口,包括debug端口
             */
            logger.info("[Collects Ports]Begins")
            Set<PortMeta> portSet = pMetaList.inject(new HashSet<PortMeta>()) {
                portSet, pMeta ->
                    pMeta.PortList.each {
                        portMeta ->
                            if (portMeta.Port == 8000) {
                                def eMsg = "project ${pMeta.Name} applies for 8000 port, which is used by Java Debug"
                                logger.error eMsg
                                throw new Exception(eMsg)
                            } else if (portSet.contains(portMeta)) {
                                def eMsg = "2 project apply for the same port:${portMeta.Port}."
                                logger.error eMsg
                                throw new Exception(eMsg)
                            } else {
                                logger.info("collect port from project ${pMeta.Name}: ${portMeta}")
                                portSet.add(portMeta)
                            }
                    }
                    portSet
            }
            Boolean needDebugPort = pMetaList.inject(false) {
                needDebugPort, it -> needDebugPort || it.NeedJavaDebugPort
            }
            if (needDebugPort) {
                portSet.add(new PortMeta(Port: 8000, Description: "Java Debug Port"))
            }
            logger.info("[Collects Ports]Ends")
            /**
             * 寻找可用端口
             */
            logger.info("[Find available ports]Begins")
            int nextPort = 20000
            configedPortList = portSet.collect {
                portMeta ->
                    for (nextPort++; Tool.isPortInUse("0.0.0.0", nextPort); nextPort++) {
                    }
                    def p = [IP: host, PrivatePort: portMeta.Port, PublicPort: nextPort, Type: "tcp"] as LazyMap
                    logger.info("found port:${p}")
                    p
            }
            logger.info("[Find available ports]Ends")
            dockerExists = false
        }

        /**
         * 需要挂载的目录信息处理:
         * 1. node
         * 2. gradle
         * 3. ~/.ssh
         * 4. 日志目录
         */
        Set<String> containerInnerVolumnSet = pMetaList.inject(new HashSet<>()) {
            volumnSet, pMeta ->
                if (pMeta.LogFolder != null) {
                    if (volumnSet.contains(pMeta.LogFolder)) {
                        def eMsg = "2 project apply for the same log folder:${portMeta.LogFolder}."
                        logger.error eMsg
                        throw new Exception(eMsg)
                    } else {
                        volumnSet.add(pMeta.LogFolder)
                    }
                }
                volumnSet
        }
        List<Object> mounts = containerInnerVolumnSet.collect {
            path ->
                [
                        "Source"     : "/docker-deploy/${dockerName}/" + path.split("/").last(),
                        "Destination": path,
                        "Mode"       : "ro,Z",
                        "RW"         : true
                ]
        }
        if (pMetaList.find { pMeta -> pMeta.NeedMountNodeLib }) {
            mounts.add([
                    "Source"     : "/home/sankuai/fe-paidui/node_modules/",
                    "Destination": "/src/node_modules/",
                    "RW"         : true
            ])
        }
        if (pMetaList.find { pMeta -> pMeta.NeedMountGradleLib }) {
            mounts.add([
                    "Source"     : "/home/sankuai/.gradle/",
                    "Destination": "/root/.gradle/",
                    "RW"         : true
            ])
        }
        mounts.add([
                "Source"     : "/home/sankuai/.ssh/",
                "Destination": "/root/.ssh",
                "RW"         : true
        ])
        /**
         * 停止/删除已存在的container
         */
        if (dockerExists) {
            dClient.stopAndRemoveContainer(container.Id)
        }
        /**
         * 创建/docker-deploy目录
         * 在/docker-deploy目录下面创建属于本次部署的私有目录: /docker-deploy/${docker-name}*/
        def contextDir = new File("/docker-deploy/${dockerName}/")
        contextDir.deleteDir()
        contextDir.mkdirs()
        containerInnerVolumnSet.each {
            path ->
                new File("/docker-deploy/${dockerName}/" + path.split("/").last()).mkdirs()
        }
        /**
         * 再创建docker container
         * 1. container名称
         * 1. 端口映射
         * 2. 目录挂载
         */
        def containerConfig = [
                "Cmd"         : ["/sbin/init"],
                "Image"       : "srq/ubuntu:1.0",
                "Mounts"      : mounts,
                "ExposedPorts": configedPortList.inject(new LinkedHashMap<String, Object>()) {
                    map, it ->
                        map.put("${it.PrivatePort}/tcp", new LinkedHashMap<String, Object>())
                        map
                },
                "HostConfig"  : [
                        "Binds"       : mounts.collect {
                            it ->
                                "${it.Source}:${it.Destination}"
                        },
                        "PortBindings": configedPortList.inject(new LinkedHashMap<String, Object>()) {
                            map, it ->
                                map.put("${it.PrivatePort}/tcp", [[
                                                                          "HostPort": "${it.PublicPort}"
                                                                  ]])
                                map
                        }
                ]
        ]
        logger.info(containerConfig)
        def response = dClient.createContainer(containerConfig, [name: dockerName])
        if(response.status.success){
            dClient.startContainer(response.content.Id)
        }else{
            logger.error("create container failed:${dockerName}")
            logger.error(response)
        }


        /**
         * 对于每一个ProjectMeta, 进行部署工作
         */

    }
}
