package cabinet

interface AdminCabinet {

    fun createUser(): Int

    fun addMoney(user: Int, money: Double)

    suspend fun createCompany(company: String, price: Double, amount: Double)

    suspend fun updatePrice(company: String, price: Double)
}