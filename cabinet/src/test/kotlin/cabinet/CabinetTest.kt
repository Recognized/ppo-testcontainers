package cabinet

import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy
import org.testcontainers.containers.wait.strategy.WaitStrategy
import org.testcontainers.images.builder.ImageFromDockerfile
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNull


class KGenericContainer(imageName: String) : GenericContainer<KGenericContainer>(imageName)


class CabinetTest {

    @get:Rule
    val container: KGenericContainer = run {
        val file = File("../stonks/build/libs/stonks.jar")
        println(file.absolutePath)
        KGenericContainer(
            ImageFromDockerfile()
                .withFileFromFile(
                    "stonks.jar",
                    file,
                    777
                )
                .withDockerfileFromBuilder {
                    it.from("openjdk:8-jre-slim")
                        .expose(8080)
                        .copy("stonks.jar", "/stonks.jar")
                        .entryPoint("java", "-jar", "/stonks.jar")
                }
                .get()

        ).withLogConsumer {
            println(it.utf8String)
        }.waitingFor(
            HttpWaitStrategy().forPort(8080).forPath("/").forStatusCode(
                HttpStatusCode.OK.value
            )
        )
    }

    @Test
    fun `integration test`() {
        runBlocking {
            Cabinet("http://${container.containerIpAddress}:${container.getMappedPort(8080)}").use { cabinet ->
                val admin = cabinet.adminCabinet()

                val user1 = cabinet.userCabinet(admin.createUser())
                val user2 = cabinet.userCabinet(admin.createUser())

                admin.createCompany("Apple", 10.0, 100.0)
                admin.createCompany("Google", 20.0, 50.0)

                assertFails("Company is already created") {
                    admin.createCompany("Google", 20.0, 50.0)
                }

                admin.addMoney(user1.id, 20.0)
                admin.addMoney(user2.id, 30.0)

                user1.buy("Apple", 1.0)
                user1.buy("Google", 0.5)

                assertEquals(20.0, user1.netWorth())

                assertFails("Not enough money") {
                    user2.buy("Google", 20.0)
                }

                admin.addMoney(user2.id, 1000000.0)

                assertFails("Not enough stocks (Google)") {
                    user2.buy("Google", 1000.0)
                }

                assertEquals(20.0, user1.netWorth())

                admin.updatePrice("Google", 30.0)

                assertEquals(25.0, user1.netWorth())

                user1.sell("Google", 0.5)

                // Still have free money
                assertEquals(25.0, user1.netWorth())

                assertEquals(1.0, user1.info().find { it.company == "Apple" }?.amount)
                assertEquals(0.0, user1.info().find { it.company == "Google" }?.amount)
            }
        }
    }
}