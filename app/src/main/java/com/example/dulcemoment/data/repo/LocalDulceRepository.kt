
package com.example.dulcemoment.data.repo

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.dulcemoment.data.local.OrderEntity
import com.example.dulcemoment.data.local.OrderItemEntity
import com.example.dulcemoment.data.local.OrderWithDetails
import com.example.dulcemoment.data.local.ProductEntity
import com.example.dulcemoment.data.local.ProductOptionEntity
import com.example.dulcemoment.data.local.ProductWithOptions
import com.example.dulcemoment.data.local.PushAlertEntity
import com.example.dulcemoment.data.local.TrackingEventEntity
import com.example.dulcemoment.data.local.UserEntity
import com.example.dulcemoment.data.network.ApiService
import com.example.dulcemoment.data.network.AuthRequest
import com.example.dulcemoment.data.network.CardPaymentRequest
import com.example.dulcemoment.data.network.CloudinaryUploadRequest
import com.example.dulcemoment.data.network.CreateOrderRequest
import com.example.dulcemoment.data.network.CreateProductRequest
import com.example.dulcemoment.data.network.LogoutRequest
import com.example.dulcemoment.data.network.OrderItemRequest
import com.example.dulcemoment.data.network.RetrofitClient
import com.example.dulcemoment.data.network.RefreshTokenRequest
import com.example.dulcemoment.data.network.UpdateOrderStatusRequest
import com.example.dulcemoment.data.network.UpdateProductRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException

