import de.gesellix.docker.client.DockerClient
import de.gesellix.docker.client.DockerClientImpl
import de.gesellix.docker.client.DockerResponse
import groovy.json.JsonBuilder
import org.testng.annotations.Test

class PsAll {
    @Test
    def void run(){
        DockerClient dClient = new DockerClientImpl()
        DockerResponse response = dClient.ps(query: [all: true, size: true])
        response.content.each {
            println new JsonBuilder(it).toPrettyString()
            def container = dClient.inspectContainer(it.Id)
            println new JsonBuilder(container).toPrettyString()
        }
    }
}
