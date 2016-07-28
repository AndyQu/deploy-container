import com.andyqu.docker.deploy.DockerTool
import com.andyqu.docker.deploy.Tool
import de.gesellix.docker.client.DockerClient
import de.gesellix.docker.client.DockerClientImpl
import groovy.json.JsonBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testng.Assert
import org.testng.annotations.AfterTest
import org.testng.annotations.BeforeTest
import org.testng.annotations.Test

class TestExtendDockerClientImpl {
    def static final Logger logger = LoggerFactory.getLogger(TestExtendDockerClientImpl)
    def static final containerName = "qgc-unit-test"
    DockerClient dClient
    def containerId
    @BeforeTest
    def void setUp(){
        Tool.extendSlf4j()
        DockerTool.extendDockerClientImpl()
        dClient = new DockerClientImpl()
        def containerConfig = ["Cmd"   : [],
                               "Image" : "busybox:latest",
                               ]
        try {
            def response = dClient.createContainer(containerConfig, [name: containerName])
            if (response.status.success) {
                containerId = response.content.Id
                response = dClient.startContainer(containerId)
                if (response.status.success) {
                    return
                }
            }
        }catch (Exception e){
            def container=dClient.queryContainerName(containerName)
            if(container!=null){
                containerId=container.Id
                logger.info("container:${containerName} already exists")
                return
            }
        }
        Assert.fail("create/start test container")
    }

    @Test
    def void queryContainerName(){
        logger.info dClient.queryContainerName(containerName)
    }

    @AfterTest
    def void stopAndRemove(){
        logger.info new JsonBuilder( dClient.stopAndRemoveContainer(containerId)).toPrettyString()
    }
}
