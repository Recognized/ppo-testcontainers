package cabinet

import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import java.io.Closeable
import kotlin.random.Random

data class UserData(
    // Money measured in abstract units
    var money: Double = 0.0,
    // company -> amount of stocks owned
    val stonks: MutableMap<String, Double> = mutableMapOf()
)

data class StonksInfo(val company: String, val amount: Double, val netWorth: Double)

class Stocks(val price: Double, val count: Double)

class Cabinet(private val stocksUrl: String) : Closeable {
    val client = HttpClient(OkHttp) {

        install(JsonFeature) {
            serializer = JacksonSerializer()
        }

        expectSuccess = true
    }
    private val users = mutableMapOf<Int, UserData>()

    override fun close() {
        client.close()
    }

    fun createUser(money: Double = 0.0, stonks: Map<String, Double> = emptyMap()): Int {
        var id: Int? = null
        while (id == null || id in users) {
            id = Random.nextInt()
        }
        users[id] = UserData(money, stonks.toMutableMap())
        return id
    }

    fun addMoney(user: Int, money: Double) {
        val nextMoney = data(user).money + money
        if (nextMoney < 0) {
            error("Can't take more money from user than they own")
        }
        data(user).money += money
    }

    suspend fun getStonksInfo(user: Int): List<StonksInfo> {
        return data(user).stonks.map { (key, value) ->
            StonksInfo(key, value, value * getStonksInfo(key).price)
        }
    }

    suspend fun getNetWorth(user: Int): Double {
        return data(user).money + getStonksInfo(user).sumByDouble {
            it.netWorth
        }
    }

    suspend fun sellStonks(user: Int, company: String, amount: Double) {
        val currentAmount = data(user).stonks.getOrDefault(company, 0.0)
        if (currentAmount < amount) {
            error("Cannot sell more stocks than owned")
        }
        if (amount < 0) {
            error("Bad request")
        }

        client.post<Unit>("$stocksUrl/sell") {
            parameter("company", company)
            parameter("amount", amount)
        }

        data(user).money += getStonksInfo(company).price * amount
        data(user).stonks[company] = data(user).stonks[company]!! - amount
    }

    suspend fun buyStonks(user: Int, company: String, amount: Double) {
        val stockAmount = getStonksInfo(company).count
        if (amount > stockAmount) {
            error("Not enough stocks!")
        }
        if (amount < 0) {
            error("Cannot buy < 0")
        }
        val price = getStonksInfo(company).price
        if (price * amount > data(user).money) {
            error("Not enough money to buy stonks")
        }

        client.post<Unit>("$stocksUrl/buy") {
            parameter("company", company)
            parameter("amount", amount)
        }

        data(user).money -= price * amount
        data(user).stonks[company] = (data(user).stonks[company] ?: 0.0) + amount
    }

    fun userCabinet(user: Int): UserCabinet {
        return object : UserCabinet {
            override fun createUser(): Int {
                return this@Cabinet.createUser()
            }

            override val id: Int = user

            override suspend fun buy(company: String, amount: Double) {
                buyStonks(user, company, amount)
            }

            override suspend fun sell(company: String, amount: Double) {
                sellStonks(user, company, amount)
            }

            override suspend fun info(): List<StonksInfo> {
                return getStonksInfo(user)
            }

            override suspend fun netWorth(): Double {
                return getNetWorth(user)
            }
        }
    }

    fun adminCabinet(): AdminCabinet {
        return object : AdminCabinet {
            override fun createUser(): Int {
                return this@Cabinet.createUser()
            }

            override fun addMoney(user: Int, money: Double) {
                this@Cabinet.addMoney(user, money)
            }

            override suspend fun createCompany(company: String, price: Double, amount: Double) {
                client.post<String>("$stocksUrl/createCompany") {
                    parameter("company", company)
                    parameter("price", price)
                    parameter("amount", amount)
                }
            }

            override suspend fun updatePrice(company: String, price: Double) {
                client.post<Unit>("$stocksUrl/updatePrice") {
                    parameter("company", company)
                    parameter("price", price)
                }
            }
        }
    }

    private suspend fun getStonksInfo(company: String): Stocks {
        return client.get("$stocksUrl/getPrice") {
            parameter("company", company)
        }
    }

    private fun data(user: Int): UserData {
        return users[user] ?: error("User not found id=$user")
    }
}