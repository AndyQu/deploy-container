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
                Name: "qgc",
                GitRepoUri: "ssh://git@git.sankuai.com/srt/srqserver.git",
                GitbranchName: "master",
                PortList:[
                        ["Port":8080,"Description":"http jetty port"] as PortMeta
                ],
                NeedJavaDebugPort: true,
                LogFolder : "/opt/logs/srq/",
                NeedMountNodeLib: true,
                NeedMountGradleLib: true,

                DeployScriptFile:"/tmp/queue.sh",
        )

    }

    @Test
    def void deploy(){
        DeployEngine engine = new DeployEngine('http://172.27.2.94:4243')
        engine.deploy("qgc", [queueMeta])
    }
}
