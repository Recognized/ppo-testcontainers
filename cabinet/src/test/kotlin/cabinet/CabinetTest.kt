package cabinet

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.testcontainers.containers.GenericContainer
import org.testcontainers.images.builder.ImageFromDockerfile
import java.nio.file.Paths


class KGenericContainer(imageName: String) : GenericContainer<KGenericContainer>(imageName)


class CabinetTest {

    @get:Rule
    val container = KGenericContainer(
        ImageFromDockerfile()
            .withDockerfileFromBuilder {
                it.from("openjdk:8-jre-slim")
                it.expose(8080)
                it.run("mkdir /app")
                it.copy(
                    Paths.get("stonks", "build", "libs", "stonks.jar").toAbsolutePath().toString(),
                    "/app/app.jar"
                )
                it.entryPoint("java -Dport=8080 -jar /app/app.jar")
            }
            .get()
    )

    @Test
    fun `test x`() {
        println(container.isRunning)
    }
}