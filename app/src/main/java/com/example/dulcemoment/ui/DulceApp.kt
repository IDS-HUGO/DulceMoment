package com.example.dulcemoment.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.example.dulcemoment.data.local.OrderWithDetails
import com.example.dulcemoment.data.local.ProductWithOptions
import com.example.dulcemoment.ui.screens.CustomerModuleScreen
import com.example.dulcemoment.ui.screens.ErrorStateScreen
import com.example.dulcemoment.ui.screens.LoginGlassScreen
import com.example.dulcemoment.ui.screens.OrderStatusStepper
import com.example.dulcemoment.ui.screens.RegisterGlassScreen
import com.example.dulcemoment.ui.screens.SellerModuleScreen
import com.example.dulcemoment.ui.state.UiState

private object Routes {
    const val Login = "auth/login"
    const val Register = "auth/register"
    const val Customer = "customer/home"
    const val Store = "store/home"
    const val OrderDetail = "orders/detail/{orderId}"

    fun orderDetail(orderId: Int): String = "orders/detail/$orderId"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DulceApp(
    openOrderIdRequest: Int? = null,
    onOrderRequestConsumed: () -> Unit = {},
    viewModel: DulceViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val stockState by viewModel.stockState.collectAsStateWithLifecycle()
    val navController = rememberNavController()

    LaunchedEffect(uiState.message, uiState.error) {
        if (uiState.message.isNotBlank() || uiState.error.isNotBlank()) {
            kotlinx.coroutines.delay(2200)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.currentUser?.id, uiState.currentUser?.role) {
        val user = uiState.currentUser
        if (user == null) {
            navController.navigate(Routes.Login) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        } else {
            val destination = if (user.role == "store") Routes.Store else Routes.Customer
            navController.navigate(destination) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        }
    }

    LaunchedEffect(openOrderIdRequest, uiState.currentUser?.id) {
        val orderId = openOrderIdRequest ?: return@LaunchedEffect
        if (uiState.currentUser != null) {
            navController.navigate(Routes.orderDetail(orderId))
            onOrderRequestConsumed()
        }
    }

    LaunchedEffect(uiState.inAppAlert?.id) {
        if (uiState.inAppAlert != null) {
            kotlinx.coroutines.delay(4500)
            viewModel.clearInAppAlert()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("DulceMoment", style = MaterialTheme.typography.titleLarge)
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            val currentScreenState = uiState.screenState
            if (currentScreenState is UiState.Error) {
                ErrorStateScreen(
                    type = currentScreenState.type,
                    message = currentScreenState.message,
                    onRetry = viewModel::refreshDashboard,
                )
                return@Box
            }

            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                StatusBanner(
                    loading = uiState.loading,
                    message = uiState.message,
                    error = uiState.error,
                )
                InAppAlertBanner(
                    alert = uiState.inAppAlert,
                    onOpenOrder = { orderId -> navController.navigate(Routes.orderDetail(orderId)) },
                    onDismiss = viewModel::clearInAppAlert,
                )

                NavHost(
                    navController = navController,
                    startDestination = Routes.Login,
                    modifier = Modifier.fillMaxSize(),
                ) {
            composable(Routes.Login) {
                LoginGlassScreen(
                    onLogin = viewModel::login,
                    onGoRegister = { navController.navigate(Routes.Register) },
                )
            }

            composable(Routes.Register) {
                RegisterGlassScreen(
                    onRegister = viewModel::register,
                    onGoLogin = { navController.navigate(Routes.Login) },
                )
            }

            composable(Routes.Customer) {
                CustomerModuleScreen(
                    products = uiState.products,
                    stockState = stockState,
                    orders = uiState.orders,
                    alerts = uiState.alerts.map { it.body },
                    onCreateOrder = { productId, quantity, ingredients, size, shape, flavor, color, address, notes ->
                        viewModel.createOrder(productId, quantity, ingredients, size, shape, flavor, color, address, notes)
                    },
                    onPay = viewModel::payOrder,
                    onOpenOrder = { orderId -> navController.navigate(Routes.orderDetail(orderId)) },
                    onLogout = viewModel::logout,
                    onLogoutAll = viewModel::logoutAllDevices,
                    onRefresh = viewModel::refreshDashboard,
                )
            }

            composable(Routes.Store) {
                SellerModuleScreen(
                    products = uiState.products,
                    stockState = stockState,
                    orders = uiState.orders,
                    suggestedImageUrl = uiState.suggestedImageUrl,
                    onUploadImageFile = viewModel::uploadProductImage,
                    onUploadImageUrl = viewModel::uploadProductImage,
                    onAddProduct = viewModel::addProduct,
                    onToggleOutOfStock = viewModel::setProductOutOfStock,
                    onToggleAvailability = viewModel::toggleProductAvailability,
                    onStageUpdate = viewModel::advanceOrder,
                    onOpenOrder = { orderId -> navController.navigate(Routes.orderDetail(orderId)) },
                    onLogout = viewModel::logout,
                    onLogoutAll = viewModel::logoutAllDevices,
                    onRefresh = viewModel::refreshDashboard,
                )
            }

            composable(
                route = Routes.OrderDetail,
                arguments = listOf(navArgument("orderId") { type = NavType.IntType }),
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getInt("orderId") ?: -1
                val order = uiState.orders.firstOrNull { it.order.id == orderId }
                OrderDetailScreen(
                    order = order,
                    isStore = uiState.currentUser?.role == "store",
                    onBack = { navController.popBackStack() },
                    onPay = { id, card, name, cvv, exp -> viewModel.payOrder(id, card, name, cvv, exp) },
                    onStageUpdate = { id, status -> viewModel.advanceOrder(id, status) },
                    onDiagnosePayment = viewModel::diagnosePayment,
                )
            }
                }
            }

            if (uiState.loading) {
                LoadingOverlay()
            }
        }
    }
}

@Composable
private fun InAppAlertBanner(
    alert: com.example.dulcemoment.data.local.PushAlertEntity?,
    onOpenOrder: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    if (alert == null) return

    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(alert.title, style = MaterialTheme.typography.titleMedium)
            Text(alert.body, style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (alert.orderId != null) {
                    Button(onClick = { onOpenOrder(alert.orderId) }) { Text("Ver pedido") }
                }
                Button(onClick = onDismiss) { Text("Cerrar") }
            }
        }
    }
}

