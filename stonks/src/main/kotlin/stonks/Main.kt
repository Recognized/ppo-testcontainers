@file:JvmName("MainKt")

package stonks

import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.ktor.application.*
import io.ktor.features.BadRequestException
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.pipeline.PipelineContext


fun main() {
    val server = embeddedServer(Netty, port = 8080) {
        configure()
    }
    println("Started")
    server.start(wait = true)
}

class Stonks {
    // company -> amount
    val amount = mutableMapOf<String, Double>()
    // company -> stocks price
    val prices = mutableMapOf<String, Double>()
}

class Stocks(val price: Double, val count: Double)

fun Application.configure() {
    val stonks = Stonks()

    install(ContentNegotiation) {
        jackson {
            registerModule(KotlinModule())
        }
    }

    routing {
        get("/") {
            call.respond(HttpStatusCode.OK)
        }

        get("getPrice") {
            val company = call.request.queryParameters["company"]
            val price = company?.let {
                stonks.prices[it]
            }
            if (price == null) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                val count = stonks.amount[company]!!
                call.respond(Stocks(price = price, count = count))
            }
        }

        post("createCompany") {
            val company = queryParam("company")
            if (company in stonks.amount) {
                throw BadRequestException("Company $company already exists")
            }
            val price = queryParam("price").toDoubleOrNull().positive()
            val count = queryParam("amount").toDoubleOrNull().positive()
            stonks.amount[company] = count
            stonks.prices[company] = price
            call.respond(HttpStatusCode.OK)
        }

        post("buy") {
            val company = queryParam("company").company(stonks)
            val amount = queryParam("amount").toDoubleOrNull().positive()
            if (stonks.amount[company]!! < amount) {
                throw BadRequestException("Company does have much stonks")
            }
            stonks.amount[company] = stonks.amount[company]!! - amount
            call.respond(HttpStatusCode.OK)
        }

        post("sell") {
            val company = queryParam("company").company(stonks)
            val amount = queryParam("amount").toDoubleOrNull().positive()
            stonks.amount[company] = stonks.amount[company]!! + amount
            call.respond(HttpStatusCode.OK)
        }

        post("updatePrice") {
            val newPrice = queryParam("price").toDoubleOrNull().positive()
            val company = queryParam("company").company(stonks)
            stonks.prices[company] = newPrice
            call.respond(HttpStatusCode.OK)
        }
    }
}

fun <T : Number> T?.positive(): T {
    if (this == null) {
        throw BadRequestException("Param is absent")
    }
    if (toDouble() <= 0) {
        throw BadRequestException("Param must be positive")
    }
    return this
}

fun String.company(stonks: Stonks): String {
    if (!stonks.prices.contains(this)) {
        throw BadRequestException("Company $this does not exist")
    }
    return this
}

private fun PipelineContext<*, ApplicationCall>.queryParam(param: String): String {
    return call.request.queryParameters[param] ?: throw BadRequestException("Wrong or missing param: $param")
}