@Singleton
class LocalDulceRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: ApiService = RetrofitClient.api,
) : CakeRepository {
    private val sessionStore = SessionStore(context.applicationContext)
    private val sessionState = MutableStateFlow<UserEntity?>(sessionStore.loadUser())
    private val productsState = MutableStateFlow<List<ProductWithOptions>>(emptyList())
    private val stockState = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    private val ordersState = MutableStateFlow<List<OrderWithDetails>>(emptyList())
    private val alertsState = MutableStateFlow<List<PushAlertEntity>>(emptyList())

    override fun sessionFlow(): Flow<UserEntity?> = sessionState

    override suspend fun register(name: String, email: String, password: String, role: String): Result<Unit> {
        if (name.isBlank() || email.isBlank() || password.length < 6) {
            return Result.failure(IllegalArgumentException("Datos inválidos. Contraseña mínima 6 caracteres."))
        }
        return runCatching {
            val auth = apiService.register(AuthRequest(name = name, email = email.trim(), password = password, role = role))
            val user = UserEntity(
                id = auth.user.id,
                name = auth.user.name,
                email = auth.user.email,
                password = "",
                role = auth.user.role,
            )
            sessionState.value = user
            sessionStore.saveSession(user, auth.access_token, auth.refresh_token)
            refreshProducts()
            refreshOrders()
        }
    }

    override suspend fun login(email: String, password: String): Result<Unit> {
        return runCatching {
            val auth = apiService.login(AuthRequest(email = email.trim(), password = password))
            val user = UserEntity(
                id = auth.user.id,
                name = auth.user.name,
                email = auth.user.email,
                password = "",
                role = auth.user.role,
            )
            sessionState.value = user
            sessionStore.saveSession(user, auth.access_token, auth.refresh_token)
            refreshProducts()
            refreshOrders()
        }
    }

    override suspend fun bootstrapSession() {
        val user = sessionState.value ?: return
        runCatching {
            apiCallWithRefresh { token -> apiService.me(token) }
            refreshProducts()
            refreshOrders()
            appendAlert(user.id, null, "Sesión activa", "Conectado con API segura")
        }.onFailure {
            logout()
        }
    }

    override suspend fun logout() {
        val token = bearerTokenOrNull()
        val refreshToken = sessionStore.loadRefreshToken()
        if (token != null && !refreshToken.isNullOrBlank()) {
            runCatching {
                apiService.logout(token, LogoutRequest(refresh_token = refreshToken))
            }
        }

        clearLocalSession()
    }

    override suspend fun logoutAllDevices() {
        val token = bearerTokenOrNull()
        if (token != null) {
            runCatching {
                apiService.logoutAll(token)
            }
        }

        clearLocalSession()
    }

    private fun clearLocalSession() {
        sessionState.value = null
        productsState.value = emptyList()
        stockState.value = emptyMap()
        ordersState.value = emptyList()
        alertsState.value = emptyList()
        sessionStore.clear()
    }

    override fun productsFlow(): Flow<List<ProductWithOptions>> = productsState

    override fun stockFlow(): StateFlow<Map<Int, Boolean>> = stockState.asStateFlow()

    override fun customerOrdersFlow(customerId: Int): Flow<List<OrderWithDetails>> = ordersState.map { list ->
        list.filter { it.order.customerId == customerId }
    }

    override fun storeOrdersFlow(): Flow<List<OrderWithDetails>> = ordersState

    override fun alertsFlow(userId: Int): Flow<List<PushAlertEntity>> = alertsState.map { list ->
        list.filter { it.userId == userId }
    }

    override suspend fun addProduct(name: String, description: String, price: Double, stock: Int, imageUrl: String): Result<Unit> {
        if (name.isBlank() || description.isBlank() || price <= 0 || stock < 0) {
            return Result.failure(IllegalArgumentException("Datos de producto inválidos"))
        }
        return runCatching {
            apiCallWithRefresh { token ->
                apiService.createProduct(
                    token,
                    CreateProductRequest(
                        name = name,
                        description = description,
                        base_price = price,
                        stock = stock,
                        image_url = imageUrl.trim(),
                    )
                )
            }
            refreshProducts()
        }
    }

    override suspend fun refreshDashboard(): Result<Unit> {
        return runCatching {
            refreshProducts()
            if (sessionState.value != null) {
                refreshOrders()
            }
        }
    }

    override suspend fun uploadImageToCloudinary(sourceUrl: String): Result<String> {
        if (!sourceUrl.startsWith("http", ignoreCase = true)) {
            return Result.failure(IllegalArgumentException("URL de imagen inválida"))
        }
        return runCatching {
            apiCallWithRefresh { token ->
                apiService.uploadImageToCloudinary(token, CloudinaryUploadRequest(source_url = sourceUrl.trim())).image_url
            }
        }
    }

    override suspend fun uploadImageFileToCloudinary(uri: Uri): Result<String> {
        return runCatching {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri) ?: "image/*"

            val fileName = runCatching {
                contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
                }
            }.getOrNull() ?: "upload_${System.currentTimeMillis()}"

            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw IllegalArgumentException("No se pudo leer la imagen seleccionada")

            val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData(
                name = "file",
                filename = fileName,
                body = requestBody,
            )

            apiCallWithRefresh { token ->
                apiService.uploadImageFileToCloudinary(token, part).image_url
            }
        }
    }

    override suspend fun setOutOfStock(productId: Int): Result<Unit> {
        return runCatching {
            apiCallWithRefresh { token ->
                apiService.updateProduct(
                    token,
                    productId,
                    UpdateProductRequest(stock = 0, is_active = true),
                )
            }
            refreshProducts()
        }
    }

    override suspend fun restockProduct(productId: Int, unitsToAdd: Int): Result<Unit> {
        if (unitsToAdd <= 0) {
            return Result.failure(IllegalArgumentException("La reposición debe ser mayor a 0"))
        }
        val product = productsState.value.firstOrNull { it.product.id == productId }?.product
            ?: return Result.failure(IllegalArgumentException("Producto no encontrado"))

        val updatedStock = product.stock + unitsToAdd
        return runCatching {
            apiCallWithRefresh { token ->
                apiService.updateProduct(
                    token,
                    productId,
                    UpdateProductRequest(stock = updatedStock, is_active = true),
                )
            }
            refreshProducts()
        }
    }

    override suspend fun toggleProductActive(productId: Int, isActive: Boolean): Result<Unit> {
        return runCatching {
            apiCallWithRefresh { token ->
                apiService.updateProduct(
                    token,
                    productId,
                    UpdateProductRequest(is_active = isActive),
                )
            }
            refreshProducts()
        }
    }

    override suspend fun setProductStockState(productId: Int, isOutOfStock: Boolean): Result<Unit> {
        val product = productsState.value.firstOrNull { it.product.id == productId }?.product
            ?: return Result.failure(IllegalArgumentException("Producto no encontrado"))

        val targetStock = if (isOutOfStock) 0 else maxOf(product.stock, 1)
        applyLocalStock(productId = productId, isInStock = targetStock > 0)

        return runCatching {
            apiCallWithRefresh { token ->
                apiService.updateProduct(
                    token,
                    productId,
                    UpdateProductRequest(stock = targetStock, is_active = product.isActive),
                )
            }
            refreshProducts()
        }.onFailure {
            refreshProducts()
        }
    }

    override suspend fun createOrder(
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
    ): Result<Int> {
        if (quantity <= 0) return Result.failure(IllegalArgumentException("Cantidad inválida"))
        if (address.isBlank()) return Result.failure(IllegalArgumentException("La dirección es obligatoria"))

        return runCatching {
            val response = apiCallWithRefresh { token ->
                apiService.createOrder(
                    token,
                    CreateOrderRequest(
                        customer_id = customerId,
                        delivery_address = address,
                        notes = notes,
                        items = listOf(
                            OrderItemRequest(
                                product_id = productId,
                                quantity = quantity,
                                custom_ingredients = ingredients,
                                custom_size = size,
                                custom_shape = shape,
                                custom_flavor = flavor,
                                custom_color = color,
                            )
                        )
                    )
                )
            }
            refreshProducts()
            refreshOrders()
            appendAlert(customerId, response.id, "Pedido creado", "Tu pedido fue recibido en la tienda")
            response.id
        }
    }

    override suspend fun updateOrderStatus(orderId: Int, status: String): Result<Unit> {
        val valid = setOf("in_oven", "decorating", "on_the_way", "delivered")
        if (status !in valid) return Result.failure(IllegalArgumentException("Estado no válido"))

        return runCatching {
            val message = when (status) {
                "in_oven" -> "Tu pastel entró al horno"
                "decorating" -> "Estamos decorando tu pastel"
                "on_the_way" -> "¡Tu pedido va en camino!"
                else -> "Entregado"
            }
            val eta = when (status) {
                "in_oven" -> 60
                "decorating" -> 35
                "on_the_way" -> 20
                else -> 0
            }
            val updated = apiCallWithRefresh { token ->
                apiService.updateOrderStatus(
                    token,
                    orderId,
                    UpdateOrderStatusRequest(
                        status = status,
                        message = message,
                        eta_minutes = eta,
                    )
                )
            }
            refreshOrders()
            appendAlert(updated.customer_id, updated.id, "Actualización de pedido", message)
        }
    }

    override suspend fun payOrder(orderId: Int, cardNumber: String, cardName: String): Result<String> {
        if (cardNumber.filter { it.isDigit() }.length < 13 || cardName.isBlank()) {
            return Result.failure(IllegalArgumentException("Tarjeta inválida"))
        }

        return runCatching {
            apiCallWithRefresh { token ->
                apiService.payCard(
                    token,
                    CardPaymentRequest(
                        order_id = orderId,
                        card_number = cardNumber,
                        holder_name = cardName,
                    )
                )
            }
            val last4 = cardNumber.filter { it.isDigit() }.takeLast(4)
            "Pago aprobado con tarjeta terminación $last4"
        }
    }

    override suspend fun currentUserOnce(): UserEntity? = sessionState.value

    private suspend fun refreshProducts() {
        val products = apiService.products().map { product ->
            ProductWithOptions(
                product = ProductEntity(
                    id = product.id,
                    name = product.name,
                    description = product.description,
                    basePrice = product.base_price,
                    stock = product.stock,
                    imageUrl = product.image_url,
                    isActive = product.is_active,
                ),
                options = product.options.map { option ->
                    ProductOptionEntity(
                        id = option.id,
                        productId = product.id,
                        category = option.category,
                        value = option.value,
                        priceDelta = option.price_delta,
                    )
                }
            )
        }
        productsState.value = products
        stockState.value = products.associate { it.product.id to (it.product.stock > 0 && it.product.isActive) }
    }

    private suspend fun refreshOrders() {
        val user = sessionState.value
        val remoteOrders = apiCallWithRefresh { token ->
            if (user?.role == "customer") {
                apiService.orders(authHeader = token, customerId = user.id)
            } else {
                apiService.orders(authHeader = token)
            }
        }

        ordersState.value = remoteOrders.map { order ->
            OrderWithDetails(
                order = OrderEntity(
                    id = order.id,
                    customerId = order.customer_id,
                    status = order.status,
                    total = order.total,
                    deliveryAddress = order.delivery_address,
                    notes = order.notes,
                    createdAt = System.currentTimeMillis(),
                ),
                items = order.items.map { item ->
                    OrderItemEntity(
                        id = item.id,
                        orderId = order.id,
                        productId = item.product_id,
                        quantity = item.quantity,
                        unitPrice = item.unit_price,
                        ingredients = item.custom_ingredients,
                        size = item.custom_size,
                        shape = item.custom_shape,
                        flavor = item.custom_flavor,
                        color = item.custom_color,
                    )
                },
                events = order.events.map { event ->
                    TrackingEventEntity(
                        id = event.id,
                        orderId = order.id,
                        status = event.status,
                        message = event.message,
                        etaMinutes = event.eta_minutes,
                        createdAt = System.currentTimeMillis(),
                    )
                }
            )
        }
    }

    private fun appendAlert(userId: Int, orderId: Int? = null, title: String, body: String) {
        alertsState.update {
            listOf(
                PushAlertEntity(
                    id = (it.firstOrNull()?.id ?: 0) + 1,
                    userId = userId,
                    orderId = orderId,
                    title = title,
                    body = body,
                    createdAt = System.currentTimeMillis(),
                )
            ) + it
        }
    }

    private fun bearerTokenOrNull(): String? {
        val token = sessionStore.loadToken() ?: return null
        return "Bearer $token"
    }

    private suspend fun refreshAccessTokenIfPossible(): Boolean {
        val refreshToken = sessionStore.loadRefreshToken() ?: return false
        val response = runCatching {
            apiService.refresh(RefreshTokenRequest(refresh_token = refreshToken))
        }.getOrNull() ?: return false

        val user = sessionState.value ?: return false
        sessionStore.updateTokens(response.access_token, response.refresh_token)
        sessionState.value = user
        return true
    }

    private suspend fun <T> apiCallWithRefresh(block: suspend (String) -> T): T {
        val token = bearerTokenOrNull() ?: throw IllegalStateException("Sesión no iniciada")
        return try {
            block(token)
        } catch (error: HttpException) {
            if (error.code() == 401 && refreshAccessTokenIfPossible()) {
                val newToken = bearerTokenOrNull() ?: throw error
                block(newToken)
            } else {
                throw error
            }
        }
    }

    private fun applyLocalStock(productId: Int, isInStock: Boolean) {
        stockState.update { current -> current + (productId to isInStock) }
        productsState.update { list ->
            list.map { productWithOptions ->
                if (productWithOptions.product.id != productId) return@map productWithOptions
                productWithOptions.copy(
                    product = productWithOptions.product.copy(stock = if (isInStock) maxOf(productWithOptions.product.stock, 1) else 0)
                )
            }
        }
    }
}