@Composable
private fun LoadingOverlay() {
    Surface(
        color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.18f),
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(strokeWidth = 3.dp)
                    Text("Procesando...", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
private fun StatusBanner(loading: Boolean, message: String, error: String) {
    if (!loading && message.isBlank() && error.isBlank()) return

    val bgColor = when {
        error.isNotBlank() -> MaterialTheme.colorScheme.errorContainer
        loading -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.tertiaryContainer
    }
    val text = when {
        error.isNotBlank() -> "Error: $error"
        loading -> "Cargando información..."
        else -> message
    }
    Surface(
        color = bgColor,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(12.dp),
        )
    }
}

@Composable
private fun LoginScreen(
    onLogin: (String, String) -> Unit,
    onGoRegister: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Login", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = { onLogin(email, password) }) { Text("Iniciar sesión") }
        Button(onClick = onGoRegister) { Text("Ir a Register") }
    }
}

@Composable
private fun RegisterScreen(
    onRegister: (String, String, String, String) -> Unit,
    onGoLogin: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("customer") }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Register", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña") }, modifier = Modifier.fillMaxWidth())

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { role = "customer" }) { Text("Cliente") }
            Button(onClick = { role = "store" }) { Text("Tienda") }
        }
        Text("Rol: $role")
        Button(onClick = { onRegister(name, email, password, role) }) { Text("Crear cuenta") }
        Button(onClick = onGoLogin) { Text("Volver a Login") }
    }
}

