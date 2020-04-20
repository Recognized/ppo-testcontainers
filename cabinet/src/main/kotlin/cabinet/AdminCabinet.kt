package cabinet

interface AdminCabinet {

    suspend fun createCompany(company: String, price: Double, amount: Double)

    suspend fun updatePrice(company: String, price: Double)
}