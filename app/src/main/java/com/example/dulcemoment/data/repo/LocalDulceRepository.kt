
package com.example.dulcemoment.data.repo

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.dulcemoment.BuildConfig
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
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
        val normalizedUrl = sourceUrl.trim()
        return runCatching {
            withTransientRetry {
                apiCallWithRefresh { token ->
                    apiService.uploadImageToCloudinary(token, CloudinaryUploadRequest(source_url = normalizedUrl)).image_url
                }
            }
        }.recoverCatching { throwable ->
            if (throwable is HttpException && throwable.code() in setOf(502, 503, 504)) {
                normalizedUrl
            } else {
                throw throwable
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

            withTransientRetry {
                apiCallWithRefresh { token ->
                    apiService.uploadImageFileToCloudinary(token, part).image_url
                }
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

    override suspend fun payOrder(
        orderId: Int,
        cardNumber: String,
        cardName: String,
        securityCode: String,
        expiry: String,
    ): Result<String> {
        val provider = BuildConfig.PAYMENT_PROVIDER.lowercase()

        if (provider == "stripe" && BuildConfig.STRIPE_PUBLISHABLE_KEY.isBlank()) {
            return Result.failure(
                IllegalStateException("Falta STRIPE_PUBLISHABLE_KEY. Configúrala en propiedades de Gradle para habilitar pagos.")
            )
        }

        if ((provider == "mercadopago" || provider == "mercado_pago") && BuildConfig.MERCADOPAGO_PUBLIC_KEY.isBlank()) {
            return Result.failure(
                IllegalStateException("Falta MERCADOPAGO_PUBLIC_KEY. Configúrala en propiedades de Gradle para habilitar pagos.")
            )
        }

        val digits = cardNumber.filter { it.isDigit() }
        if (digits.length !in 13..19 || cardName.isBlank() || !isValidCardNumber(digits)) {
            return Result.failure(IllegalArgumentException("Tarjeta inválida"))
        }
        val cvv = securityCode.filter { it.isDigit() }
        if (cvv.length !in 3..4) {
            return Result.failure(IllegalArgumentException("CVV inválido"))
        }

        val expiryDigits = expiry.filter { it.isDigit() }
        if (expiryDigits.length != 4) {
            return Result.failure(IllegalArgumentException("Fecha de expiración inválida"))
        }
        val month = expiryDigits.take(2).toIntOrNull() ?: 0
        val yearTwoDigits = expiryDigits.takeLast(2).toIntOrNull() ?: -1
        if (month !in 1..12 || yearTwoDigits !in 0..99) {
            return Result.failure(IllegalArgumentException("Fecha de expiración inválida"))
        }
        val year = 2000 + yearTwoDigits

        return runCatching {
            val response = apiCallWithRefresh { token ->
                apiService.payCard(
                    token,
                    provider,
                    CardPaymentRequest(
                        order_id = orderId,
                        card_number = cardNumber,
                        holder_name = cardName,
                        security_code = cvv,
                        expiry_month = month,
                        expiry_year = year,
                    )
                )
            }

            val approvedByFlag = when (val raw = response["approved"] ?: response["success"] ?: response["ok"]) {
                is Boolean -> raw
                is Number -> raw.toInt() != 0
                is String -> raw.equals("true", ignoreCase = true) || raw == "1"
                else -> false
            }
            val status = (response["status"]?.toString() ?: "").lowercase()
            val approvedByStatus = status in setOf("approved", "paid", "success", "succeeded")
            val message = response["message"]?.toString()?.takeIf { it.isNotBlank() }
            val transactionId = response["transaction_id"]?.toString()?.takeIf { it.isNotBlank() }

            if (!approvedByFlag && !approvedByStatus) {
                throw IllegalStateException(message ?: "Pago rechazado por la pasarela")
            }

            refreshOrders()
            val last4 = digits.takeLast(4)
            when {
                !transactionId.isNullOrBlank() -> "Pago aprobado • TX: $transactionId • ****$last4"
                !message.isNullOrBlank() -> "$message • ****$last4"
                else -> "Pago aprobado • ****$last4"
            }
        }
    }

    override suspend fun paymentDiagnostics(orderId: Int): Result<String> {
        return runCatching {
            val diagnostics = apiCallWithRefresh { token -> apiService.paymentDiagnostics(token, orderId) }
            val provider = diagnostics["provider"]?.toString().orEmpty()
            val status = diagnostics["payment_status"]?.toString().orEmpty()
            val amount = diagnostics["amount"]?.toString().orEmpty()
            val gatewayObj = diagnostics["gateway"] as? Map<*, *>
            val statusDetail = gatewayObj?.get("status_detail")?.toString().orEmpty()
            val issuerId = gatewayObj?.get("issuer_id")?.toString().orEmpty()
            val first6 = gatewayObj?.get("first_six_digits")?.toString().orEmpty()
            val last4 = gatewayObj?.get("last_four_digits")?.toString().orEmpty()
            val recommendation = recommendationFromStatusDetail(statusDetail)
            buildString {
                append("Diagnóstico de pago\n")
                append("Proveedor: ").append(if (provider.isBlank()) "N/D" else provider).append("\n")
                append("Estado: ").append(if (status.isBlank()) "N/D" else status).append("\n")
                append("Monto: ").append(if (amount.isBlank()) "N/D" else amount).append("\n")
                if (statusDetail.isNotBlank()) append("Detalle pasarela: ").append(statusDetail).append("\n")
                if (issuerId.isNotBlank()) append("Issuer ID: ").append(issuerId).append("\n")
                if (first6.isNotBlank() || last4.isNotBlank()) append("BIN: ").append(first6).append("****").append(last4).append("\n")
                if (recommendation.isNotBlank()) append("Recomendación: ").append(recommendation).append("\n")
            }.trim()
        }.recoverCatching { error ->
            throw IllegalStateException(mapErrorToUserMessage(error, "No se pudo consultar el diagnóstico de pago"))
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

    private suspend fun <T> withTransientRetry(
        attempts: Int = 3,
        initialDelayMs: Long = 700,
        block: suspend () -> T,
    ): T {
        var currentDelayMs = initialDelayMs
        repeat(attempts - 1) {
            try {
                return block()
            } catch (error: HttpException) {
                if (error.code() !in setOf(502, 503, 504)) throw error
            }
            delay(currentDelayMs)
            currentDelayMs *= 2
        }
        return block()
    }

    private fun isValidCardNumber(digitsOnly: String): Boolean {
        var sum = 0
        var alternate = false
        for (index in digitsOnly.length - 1 downTo 0) {
            var n = digitsOnly[index].digitToIntOrNull() ?: return false
            if (alternate) {
                n *= 2
                if (n > 9) n -= 9
            }
            sum += n
            alternate = !alternate
        }
        return sum % 10 == 0
    }

    private fun mapErrorToUserMessage(error: Throwable, fallback: String): String {
        if (error !is HttpException) {
            val text = error.message.orEmpty()
            return if (text.isBlank()) fallback else text
        }

        val body = runCatching { error.response()?.errorBody()?.string().orEmpty() }.getOrDefault("")
        if (body.isNotBlank()) {
            runCatching {
                val json = JSONObject(body)
                val detail = json.opt("detail")
                when (detail) {
                    is String -> detail
                    is JSONObject -> detail.optString("message", fallback)
                    else -> fallback
                }
            }.getOrNull()?.let { parsed ->
                if (parsed.isNotBlank()) return parsed
            }
        }

        return when (error.code()) {
            400 -> "La solicitud contiene datos inválidos."
            401 -> "Tu sesión expiró. Inicia sesión nuevamente."
            403 -> "No tienes permisos para esta operación."
            404 -> "No se encontró la información solicitada."
            409 -> "La operación no puede completarse por conflicto de estado."
            in 500..599 -> "El servidor no está disponible por ahora. Intenta más tarde."
            else -> fallback
        }
    }

    private fun recommendationFromStatusDetail(statusDetail: String): String {
        return when (statusDetail.lowercase()) {
            "cc_rejected_insufficient_amount" -> "Verifica saldo disponible o utiliza otra tarjeta."
            "cc_rejected_bad_filled_security_code" -> "Revisa el CVV y vuelve a intentarlo."
            "cc_rejected_bad_filled_date" -> "Verifica la fecha de vencimiento (MM/YY)."
            "cc_rejected_high_risk" -> "Intenta con otra tarjeta o con la app del banco habilitada."
            "cc_rejected_call_for_authorize" -> "Autoriza la compra en la app de tu banco y vuelve a intentar."
            "cc_rejected_duplicated_payment" -> "No repitas el cobro: confirma primero en movimientos de la tarjeta."
            "cc_rejected_other_reason" -> "Prueba otro método de pago o contacta a tu banco."
            else -> ""
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
