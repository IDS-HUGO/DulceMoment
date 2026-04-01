package com.example.dulcemoment.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.dulcemoment.data.local.OrderWithDetails
import com.example.dulcemoment.data.local.ProductWithOptions
import com.example.dulcemoment.ui.theme.ThemeConstants
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class CustomerSection {
    Catalog,
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun CheckoutBottomSheet(
        sheetState: androidx.compose.material3.SheetState,
        orderId: Int?,
        onDismiss: () -> Unit,
        onPay: (Int, String, String, String, String) -> Unit,
    ) {
        var cardNumber by rememberSaveable { mutableStateOf("") }
        var holderName by rememberSaveable { mutableStateOf("") }
        var cvv by rememberSaveable { mutableStateOf("") }
        var expiry by rememberSaveable { mutableStateOf("") }
        var showCvv by rememberSaveable { mutableStateOf(false) }
        var errorType by rememberSaveable { mutableStateOf<com.example.dulcemoment.ui.state.UiErrorType?>(null) }
        var errorMessage by rememberSaveable { mutableStateOf("") }

        // TODO: Reemplazar con datos reales del pedido
        val subtotal = 100.0 // Ejemplo
        val iva = subtotal * 0.05
        val total = subtotal + iva

        val maskedCard = remember(cardNumber) { maskCardNumber(cardNumber) }
        val maskedExp = remember(expiry) { maskExpiry(expiry) }
        val cardValid = maskedCard.filter { it.isDigit() }.length == 16
        val holderValid = holderName.trim().length >= 3
        val cvvValid = cvv.length in 3..4
        val expValid = maskedExp.length == 5

        if (errorType != null) {
            ErrorStateScreen(
                type = errorType!!,
                message = errorMessage,
                onRetry = {
                    errorType = null
                    errorMessage = ""
                }
            )
            return
        }

        ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text("Pago del pedido", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ThemeConstants.ChocolateSecondary)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ThemeConstants.SurfaceLight),
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Subtotal: $${"%.2f".format(subtotal)}", color = ThemeConstants.TextMedium)
                        Text("IVA (5%): $${"%.2f".format(iva)}", color = ThemeConstants.TextMedium)
                        Text("Total: $${"%.2f".format(total)}", fontWeight = FontWeight.Bold, color = ThemeConstants.ChocolateSecondary)
                    }
                }
                OutlinedTextField(
                    value = maskedCard,
                    onValueChange = { cardNumber = it.filter(Char::isDigit).take(16) },
                    label = { Text("Número de tarjeta") },
                    isError = !cardValid && maskedCard.isNotBlank(),
                    supportingText = { if (!cardValid && maskedCard.isNotBlank()) Text("Formato 16 dígitos") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Filled.CreditCard, contentDescription = null) },
                )
                OutlinedTextField(
                    value = cvv,
                    onValueChange = { cvv = it.filter(Char::isDigit).take(4) },
                    label = { Text("CVV") },
                    isError = !cvvValid && cvv.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    visualTransformation = if (showCvv) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showCvv = !showCvv }) {
                            Icon(
                                imageVector = if (showCvv) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (showCvv) "Ocultar CVV" else "Mostrar CVV"
                            )
                        }
                    },
                )
                OutlinedTextField(
                    value = holderName,
                    onValueChange = { holderName = it },
                    label = { Text("Titular de la tarjeta") },
                    isError = !holderValid && holderName.isNotBlank(),
                    supportingText = { if (!holderValid && holderName.isNotBlank()) Text("Mínimo 3 caracteres") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                )
                OutlinedTextField(
                    value = maskedExp,
                    onValueChange = { expiry = it.filter(Char::isDigit).take(4) },
                    label = { Text("Exp MM/YY") },
                    isError = !expValid && maskedExp.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Filled.DateRange, contentDescription = null) },
                )
                Button(
                    onClick = {
                        if (orderId != null) {
                            // Aquí iría la llamada real a la API de pago
                            // Simulación de errores para demo:
                            if (!cardValid || !holderValid || !cvvValid || !expValid) {
                                errorType = com.example.dulcemoment.ui.state.UiErrorType.PAYMENT_REJECTED
                                errorMessage = "Datos de tarjeta inválidos."
                            } else {
                                // Simular error de red o servidor
                                errorType = com.example.dulcemoment.ui.state.UiErrorType.SERVER
                                errorMessage = "No se pudo procesar el pago. Intenta de nuevo."
                            }
                        }
                    },
                    enabled = orderId != null && cardValid && holderValid && cvvValid && expValid,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ThemeConstants.ChocolateSecondary, contentColor = Color.White)
                ) {
                    Text("Pagar")
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
        containerColor = ThemeConstants.CreamPrimary,
        bottomBar = {
            NavigationBar(containerColor = ThemeConstants.SurfaceLight) {
                NavigationBarItem(
                    selected = selectedSection == CustomerSection.Catalog,
                    onClick = { selectedSection = CustomerSection.Catalog },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Catálogo") },
                    label = { Text("Catálogo") },
                )
                NavigationBarItem(
                    selected = selectedSection == CustomerSection.Orders,
                    onClick = { selectedSection = CustomerSection.Orders },
                    icon = { Icon(Icons.Filled.ShoppingCart, contentDescription = "Pedidos") },
                    label = { Text("Pedidos") },
                )
                NavigationBarItem(
                    selected = selectedSection == CustomerSection.Profile,
                    onClick = { selectedSection = CustomerSection.Profile },
                    icon = { Icon(Icons.Filled.AccountCircle, contentDescription = "Perfil") },
                    label = { Text("Perfil") },
                )
            }
        },
    ) { innerPadding ->
        when (selectedSection) {
            CustomerSection.Catalog -> {
                LazyColumn(
                    modifier = Modifier
                        .padding(innerPadding)
                        .background(ThemeConstants.CreamPrimary),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                "Catálogo",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = ThemeConstants.ChocolateSecondary
                            )
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = ThemeConstants.SurfaceLight),
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Vendido por: $sellerName", fontWeight = FontWeight.SemiBold, color = ThemeConstants.ChocolateSecondary)
                                    if (sellerEmail.isNotBlank()) {
                                        Text("Contacto: $sellerEmail", color = ThemeConstants.TextMedium)
                                    }
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = onRefresh,
                                    modifier = Modifier.weight(1f),
                                    colors = primaryButtonColors
                                ) {
                                    Text("Actualizar", fontWeight = FontWeight.SemiBold)
                                }
                                Button(
                                    onClick = { showCheckoutSheet = true },
                                    enabled = latestOrder != null,
                                    modifier = Modifier.weight(1f),
                                    colors = accentButtonColors
                                ) {
                                    Text("Pagar", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    item {
                        if (products.isEmpty()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp),
                                colors = CardDefaults.cardColors(containerColor = ThemeConstants.SurfaceLight),
                            ) {
                                Text(
                                    "No hay productos disponibles en este momento.",
                                    modifier = Modifier.padding(14.dp),
                                    color = ThemeConstants.TextMedium,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                products.chunked(2).forEach { rowProducts ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        rowProducts.forEach { product ->
                                            val inStock = stockState[product.product.id] ?: (product.product.stock > 0)
                                            ProductCatalogCard(
                                                modifier = Modifier.weight(1f),
                                                product = product,
                                                inStock = inStock,
                                                selected = selectedProductId == product.product.id,
                                                onSelect = { selectedProductId = product.product.id },
                                            )
                                        }
                                        if (rowProducts.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Personaliza tu pastel",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = ThemeConstants.ChocolateSecondary
                            )
                            AtelierSelector(
                                label = "Forma",
                                values = listOf("redondo", "cuadrado"),
                                selected = selectedShape,
                                onSelected = { selectedShape = it },
                            )
                            AtelierSelector(
                                label = "Sabor",
                                values = listOf("chocolate", "vainilla", "fresa"),
                                selected = selectedFlavor,
                                onSelected = { selectedFlavor = it },
                            )
                            AtelierSelector(
                                label = "Color",
                                values = listOf("rosa", "blanco", "azul"),
                                selected = selectedColor,
                                onSelected = { selectedColor = it },
                            )
                            OutlinedTextField(
                                value = ingredients,
                                onValueChange = { ingredients = it },
                                label = { Text("Ingredientes extra") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = fieldColors,
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = ThemeConstants.OnCreamPrimary),
                            )
                            OutlinedTextField(
                                value = address,
                                onValueChange = { address = it },
                                label = { Text("Dirección") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = fieldColors,
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = ThemeConstants.OnCreamPrimary),
                            )
                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                label = { Text("Notas") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = fieldColors,
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = ThemeConstants.OnCreamPrimary),
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = { if (quantity > 1) quantity-- },
                                    modifier = Modifier.size(50.dp),
                                    colors = accentButtonColors
                                ) {
                                    Text("-", color = ThemeConstants.ChocolateSecondary, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    "Cantidad: $quantity",
                                    fontWeight = FontWeight.SemiBold,
                                    color = ThemeConstants.ChocolateSecondary,
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = { quantity++ },
                                    modifier = Modifier.size(50.dp),
                                    colors = accentButtonColors
                                ) {
                                    Text("+", color = ThemeConstants.ChocolateSecondary, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text(
                                "Precio estimado: $${"%.2f".format(dynamicPrice)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ThemeConstants.ChocolateSecondary
                            )
                            Button(
                                onClick = {
                                    if (canCreateOrder) {
                                        val product = selectedProduct ?: return@Button
                                        onCreateOrder(
                                            product.product.id,
                                            quantity,
                                            ingredients,
                                            "mediano",
                                            selectedShape,
                                            selectedFlavor,
                                            selectedColor,
                                            address,
                                            notes,
                                        )
                                    }
                                },
                                enabled = canCreateOrder,
                                modifier = Modifier.fillMaxWidth(),
                                colors = primaryButtonColors
                            ) {
                                Text("Crear pedido", fontWeight = FontWeight.SemiBold)
                            }

                            val orderHint = when {
                                selectedProduct == null -> "Selecciona un producto para continuar."
                                !selectedInStock -> "El producto seleccionado no tiene stock disponible."
                                address.isBlank() -> "Agrega una dirección de entrega para crear el pedido."
                                else -> "Pedido listo para enviar."
                            }
                            Text(
                                text = orderHint,
                                color = if (canCreateOrder) ThemeConstants.ChocolateSecondary else ThemeConstants.TextMedium,
                                fontWeight = if (canCreateOrder) FontWeight.SemiBold else FontWeight.Normal,
                            )
                        }
                    }
                }
            }

            CustomerSection.Orders -> {
                LazyColumn(
                    modifier = Modifier
                        .padding(innerPadding)
                        .background(ThemeConstants.CreamPrimary),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            colors = CardDefaults.cardColors(containerColor = ThemeConstants.SurfaceLight),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    "Estado de tu pedido",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = ThemeConstants.ChocolateSecondary
                                )
                                if (latestOrder == null) {
                                    Text("Aún no tienes pedidos creados.", color = ThemeConstants.TextMedium)
                                } else {
                                    Text(
                                        "Pedido #${latestOrder.order.id} • ${orderStatusLabel(latestOrder.order.status)}",
                                        color = ThemeConstants.OnCreamPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    OrderStatusStepper(currentStatus = latestOrder.order.status)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { onOpenOrder(latestOrder.order.id) },
                                            modifier = Modifier.weight(1f),
                                            colors = primaryButtonColors
                                        ) {
                                            Text("Ver detalle", fontWeight = FontWeight.SemiBold)
                                        }
                                        if (latestOrder.order.status == "created") {
                                            Button(
                                                onClick = { showCheckoutSheet = true },
                                                modifier = Modifier.weight(1f),
                                                colors = accentButtonColors
                                            ) {
                                                Text("Pagar", fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }
                                    if (latestOrder.order.status == "created") {
                                        Text(
                                            "Al pagar, el vendedor será notificado automáticamente.",
                                            color = ThemeConstants.TextMedium,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            colors = CardDefaults.cardColors(containerColor = ThemeConstants.SurfaceLight),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    "Mis pedidos",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ThemeConstants.ChocolateSecondary
                                )
                                if (orders.isEmpty()) {
                                    Text("Aún no has realizado pedidos.", color = ThemeConstants.TextMedium)
                                } else {
                                    orders
                                        .sortedByDescending { it.order.createdAt }
                                        .forEach { orderDetail ->
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp),
                                                colors = CardDefaults.cardColors(containerColor = ThemeConstants.SurfaceLighter),
                                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(10.dp),
                                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            "Pedido #${orderDetail.order.id}",
                                                            color = ThemeConstants.ChocolateSecondary,
                                                            fontWeight = FontWeight.SemiBold,
                                                            style = MaterialTheme.typography.bodyLarge
                                                        )
                                                        Text(
                                                            orderStatusLabel(orderDetail.order.status),
                                                            color = ThemeConstants.TextMedium,
                                                            style = MaterialTheme.typography.bodySmall
                                                        )
                                                    }
                                                    Text(
                                                        "Fecha: ${formatOrderDate(orderDetail.order.createdAt)}",
                                                        color = ThemeConstants.TextMedium,
                                                        style = MaterialTheme.typography.bodySmall
                                                    )
                                                    Text(
                                                        "Total: $${"%.2f".format(orderDetail.order.total)}",
                                                        color = ThemeConstants.OnCreamPrimary,
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                        Button(
                                                            onClick = { onOpenOrder(orderDetail.order.id) },
                                                            colors = primaryButtonColors,
                                                        ) {
                                                            Text("Ver detalle", fontWeight = FontWeight.SemiBold)
                                                        }
                                                        if (orderDetail.order.status == "created") {
                                                            Button(
                                                                onClick = {
                                                                    showCheckoutSheet = true
                                                                },
                                                                colors = accentButtonColors,
                                                            ) {
                                                                Text("Pagar", fontWeight = FontWeight.SemiBold)
                                                            }
                                                        }
                                                    }
                                                    if (orderDetail.order.status == "created") {
                                                        Text(
                                                            "Al pagar, el vendedor será notificado automáticamente.",
                                                            color = ThemeConstants.TextMedium,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            modifier = Modifier.padding(top = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                }
                            }
                        }
                    }
                }
            }

            CustomerSection.Profile -> {
                LazyColumn(
                    modifier = Modifier
                        .padding(innerPadding)
                        .background(ThemeConstants.CreamPrimary),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            colors = CardDefaults.cardColors(containerColor = ThemeConstants.SurfaceLight),
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Mi perfil", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ThemeConstants.ChocolateSecondary)
                                OutlinedTextField(
                                    value = editableName,
                                    onValueChange = { editableName = it },
                                    label = { Text("Nombre") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = fieldColors,
                                )
                                OutlinedTextField(
                                    value = editableEmail,
                                    onValueChange = { editableEmail = it },
                                    label = { Text("Email") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = fieldColors,
                                )
                                Button(
                                    onClick = { onUpdateProfile(editableName, editableEmail) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = primaryButtonColors,
                                ) {
                                    Text("Guardar cambios", fontWeight = FontWeight.SemiBold)
                                }
                                Text("Vendedor actual: $sellerName", color = ThemeConstants.ChocolateSecondary, fontWeight = FontWeight.SemiBold)
                                if (sellerEmail.isNotBlank()) {
                                    Text("Contacto vendedor: $sellerEmail", color = ThemeConstants.TextMedium)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = onRefresh,
                                        modifier = Modifier.weight(1f),
                                        colors = accentButtonColors,
                                    ) {
                                        Text("Actualizar", fontWeight = FontWeight.SemiBold)
                                    }
                                    Button(
                                        onClick = onLogout,
                                        modifier = Modifier.weight(1f),
                                        colors = primaryButtonColors,
                                    ) {
                                        Text("Cerrar sesión", fontWeight = FontWeight.SemiBold)
                                    }
                                }
                                Button(
                                    onClick = onLogoutAll,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ThemeConstants.ErrorRed,
                                        contentColor = Color.White,
                                    ),
                                ) {
                                    Text("Cerrar sesión en todos", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatOrderDate(timeInMillis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return formatter.format(Date(timeInMillis))
}

@Composable
private fun ProductCatalogCard(
    modifier: Modifier = Modifier,
    product: ProductWithOptions,
    inStock: Boolean,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(24.dp),
    ) {
        Box {
            AsyncImage(
                model = product.product.imageUrl,
                contentDescription = product.product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                contentScale = ContentScale.Crop,
                colorFilter = if (!inStock) {
                    ColorFilter.tint(Color.Gray.copy(alpha = 0.4f), blendMode = BlendMode.SrcAtop)
                } else {
                    null
                },
            )

            androidx.compose.animation.AnimatedVisibility(
                visible = !inStock,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
            ) {
                Badge {
                    Text("AGOTADO")
                }
            }
        }

        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(product.product.name, fontWeight = FontWeight.SemiBold, color = ThemeConstants.ChocolateSecondary)
            Text("$${"%.2f".format(product.product.basePrice)}", color = ThemeConstants.OnCreamPrimary)
            Button(
                onClick = onSelect,
                enabled = inStock,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ThemeConstants.ChocolateSecondary,
                    contentColor = Color.White,
                    disabledContainerColor = ThemeConstants.BorderMedium,
                    disabledContentColor = ThemeConstants.OnCreamPrimary.copy(alpha = 0.75f),
                ),
            ) {
                Text("Seleccionar")
            }
        }
    }
}

@Composable
private fun AtelierSelector(
    label: String,
    values: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, fontWeight = FontWeight.SemiBold, color = ThemeConstants.ChocolateSecondary)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(values.size) { index ->
                val value = values[index]
                Card(
                    modifier = Modifier.shadow(4.dp, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = if (value == selected) ThemeConstants.ChocolateSecondary else ThemeConstants.SurfaceLight,
                    ),
                    onClick = { onSelected(value) },
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(ThemeConstants.PastelAccent, RoundedCornerShape(50))
                        )
                        Text(
                            value.replaceFirstChar { it.uppercaseChar() },
                            color = if (value == selected) Color.White else ThemeConstants.ChocolateSecondary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CheckoutBottomSheet(
    sheetState: androidx.compose.material3.SheetState,
    orderId: Int?,
    onDismiss: () -> Unit,
    onPay: (Int, String, String, String, String) -> Unit,
) {
    var cardNumber by rememberSaveable { mutableStateOf("") }
    var holderName by rememberSaveable { mutableStateOf("") }
    var cvv by rememberSaveable { mutableStateOf("") }
    var expiry by rememberSaveable { mutableStateOf("") }

    val maskedCard = remember(cardNumber) { maskCardNumber(cardNumber) }
    val maskedExp = remember(expiry) { maskExpiry(expiry) }
    val cardValid = maskedCard.filter { it.isDigit() }.length == 16
    val holderValid = holderName.trim().length >= 3
    val cvvValid = cvv.length in 3..4
    val expValid = maskedExp.length == 5

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Checkout", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = maskedCard,
                onValueChange = { cardNumber = it.filter(Char::isDigit).take(16) },
                label = { Text("Número") },
                isError = !cardValid && maskedCard.isNotBlank(),
                supportingText = { if (!cardValid && maskedCard.isNotBlank()) Text("Formato 16 dígitos") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
            )
            OutlinedTextField(
                value = cvv,
                onValueChange = { cvv = it.filter(Char::isDigit).take(4) },
                label = { Text("CVV") },
                isError = !cvvValid && cvv.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
            )
            OutlinedTextField(
                value = holderName,
                onValueChange = { holderName = it },
                label = { Text("Titular") },
                isError = !holderValid && holderName.isNotBlank(),
                supportingText = { if (!holderValid && holderName.isNotBlank()) Text("Mínimo 3 caracteres") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
            )
            OutlinedTextField(
                value = maskedExp,
                onValueChange = { expiry = it.filter(Char::isDigit).take(4) },
                label = { Text("Exp MM/YY") },
                isError = !expValid && maskedExp.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
            )
            Button(
                onClick = {
                    if (orderId != null) {
                        onPay(orderId, maskedCard, holderName.trim(), cvv, maskedExp)
                        onDismiss()
                    }
                },
                enabled = orderId != null && cardValid && holderValid && cvvValid && expValid,
            ) {
                Text("Pagar")
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun OrderStatusStepper(currentStatus: String) {
    val stages = listOf("created", "in_oven", "decorating", "on_the_way", "delivered")
    val labels = listOf(
        "Confirmado",
        "En horno",
        "Decorando",
        "En camino",
        "Entregado",
    )
    val current = stages.indexOf(currentStatus).coerceAtLeast(0)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        labels.forEachIndexed { index, label ->
            val active = index <= current
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            if (active) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(50)
                        )
                )
                Text(label, color = if (active) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun orderStatusLabel(status: String): String {
    return when (status) {
        "created" -> "Confirmado"
        "in_oven" -> "En horno"
        "decorating" -> "Decorando"
        "on_the_way" -> "En camino"
        "delivered" -> "Entregado"
        else -> status
    }
}

private fun maskCardNumber(raw: String): String {
    return raw.filter { it.isDigit() }
        .chunked(4)
        .joinToString(" ")
        .take(19)
}

private fun maskExpiry(raw: String): String {
    val digits = raw.filter { it.isDigit() }.take(4)
    return if (digits.length <= 2) digits else "${digits.take(2)}/${digits.drop(2)}"
}
