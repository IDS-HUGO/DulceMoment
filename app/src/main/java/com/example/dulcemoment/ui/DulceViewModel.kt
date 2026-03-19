package com.example.dulcemoment.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dulcemoment.data.local.OrderWithDetails
import com.example.dulcemoment.data.local.ProductWithOptions
import com.example.dulcemoment.data.local.PushAlertEntity
import com.example.dulcemoment.data.local.UserEntity
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
            _uiState.update { it.copy(orders = emptyList(), alerts = emptyList()) }
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
                .onSuccess { emitMessage("Información actualizada") }
                .onFailure { emitError(it.message ?: "No se pudo actualizar") }
        }
    }

    fun payOrder(orderId: Int, cardNumber: String, cardName: String) {
        launchWithState {
            repository.payOrder(orderId, cardNumber, cardName)
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

    fun uploadProductImage(sourceUrl: String) {
        launchWithState {
            repository.uploadImageToCloudinary(sourceUrl)
                .onSuccess { imageUrl ->
                    _uiState.update { state -> state.copy(suggestedImageUrl = imageUrl) }
                    emitMessage("Imagen subida a Cloudinary")
                }
                .onFailure { emitError(it.message ?: "No se pudo subir imagen") }
        }
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
        val type = when {
            error.contains("conex", ignoreCase = true) -> UiErrorType.NETWORK
            error.contains("pago", ignoreCase = true) -> UiErrorType.PAYMENT_REJECTED
            error.contains("agotado", ignoreCase = true) -> UiErrorType.OUT_OF_STOCK
            error.contains("server", ignoreCase = true) || error.contains("502") || error.contains("500") -> UiErrorType.SERVER
            else -> UiErrorType.UNKNOWN
        }
        _uiState.update {
            it.copy(
                error = error,
                message = "",
                screenState = UiState.Error(message = error, type = type),
            )
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
