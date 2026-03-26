package com.example.dulcemoment.data.repo

import com.example.dulcemoment.data.local.OrderWithDetails
import com.example.dulcemoment.data.local.ProductWithOptions
import com.example.dulcemoment.data.local.PushAlertEntity
import com.example.dulcemoment.data.local.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

data class AdminOrderSummary(
    val period: String,
    val totalOrders: Int,
    val totalSales: Double,
    val statusBreakdown: List<Pair<String, Int>>,
)

interface CakeRepository {
    fun sessionFlow(): Flow<UserEntity?>
    fun productsFlow(): Flow<List<ProductWithOptions>>
    fun stockFlow(): StateFlow<Map<Int, Boolean>>
    fun customerOrdersFlow(customerId: Int): Flow<List<OrderWithDetails>>
    fun storeOrdersFlow(): Flow<List<OrderWithDetails>>
    fun alertsFlow(userId: Int): Flow<List<PushAlertEntity>>

    suspend fun register(name: String, email: String, password: String, role: String): Result<Unit>
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun bootstrapSession()
    suspend fun logout()
    suspend fun logoutAllDevices()

    suspend fun addProduct(name: String, description: String, price: Double, stock: Int, imageUrl: String): Result<Unit>
    suspend fun updateProduct(productId: Int, name: String, description: String, price: Double, stock: Int): Result<Unit>
    suspend fun deleteProduct(productId: Int): Result<String>
    suspend fun ordersSummary(period: String): Result<AdminOrderSummary>
    suspend fun refreshDashboard(): Result<Unit>
    suspend fun uploadImageToCloudinary(sourceUrl: String): Result<String>
    suspend fun uploadImageFileToCloudinary(uri: android.net.Uri): Result<String>
    suspend fun setOutOfStock(productId: Int): Result<Unit>
    suspend fun restockProduct(productId: Int, unitsToAdd: Int): Result<Unit>
    suspend fun toggleProductActive(productId: Int, isActive: Boolean): Result<Unit>
    suspend fun setProductStockState(productId: Int, isOutOfStock: Boolean): Result<Unit>

    suspend fun createOrder(
        customerId: Int,
        productId: Int,
        quantity: Int,
        ingredients: String,
        size: String,
        shape: String,
        flavor: String,
        color: String,
        address: String,
        notes: String,
    ): Result<Int>

    suspend fun updateOrderStatus(orderId: Int, status: String): Result<Unit>
    suspend fun payOrder(
        orderId: Int,
        cardNumber: String,
        cardName: String,
        securityCode: String,
        expiry: String,
    ): Result<String>
    suspend fun paymentDiagnostics(orderId: Int): Result<String>
    suspend fun currentUserOnce(): UserEntity?
}
