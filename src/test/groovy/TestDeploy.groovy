import com.sankuai.srq.deploy.DeployEngine
import com.sankuai.srq.deploy.PortMeta
import com.sankuai.srq.deploy.ProjectMeta
import org.testng.annotations.BeforeTest
import org.testng.annotations.Test

class TestDeploy {
    ProjectMeta queueMeta

    @BeforeTest
    def void setup() {
        queueMeta = new ProjectMeta(
                projectName: "qgc",
                gitRepoUri: "ssh://git@git.sankuai.com/srt/srqserver.git",
                gitbranchName: "master",

                portList:[
                        ["port":8088,"description":"http jetty port"] as PortMeta
                ],
                needJavaDebugPort: true,
                logFolder: "/opt/logs/srq/",
                needMountNodeLib: true,
                needMountGradleLib: true,

                deployScriptFile:"/tmp/queue.sh",
        )
    }

    @Test
    def void deploy(){
        DeployEngine engine = new DeployEngine()
        engine.deploy("qgc", [queueMeta])
    }
}
