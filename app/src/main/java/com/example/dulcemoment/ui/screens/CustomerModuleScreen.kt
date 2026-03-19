package com.example.dulcemoment.ui.screens

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerModuleScreen(
    products: List<ProductWithOptions>,
    stockState: Map<Int, Boolean>,
    orders: List<OrderWithDetails>,
    alerts: List<String>,
    onCreateOrder: (Int, Int, String, String, String, String, String, String, String) -> Unit,
    onPay: (Int, String, String) -> Unit,
    onOpenOrder: (Int) -> Unit,
    onLogout: () -> Unit,
    onLogoutAll: () -> Unit,
    onRefresh: () -> Unit,
) {
    var selectedProductId by rememberSaveable { mutableStateOf(0) }
    var quantity by rememberSaveable { mutableStateOf(1) }
    var selectedShape by rememberSaveable { mutableStateOf("redondo") }
    var selectedFlavor by rememberSaveable { mutableStateOf("chocolate") }
    var selectedColor by rememberSaveable { mutableStateOf("rosa") }
    var ingredients by rememberSaveable { mutableStateOf("fresa,oreo") }
    var address by rememberSaveable { mutableStateOf("Av. Principal 123") }
    var notes by rememberSaveable { mutableStateOf("") }
    var showCheckoutSheet by rememberSaveable { mutableStateOf(false) }

    val selectedProduct = products.firstOrNull { it.product.id == selectedProductId }
    val dynamicPrice by remember(selectedProduct, quantity, selectedShape, selectedFlavor, selectedColor, ingredients) {
        derivedStateOf {
            selectedProduct?.let { product ->
                val options = product.options
                val selectionDelta = listOf(
                    "shape" to selectedShape,
                    "flavor" to selectedFlavor,
                    "color" to selectedColor,
                ).sumOf { (category, value) ->
                    options.firstOrNull { it.category == category && it.value.equals(value, ignoreCase = true) }?.priceDelta ?: 0.0
                }
                val ingredientDelta = ingredients
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .sumOf { selectedIngredient ->
                        options.firstOrNull {
                            it.category == "ingredient" && it.value.equals(selectedIngredient, ignoreCase = true)
                        }?.priceDelta ?: 0.0
                    }
                (product.product.basePrice + selectionDelta + ingredientDelta) * quantity
            } ?: 0.0
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showCheckoutSheet) {
        CheckoutBottomSheet(
            sheetState = sheetState,
            orderId = orders.firstOrNull()?.order?.id,
            onDismiss = { showCheckoutSheet = false },
            onPay = onPay,
        )
    }

    LazyColumn(
        modifier = Modifier.padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text("Catálogo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onLogout) { Text("Cerrar sesión") }
                Button(onClick = onLogoutAll) { Text("Cerrar en todos") }
                Button(onClick = onRefresh) { Text("Actualizar") }
            }
        }

        item {
            val gridRows = ((products.size + 1) / 2).coerceAtLeast(1)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height((gridRows * 220).dp),
                userScrollEnabled = false,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(products) { product ->
                    val inStock = stockState[product.product.id] ?: (product.product.stock > 0)
                    ProductCatalogCard(
                        product = product,
                        inStock = inStock,
                        selected = selectedProductId == product.product.id,
                        onSelect = { selectedProductId = product.product.id },
                    )
                }
            }
        }

        item {
            Text("El Atelier", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
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
            )
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Dirección") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            )
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notas") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = { if (quantity > 1) quantity-- }) { Text("-") }
                Text("Cantidad: $quantity")
                Button(onClick = { quantity++ }) { Text("+") }
            }
            Text("Precio dinámico: ${"%.2f".format(dynamicPrice)}")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        if (selectedProduct != null) {
                            onCreateOrder(
                                selectedProduct.product.id,
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
                    enabled = selectedProduct != null && (stockState[selectedProduct.product.id] ?: true),
                ) {
                    Text("Crear pedido")
                }
                Button(
                    onClick = { showCheckoutSheet = true },
                    enabled = orders.isNotEmpty(),
                ) {
                    Text("Checkout")
                }
            }
        }

        item {
            Text("Mis pedidos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            orders.take(4).forEach { order ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Pedido #${order.order.id} - ${order.order.status}")
                        OrderStatusStepper(currentStatus = order.order.status)
                        Button(onClick = { onOpenOrder(order.order.id) }) { Text("Ver detalle") }
                    }
                }
            }
        }

        item {
            Text("Push Alerts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            alerts.take(6).forEach { Text("• $it") }
        }
    }
}

@Composable
private fun ProductCatalogCard(
    product: ProductWithOptions,
    inStock: Boolean,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier
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
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
            ) {
                Badge {
                    Text("AGOTADO")
                }
            }
        }

        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(product.product.name, fontWeight = FontWeight.SemiBold)
            Text("$${"%.2f".format(product.product.basePrice)}")
            Button(onClick = onSelect, enabled = inStock) { Text("Seleccionar") }
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
        Text(label, fontWeight = FontWeight.SemiBold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(values.size) { index ->
                val value = values[index]
                Card(
                    modifier = Modifier.shadow(4.dp, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = if (value == selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
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
                                .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(50))
                        )
                        Text(value.replaceFirstChar { it.uppercaseChar() })
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
    onPay: (Int, String, String) -> Unit,
) {
    var cardNumber by rememberSaveable { mutableStateOf("") }
    var cvv by rememberSaveable { mutableStateOf("") }
    var expiry by rememberSaveable { mutableStateOf("") }

    val maskedCard = remember(cardNumber) { maskCardNumber(cardNumber) }
    val maskedExp = remember(expiry) { maskExpiry(expiry) }
    val cardValid = maskedCard.filter { it.isDigit() }.length == 16
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
                        onPay(orderId, maskedCard, "Cliente")
                        onDismiss()
                    }
                },
                enabled = orderId != null && cardValid && cvvValid && expValid,
            ) {
                Text("Pagar")
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun OrderStatusStepper(currentStatus: String) {
    val stages = listOf("in_oven", "decorating", "on_the_way", "delivered")
    val labels = listOf(
        "ESTADO_HORNO",
        "ESTADO_DECORACION",
        "ESTADO_TRANSITO",
        "ESTADO_ENTREGADO",
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
                Text(label)
            }
        }
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