@Composable
private fun ClientSection(
    products: List<ProductWithOptions>,
    orders: List<OrderWithDetails>,
    alerts: List<String>,
    onCreateOrder: (Int, Int, String, String, String, String, String, String, String) -> Unit,
    onPay: (Int, String, String) -> Unit,
    onOpenOrder: (Int) -> Unit,
    onLogout: () -> Unit,
    onLogoutAll: () -> Unit,
    onRefresh: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var selectedProductId by remember { mutableStateOf(0) }
    var quantity by remember { mutableStateOf(1) }
    var ingredients by remember { mutableStateOf("fresa,oreo") }
    var size by remember { mutableStateOf("mediano") }
    var shape by remember { mutableStateOf("redondo") }
    var flavor by remember { mutableStateOf("chocolate") }
    var color by remember { mutableStateOf("rosa") }
    var address by remember { mutableStateOf("Av. Principal 123") }
    var notes by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("4242 4242 4242 4242") }
    var cardName by remember { mutableStateOf("Cliente Demo") }
    val selectedProduct = products.firstOrNull { it.product.id == selectedProductId }
    val visibleProducts = remember(products, query) {
        if (query.isBlank()) products
        else products.filter { it.product.name.contains(query, ignoreCase = true) || it.product.description.contains(query, ignoreCase = true) }
    }
    val estimatedTotal = remember(selectedProduct, quantity, ingredients, size, shape, flavor, color) {
        selectedProduct?.let { selected ->
            val options = selected.options
            val map = mapOf(
                "size" to size,
                "shape" to shape,
                "flavor" to flavor,
                "color" to color,
            )
            val customDelta = map.entries.sumOf { (category, value) ->
                options.firstOrNull { it.category == category && it.value.equals(value, ignoreCase = true) }?.priceDelta ?: 0.0
            }
            val ingredientList = ingredients.split(",").map { it.trim() }.filter { it.isNotBlank() }
            val ingredientsDelta = ingredientList.sumOf { ingredient ->
                options.firstOrNull { it.category == "ingredient" && it.value.equals(ingredient, ignoreCase = true) }?.priceDelta ?: 0.0
            }
            (selected.product.basePrice + customDelta + ingredientsDelta) * quantity
        } ?: 0.0
    }

    val latestOrder = orders.firstOrNull()

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("Catálogo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onLogout) { Text("Cerrar sesión") }
                Button(onClick = onRefresh) { Text("Actualizar") }
            }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Buscar producto") },
                modifier = Modifier.fillMaxWidth(),
            )
            if (selectedProductId > 0) {
                Text("Seleccionado: #$selectedProductId")
            }
            if (visibleProducts.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("No encontramos productos para mostrar")
                        Button(onClick = onRefresh) { Text("Reintentar carga") }
                    }
                }
            }
        }

        items(visibleProducts) { product ->
            ProductCard(product = product, onChoose = { selectedProductId = product.product.id })
        }

        item {
            Text("Personalización", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { if (quantity > 1) quantity-- }) { Text("-") }
                Text("Cantidad: $quantity", modifier = Modifier.weight(1f).padding(top = 10.dp))
                Button(onClick = { quantity++ }) { Text("+") }
            }
            OutlinedTextField(value = ingredients, onValueChange = { ingredients = it }, label = { Text("Ingredientes (coma)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = size, onValueChange = { size = it }, label = { Text("Tamaño") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = shape, onValueChange = { shape = it }, label = { Text("Forma") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = flavor, onValueChange = { flavor = it }, label = { Text("Sabor") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = color, onValueChange = { color = it }, label = { Text("Color") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Dirección") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notas") }, modifier = Modifier.fillMaxWidth())
            if (selectedProduct != null) {
                Text("Total estimado: ${"%.2f".format(estimatedTotal)}")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    if (selectedProductId > 0 && selectedProduct?.product?.stock ?: 0 > 0) {
                        onCreateOrder(selectedProductId, quantity, ingredients, size, shape, flavor, color, address, notes)
                    }
                }) { Text("Comprar ahora") }
            }
        }

        item {
            Text("Pago con tarjeta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(value = cardNumber, onValueChange = { cardNumber = it }, label = { Text("Número de tarjeta") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = cardName, onValueChange = { cardName = it }, label = { Text("Titular") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = {
                latestOrder?.let { onPay(it.order.id, cardNumber, cardName) }
            }) { Text("Pagar último pedido") }
        }
    }
}

