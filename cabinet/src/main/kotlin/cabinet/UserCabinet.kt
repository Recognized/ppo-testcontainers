package cabinet


interface UserCabinet {
    fun createUser(): Int
    suspend fun buy(company: String, amount: Double)
    suspend fun sell(company: String, amount: Double)
    suspend fun info(): List<StonksInfo>
    suspend fun netWorth(): Double
}