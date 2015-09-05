package com.sankuai.srq.deploy

import de.gesellix.docker.client.DockerClientImpl
import de.gesellix.docker.client.DockerResponse
import groovy.json.JsonBuilder
import org.slf4j.Logger

import java.security.MessageDigest

/**
 * Created by andy on 15/9/4.
 */
class Tool {
    def static generateMD5(String s) {
        MessageDigest digest = MessageDigest.getInstance("MD5")
        digest.update(s.bytes);
        new BigInteger(1, digest.digest()).toString(16).padLeft(32, '0')
    }

    def static boolean isPortInUse(String hostName, int portNumber) {
        boolean result;
        try {
            Socket s = new Socket(hostName, portNumber);
            s.close();
            true
        } catch (Exception e) {
            false
        }
    }
    def static void extendLog4j(){
        Logger.metaClass.trace={
            msgObj->
                trace(new JsonBuilder(msgObj).toPrettyString())
        }
        Logger.metaClass.debug={
            msgObj->
                debug(new JsonBuilder(msgObj).toPrettyString())
        }
        Logger.metaClass.info={
            msgObj->
                info(new JsonBuilder(msgObj).toPrettyString())
        }
        Logger.metaClass.warn={
            msgObj->
                warn(new JsonBuilder(msgObj).toPrettyString())
        }
        Logger.metaClass.error={
            msgObj->
                error(new JsonBuilder(msgObj).toPrettyString())
        }
    }
}

class DockerTool {
    def static void extendDockerClientImpl(){
        addQueryContainerName()
        addStopAndRemoveContainer()
    }
    def static void addQueryContainerName() {
        DockerClientImpl.metaClass.queryContainerName = {
            containerName ->
                DockerResponse response = ps(query: [all: true, size: true])
                def foundContainer = null
                if (response.status.success) {
                    response.content.find {
                        container ->
                            container.Names.find {
                                name ->
                                    if (name.contains(containerName)) {
                                        foundContainer = container
                                        true
                                    } else {
                                        false
                                    }
                            }
                    }
                }
                foundContainer
        }
    }

    def static void addStopAndRemoveContainer() {
        DockerClientImpl.metaClass.stopAndRemoveContainer = {
            containerId ->
                DockerResponse response = stop(containerId)
                if (response.status.success || response.status.code == 304) {
                    response = rm(containerId)
                    if (response.status.success) {
                        logger.info("remove ${containerId} successfully")
                    }else{
                        logger.error("remove ${containerId} failed")
                        logger.error(response)
                    }
                }else{
                    logger.error("stop ${containerId} failed")
                    logger.error(response)
                }
        }
    }
}