@Composable
private fun StoreSection(
    products: List<ProductWithOptions>,
    orders: List<OrderWithDetails>,
    suggestedImageUrl: String,
    onUploadImage: (String) -> Unit,
    onAddProduct: (String, String, Double, Int, String) -> Unit,
    onOutOfStock: (Int) -> Unit,
    onRestock: (Int, Int) -> Unit,
    onToggleAvailability: (Int, Boolean) -> Unit,
    onStageUpdate: (Int, String) -> Unit,
    onOpenOrder: (Int) -> Unit,
    onLogout: () -> Unit,
    onLogoutAll: () -> Unit,
    onRefresh: () -> Unit,
) {
    var name by remember { mutableStateOf("Nuevo pastel") }
    var description by remember { mutableStateOf("Pastel artesanal") }
    var basePrice by remember { mutableStateOf("350") }
    var stock by remember { mutableStateOf("8") }
    var uploadSourceUrl by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    LaunchedEffect(suggestedImageUrl) {
        if (suggestedImageUrl.isNotBlank()) {
            imageUrl = suggestedImageUrl
        }
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("Panel Tienda", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onLogout) { Text("Cerrar sesión") }
                Button(onClick = onRefresh) { Text("Actualizar") }
            }
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = basePrice, onValueChange = { basePrice = it }, label = { Text("Precio base") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Stock") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = uploadSourceUrl, onValueChange = { uploadSourceUrl = it }, label = { Text("URL origen imagen") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = { onUploadImage(uploadSourceUrl) }) { Text("Subir URL a Cloudinary") }
            OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("URL imagen producto") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                val parsedPrice = basePrice.toDoubleOrNull() ?: return@Button
                val parsedStock = stock.toIntOrNull() ?: return@Button
                onAddProduct(name, description, parsedPrice, parsedStock, imageUrl)
            }) {
                Text("Agregar producto")
            }
        }

        item {
            Text("Inventario", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (products.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Tu inventario está vacío")
                        Button(onClick = onRefresh) { Text("Actualizar") }
                    }
                }
            }
        }

        items(products) { product ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(product.product.name, fontWeight = FontWeight.SemiBold)
                    Text("Stock: ${product.product.stock} ${if (product.product.stock <= 0) "(AGOTADO)" else ""}")
                    Text("Visible: ${if (product.product.isActive) "Sí" else "No"}")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { onOutOfStock(product.product.id) }) {
                            Text("Marcar agotado")
                        }
                        Button(onClick = { onRestock(product.product.id, 5) }) {
                            Text("+5 stock")
                        }
                        Button(onClick = { onToggleAvailability(product.product.id, !product.product.isActive) }) {
                            Text(if (product.product.isActive) "Ocultar" else "Activar")
                        }
                    }
                }
            }
        }

        item {
            Text("Pedidos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            orders.forEach { order ->
                Text("#${order.order.id} | ${order.order.status} | Total ${order.order.total}")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onStageUpdate(order.order.id, "in_oven") }) { Text("Horno") }
                    Button(onClick = { onStageUpdate(order.order.id, "decorating") }) { Text("Decorar") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onStageUpdate(order.order.id, "on_the_way") }) { Text("En camino") }
                    Button(onClick = { onStageUpdate(order.order.id, "delivered") }) { Text("Entregar") }
                }
                Button(onClick = { onOpenOrder(order.order.id) }) { Text("Ver detalle") }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun OrderDetailScreen(
    order: OrderWithDetails?,
    isStore: Boolean,
    onBack: () -> Unit,
    onPay: (Int, String, String, String, String) -> Unit,
    onStageUpdate: (Int, String) -> Unit,
    onDiagnosePayment: (Int) -> Unit,
) {
    var cardNumber by remember { mutableStateOf("4242 4242 4242 4242") }
    var cardName by remember { mutableStateOf("Cliente Demo") }
    var cvv by remember { mutableStateOf("123") }
    var expiry by remember { mutableStateOf("12/30") }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(onClick = onBack) { Text("Volver") }
        if (order == null) {
            Text("Pedido no encontrado")
            return
        }

        Text("Pedido #${order.order.id}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text("Estado: ${order.order.status}")
        OrderStatusStepper(currentStatus = order.order.status)
        Text("Total: ${order.order.total}")
        Text("Dirección: ${order.order.deliveryAddress}")

        Text("Items", fontWeight = FontWeight.SemiBold)
        order.items.forEach { item ->
            Text("• Producto ${item.productId} - ${item.unitPrice}")
        }

        Text("Tracking", fontWeight = FontWeight.SemiBold)
        order.events.sortedBy { it.id }.forEach { event ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(event.status, fontWeight = FontWeight.SemiBold)
                    Text(event.message)
                }
            }
        }

        if (isStore) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onStageUpdate(order.order.id, "in_oven") }) { Text("Horno") }
                Button(onClick = { onStageUpdate(order.order.id, "decorating") }) { Text("Decorar") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onStageUpdate(order.order.id, "on_the_way") }) { Text("En camino") }
                Button(onClick = { onStageUpdate(order.order.id, "delivered") }) { Text("Entregar") }
            }
        } else {
            OutlinedTextField(value = cardNumber, onValueChange = { cardNumber = it }, label = { Text("Número tarjeta") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = cardName, onValueChange = { cardName = it }, label = { Text("Titular") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = cvv, onValueChange = { cvv = it }, label = { Text("CVV") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = expiry, onValueChange = { expiry = it }, label = { Text("Exp MM/YY") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = { onPay(order.order.id, cardNumber, cardName, cvv, expiry) }) { Text("Pagar pedido") }
            Button(onClick = { onDiagnosePayment(order.order.id) }) { Text("Diagnóstico de pago") }
        }
    }
}

@Composable
private fun OrderStatusTimeline(currentStatus: String) {
    val stages = listOf("created", "in_oven", "decorating", "on_the_way", "delivered")
    val labels = mapOf(
        "created" to "Confirmado",
        "in_oven" to "En horno",
        "decorating" to "Decorando",
        "on_the_way" to "En camino",
        "delivered" to "Entregado",
    )
    val currentIndex = stages.indexOf(currentStatus).coerceAtLeast(0)
    val progress = (currentIndex + 1) / stages.size.toFloat()

    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            stages.forEachIndexed { index, stage ->
                val completed = index <= currentIndex
                val bg = if (completed) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = bg),
                ) {
                    Text(
                        text = labels[stage] ?: stage,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductCard(
    product: ProductWithOptions,
    onChoose: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (product.product.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = product.product.imageUrl,
                    contentDescription = product.product.name,
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    contentScale = ContentScale.Crop,
                )
            }
            Text(product.product.name, fontWeight = FontWeight.SemiBold)
            Text(product.product.description)
            Text("Precio base: ${product.product.basePrice}")
            Text("Stock: ${product.product.stock} ${if (product.product.stock <= 0) "(AGOTADO)" else ""}")
            Button(onClick = onChoose) { Text("Seleccionar") }
        }
    }
}
