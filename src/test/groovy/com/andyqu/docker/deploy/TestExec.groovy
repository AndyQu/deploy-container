package com.andyqu.docker.deploy
import de.gesellix.docker.client.DockerClient
import de.gesellix.docker.client.DockerClientImpl
import org.testng.annotations.Test

class TestExec {
    @Test
    def void run(){
        /*
        DockerClient client = new DockerClientImpl()
        def response = client.exec('d470e897df9d',['/bin/bash','/scripts/deploy_qgc.sh'])
        println response.stream.text
        println response.stream
        */
    }
}
