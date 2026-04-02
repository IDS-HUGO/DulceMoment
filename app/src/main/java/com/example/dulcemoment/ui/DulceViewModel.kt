package com.example.dulcemoment.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dulcemoment.data.local.OrderWithDetails
import com.example.dulcemoment.data.local.ProductWithOptions
import com.example.dulcemoment.data.local.PushAlertEntity
import com.example.dulcemoment.data.local.UserEntity
import com.example.dulcemoment.data.repo.AdminOrderSummary
import com.example.dulcemoment.data.repo.CakeRepository
import com.example.dulcemoment.ui.state.UiErrorType
import com.example.dulcemoment.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class DulceUiState(
    val currentUser: UserEntity? = null,
    val products: List<ProductWithOptions> = emptyList(),
    val orders: List<OrderWithDetails> = emptyList(),
    val alerts: List<PushAlertEntity> = emptyList(),
    val loading: Boolean = false,
    val message: String = "",
    val error: String = "",
    val suggestedImageUrl: String = "",
    val inAppAlert: PushAlertEntity? = null,
    val adminSummary: AdminOrderSummary? = null,
    val storeName: String = "DulceMoment",
    val storeEmail: String = "",
    val screenState: UiState<Unit> = UiState.Success(Unit),
)

@HiltViewModel
class DulceViewModel @Inject constructor(
    private val repository: CakeRepository,
    @ApplicationContext applicationContext: android.content.Context,
) : ViewModel() {
    private val notifier = DulceNotifier(applicationContext)

    private val _uiState = MutableStateFlow(DulceUiState())
    val uiState: StateFlow<DulceUiState> = _uiState.asStateFlow()
    val stockState: StateFlow<Map<Int, Boolean>> = repository.stockFlow()

    private var ordersJob: Job? = null
    private var alertsJob: Job? = null
    private var pollJob: Job? = null
    private val seenAlertIds = mutableSetOf<Int>()
    private val lastKnownOrderStatus = mutableMapOf<Int, String>()
    private var alertsInitialized = false

    init {
        observeSession()
        observeProducts()
        viewModelScope.launch {
            repository.bootstrapSession()
        }
    }

    private fun observeSession() {
        viewModelScope.launch {
            repository.sessionFlow().collect { user ->
                _uiState.update { it.copy(currentUser = user) }
                subscribeRoleData(user)
            }
        }
    }

    private fun observeProducts() {
        viewModelScope.launch {
            repository.productsFlow().collect { products ->
                _uiState.update { it.copy(products = products) }
            }
        }
    }

    private fun subscribeRoleData(user: UserEntity?) {
        ordersJob?.cancel()
        alertsJob?.cancel()
        pollJob?.cancel()

        if (user == null) {
            alertsInitialized = false
            seenAlertIds.clear()
            lastKnownOrderStatus.clear()
            _uiState.update { it.copy(orders = emptyList(), alerts = emptyList(), storeName = "DulceMoment", storeEmail = "") }
            return
        }

        ordersJob = viewModelScope.launch {
            val flow = if (user.role == "store") repository.storeOrdersFlow() else repository.customerOrdersFlow(user.id)
            flow.collect { orders ->
                _uiState.update { it.copy(orders = orders) }

                if (user.role == "customer") {
                    orders.forEach { orderDetail ->
                        val orderId = orderDetail.order.id
                        val newStatus = orderDetail.order.status
                        val previousStatus = lastKnownOrderStatus[orderId]
                        if (previousStatus != null && previousStatus != newStatus) {
                            val message = statusMessage(newStatus)
                            val alert = PushAlertEntity(
                                id = System.currentTimeMillis().toInt(),
                                userId = user.id,
                                orderId = orderId,
                                title = "Actualización de pedido",
                                body = message,
                                createdAt = System.currentTimeMillis(),
                            )
                            notifier.notifyOrderUpdate(
                                title = alert.title,
                                body = alert.body,
                                notificationId = alert.id,
                                orderId = orderId,
                            )
                            _uiState.update { state -> state.copy(inAppAlert = alert) }
                        }
                        lastKnownOrderStatus[orderId] = newStatus
                    }
                }
            }
        }

        pollJob = viewModelScope.launch {
            while (isActive && _uiState.value.currentUser != null) {
                repository.refreshDashboard()
                delay(5000)
            }
        }

        alertsJob = viewModelScope.launch {
            repository.alertsFlow(user.id).collect { alerts ->
                _uiState.update { it.copy(alerts = alerts) }
                if (!alertsInitialized) {
                    seenAlertIds.clear()
                    seenAlertIds.addAll(alerts.map { it.id })
                    alertsInitialized = true
                    return@collect
                }

                alerts
                    .filter { alert -> alert.id !in seenAlertIds }
                    .sortedBy { it.id }
                    .forEach { alert ->
                        if (!alert.title.equals("Sesión activa", ignoreCase = true)) {
                            notifier.notifyOrderUpdate(
                                title = alert.title,
                                body = alert.body,
                                notificationId = alert.id,
                                orderId = alert.orderId,
                            )
                            _uiState.update { state -> state.copy(inAppAlert = alert) }
                        }
                        seenAlertIds.add(alert.id)
                    }
            }
        }
    }

    fun clearInAppAlert() {
        _uiState.update { it.copy(inAppAlert = null) }
    }

    fun register(name: String, email: String, password: String, role: String) {
        launchWithState {
            repository.register(name, email, password, role)
                .onSuccess { emitMessage("Cuenta creada y sesión iniciada") }
                .onFailure { emitError(it.message ?: "No se pudo registrar") }
        }
    }

    fun login(email: String, password: String) {
        launchWithState {
            repository.login(email, password)
                .onSuccess { emitMessage("Sesión iniciada") }
                .onFailure { emitError(it.message ?: "No se pudo iniciar sesión") }
        }
    }

    fun logout() {
        launchWithState {
            repository.logout()
            emitMessage("Sesión cerrada")
        }
    }

    fun logoutAllDevices() {
        launchWithState {
            repository.logoutAllDevices()
            emitMessage("Sesión cerrada en todos los dispositivos")
        }
    }

    fun createOrder(
        productId: Int,
        quantity: Int,
        ingredients: String,
        size: String,
        shape: String,
        flavor: String,
        color: String,
        address: String,
        notes: String,
    ) {
        val user = _uiState.value.currentUser ?: return
        if (user.role != "customer") {
            emitError("Solo los clientes pueden crear pedidos")
            return
        }
        if (productId <= 0) {
            emitError("Selecciona un producto antes de crear el pedido")
            return
        }
        if (quantity <= 0) {
            emitError("La cantidad debe ser mayor a 0")
            return
        }
        if (address.isBlank()) {
            emitError("Ingresa una dirección de entrega válida")
            return
        }

        launchWithState {
            repository.createOrder(
                customerId = user.id,
                productId = productId,
                quantity = quantity,
                ingredients = ingredients,
                size = size,
                shape = shape,
                flavor = flavor,
                color = color,
                address = address,
                notes = notes,
            )
                .onSuccess { emitMessage("Pedido creado #$it") }
                .onFailure { emitError(it.message ?: "No se pudo crear el pedido") }
        }
    }

    fun refreshDashboard() {
        launchWithState {
            repository.refreshDashboard()
                .onSuccess {
                    if (_uiState.value.currentUser?.role == "store") {
                        repository.ordersSummary("day")
                            .onSuccess { summary -> _uiState.update { it.copy(adminSummary = summary) } }
                    } else {
                        repository.getStorePublicProfile()
                            .onSuccess { (name, email) -> _uiState.update { it.copy(storeName = name, storeEmail = email) } }
                    }
                    emitMessage("Información actualizada")
                }
                .onFailure { emitError(it.message ?: "No se pudo actualizar") }
        }
    }

    fun updateCustomerProfile(name: String, email: String) {
        launchWithState {
            repository.updateCurrentUserProfile(name, email)
                .onSuccess { updated ->
                    _uiState.update { it.copy(currentUser = updated) }
                    emitMessage("Perfil actualizado")
                }
                .onFailure { emitError(it.message ?: "No se pudo actualizar el perfil") }
        }
    }

    fun payOrder(orderId: Int, cardNumber: String, cardName: String, securityCode: String, expiry: String) {
        launchWithState {
            repository.payOrder(orderId, cardNumber, cardName, securityCode, expiry)
                .onSuccess { emitMessage(it) }
                .onFailure { emitError(it.message ?: "Pago rechazado") }
        }
    }

    fun advanceOrder(orderId: Int, status: String) {
        launchWithState {
            repository.updateOrderStatus(orderId, status)
                .onSuccess { emitMessage("Estado actualizado") }
                .onFailure { emitError(it.message ?: "No se pudo actualizar") }
        }
    }

    fun diagnosePayment(orderId: Int) {
        launchWithState {
            repository.paymentDiagnostics(orderId)
                .onSuccess { emitMessage(it) }
                .onFailure { emitError(it.message ?: "No se pudo consultar el diagnóstico de pago") }
        }
    }

    fun addProduct(name: String, description: String, basePrice: Double, stock: Int, imageUrl: String) {
        launchWithState {
            repository.addProduct(name, description, basePrice, stock, imageUrl)
                .onSuccess {
                    _uiState.update { state -> state.copy(suggestedImageUrl = "") }
                    emitMessage("Producto agregado")
                }
                .onFailure { emitError(it.message ?: "No se pudo agregar") }
        }
    }

    fun updateProduct(productId: Int, name: String, description: String, basePrice: Double, stock: Int) {
        launchWithState {
            repository.updateProduct(productId, name, description, basePrice, stock)
                .onSuccess { emitMessage("Producto actualizado") }
                .onFailure { emitError(it.message ?: "No se pudo actualizar el producto") }
        }
    }

    fun deleteProduct(productId: Int) {
        launchWithState {
            repository.deleteProduct(productId)
                .onSuccess { emitMessage(it) }
                .onFailure { emitError(it.message ?: "No se pudo eliminar el producto") }
        }
    }

    fun loadAdminSummary(period: String) {
        launchWithState {
            repository.ordersSummary(period)
                .onSuccess { summary ->
                    _uiState.update { it.copy(adminSummary = summary) }
                }
                .onFailure { emitError(it.message ?: "No se pudo cargar el resumen de pedidos") }
        }
    }

    fun uploadProductImage(sourceUrl: String) {
        launchWithState {
            repository.uploadImageToCloudinary(sourceUrl)
                .onSuccess { imageUrl ->
                    _uiState.update { state -> state.copy(suggestedImageUrl = imageUrl) }
                    emitMessage("Imagen lista para publicar")
                }
                .onFailure { emitError(uploadErrorMessage(it)) }
        }
    }

    fun uploadProductImage(uri: Uri) {
        launchWithState {
            repository.uploadImageFileToCloudinary(uri)
                .onSuccess { imageUrl ->
                    _uiState.update { state -> state.copy(suggestedImageUrl = imageUrl) }
                    emitMessage("Imagen lista para publicar")
                }
                .onFailure { emitError(uploadErrorMessage(it)) }
        }
    }

    private fun uploadErrorMessage(error: Throwable): String {
        return normalizeErrorText(error.message.orEmpty(), "No se pudo subir imagen")
    }

    fun markOutOfStock(productId: Int) {
        launchWithState {
            repository.setOutOfStock(productId)
                .onSuccess { emitMessage("Producto marcado como agotado") }
                .onFailure { emitError(it.message ?: "No se pudo marcar") }
        }
    }

    fun restockProduct(productId: Int, unitsToAdd: Int) {
        launchWithState {
            repository.restockProduct(productId, unitsToAdd)
                .onSuccess { emitMessage("Inventario actualizado") }
                .onFailure { emitError(it.message ?: "No se pudo reponer stock") }
        }
    }

    fun toggleProductAvailability(productId: Int, isActive: Boolean) {
        launchWithState {
            repository.toggleProductActive(productId, isActive)
                .onSuccess { emitMessage(if (isActive) "Producto activado" else "Producto ocultado") }
                .onFailure { emitError(it.message ?: "No se pudo actualizar disponibilidad") }
        }
    }

    fun setProductOutOfStock(productId: Int, isOutOfStock: Boolean) {
        launchWithState {
            repository.setProductStockState(productId, isOutOfStock)
                .onSuccess {
                    emitMessage(
                        if (isOutOfStock) "Producto marcado como agotado"
                        else "Producto disponible nuevamente"
                    )
                }
                .onFailure { emitError(it.message ?: "No se pudo actualizar stock") }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(message = "", error = "") }
    }

    private fun launchWithState(block: suspend DulceViewModel.() -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }
            try {
                block()
            } finally {
                _uiState.update { it.copy(loading = false) }
            }
        }
    }

    private fun emitMessage(message: String) {
        _uiState.update { it.copy(message = message, error = "", screenState = UiState.Success(Unit)) }
    }

    private fun emitError(error: String) {
        val readableError = normalizeErrorText(error, "Ocurrió un problema inesperado")
        val type = when {
            readableError.contains("conex", ignoreCase = true) -> UiErrorType.NETWORK
            readableError.contains("pago", ignoreCase = true) || readableError.contains("tarjeta", ignoreCase = true) -> UiErrorType.PAYMENT_REJECTED
            readableError.contains("agotado", ignoreCase = true) -> UiErrorType.OUT_OF_STOCK
            readableError.contains("servidor", ignoreCase = true) || readableError.contains("pasarela", ignoreCase = true) -> UiErrorType.SERVER
            else -> UiErrorType.UNKNOWN
        }
        _uiState.update {
            it.copy(
                error = readableError,
                message = "",
                screenState = UiState.Error(message = readableError, type = type),
            )
        }
    }

    private fun normalizeErrorText(raw: String, fallback: String): String {
        val text = raw.trim()
        if (text.isBlank()) return fallback

        val lowered = text.lowercase()
        if (lowered.contains("payment required") || lowered.contains("402")) {
            return "El pago no pudo procesarse. Revisa la tarjeta e inténtalo de nuevo."
        }
        val paymentMessage = mapMercadoPagoStatusDetail(lowered)
        if (paymentMessage != null) return paymentMessage

        if (lowered.contains("no address associated") || lowered.contains("failed to connect") || lowered.contains("timeout")) {
            return "No hay conexión con el servidor. Verifica internet e inténtalo de nuevo."
        }
        if (lowered.contains("no autorizado") || lowered.contains("unauthorized") || lowered.contains("401")) {
            return "Tu sesión expiró. Inicia sesión nuevamente."
        }
        if (lowered.contains("403")) {
            return "No tienes permisos para realizar esta acción."
        }
        if (lowered.contains("404")) {
            return "No se encontró la información solicitada."
        }
        if (lowered.contains("500") || lowered.contains("502") || lowered.contains("503") || lowered.contains("504")) {
            return "El servidor está temporalmente no disponible. Intenta de nuevo en unos minutos."
        }

        return text
            .replace(Regex("HTTP\\s*\\d+", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\b(4\\d\\d|5\\d\\d)\\b"), "")
            .replace(":", " ")
            .replace(Regex("\\s+"), " ")
            .trim()
            .ifBlank { fallback }
    }

    private fun mapMercadoPagoStatusDetail(text: String): String? {
        return when {
            text.contains("cc_rejected_bad_filled_card_number") -> "El número de tarjeta es inválido."
            text.contains("cc_rejected_bad_filled_date") -> "La fecha de vencimiento no es válida."
            text.contains("cc_rejected_bad_filled_security_code") -> "El código de seguridad (CVV) es inválido."
            text.contains("cc_rejected_call_for_authorize") -> "Debes autorizar el pago con tu banco."
            text.contains("cc_rejected_card_disabled") -> "La tarjeta está deshabilitada. Contacta a tu banco."
            text.contains("cc_rejected_duplicated_payment") -> "Este pago parece duplicado. Revisa tus movimientos."
            text.contains("cc_rejected_high_risk") -> "Pago rechazado por seguridad. Intenta con otra tarjeta."
            text.contains("cc_rejected_insufficient_amount") -> "Fondos insuficientes en la tarjeta."
            text.contains("cc_rejected_max_attempts") -> "Se alcanzó el máximo de intentos. Intenta más tarde."
            text.contains("cc_rejected_other_reason") -> "El banco rechazó el pago. Prueba con otra tarjeta."
            text.contains("cc_rejected") -> "El pago fue rechazado. Verifica tus datos e inténtalo nuevamente."
            else -> null
        }
    }

    private fun statusMessage(status: String): String {
        return when (status) {
            "in_oven" -> "Tu pastel entró al horno"
            "decorating" -> "Estamos decorando tu pastel"
            "on_the_way" -> "¡Tu pedido va en camino!"
            "delivered" -> "Tu pedido fue entregado"
            else -> "Tu pedido cambió de estado"
        }
    }
}
