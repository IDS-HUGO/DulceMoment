package com.example.dulcemoment.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import coil.compose.AsyncImage
import com.example.dulcemoment.data.local.OrderWithDetails
import com.example.dulcemoment.data.local.ProductWithOptions
import com.example.dulcemoment.data.repo.AdminOrderSummary
import com.example.dulcemoment.domain.sellerNextOrderStatuses
import com.example.dulcemoment.ui.theme.ThemeConstants

private enum class SellerSection {
    Summary,
    Create,
    Inventory,
    Account,
}

@Composable
fun SellerModuleScreen(
    products: List<ProductWithOptions>,
    stockState: Map<Int, Boolean>,
    orders: List<OrderWithDetails>,
    adminSummary: AdminOrderSummary?,
    onPublishProduct: (String, String, Double, Int, Uri?) -> Unit,
    onEditProduct: (Int, String, String, Double, Int) -> Unit,
    onDeleteProduct: (Int) -> Unit,
    onLoadSummary: (String) -> Unit,
    onToggleOutOfStock: (Int, Boolean) -> Unit,
    onToggleAvailability: (Int, Boolean) -> Unit,
    onStageUpdate: (Int, String) -> Unit,
    onOpenOrder: (Int) -> Unit,
    onLogout: () -> Unit,
    onRefresh: () -> Unit,
) {
    var selectedSection by rememberSaveable { mutableStateOf(SellerSection.Summary) }
    var name by rememberSaveable { mutableStateOf("Nuevo pastel") }
    var description by rememberSaveable { mutableStateOf("Pastel artesanal") }
    var basePrice by rememberSaveable { mutableStateOf("350") }
    var stock by rememberSaveable { mutableStateOf("8") }
    var selectedImageUri by rememberSaveable { mutableStateOf<String?>(null) }
    var editingProductId by rememberSaveable { mutableStateOf<Int?>(null) }
    var selectedPeriod by rememberSaveable { mutableStateOf("day") }
    var orderSearch by rememberSaveable { mutableStateOf("") }
    var selectedStatusFilter by rememberSaveable { mutableStateOf("all") }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = ThemeConstants.OnCreamPrimary,
        unfocusedTextColor = ThemeConstants.OnCreamPrimary,
        focusedContainerColor = ThemeConstants.SurfaceLight,
        unfocusedContainerColor = ThemeConstants.SurfaceLighter,
        focusedBorderColor = ThemeConstants.ChocolateSecondary,
        unfocusedBorderColor = ThemeConstants.BorderLight,
        cursorColor = ThemeConstants.ChocolateSecondary,
        focusedLabelColor = ThemeConstants.ChocolateSecondary,
        unfocusedLabelColor = ThemeConstants.TextMedium,
        focusedPlaceholderColor = ThemeConstants.TextMedium,
        unfocusedPlaceholderColor = ThemeConstants.TextMedium,
    )

    val primaryButtonColors = ButtonDefaults.buttonColors(
        containerColor = ThemeConstants.ChocolateSecondary,
        contentColor = androidx.compose.ui.graphics.Color.White,
        disabledContainerColor = ThemeConstants.BorderMedium,
        disabledContentColor = ThemeConstants.OnCreamPrimary.copy(alpha = 0.75f),
    )

    val accentTonalColors = ButtonDefaults.filledTonalButtonColors(
        containerColor = ThemeConstants.PastelAccent,
        contentColor = ThemeConstants.ChocolateSecondary,
        disabledContainerColor = ThemeConstants.SurfaceLighter,
        disabledContentColor = ThemeConstants.TextMedium,
    )

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri.toString()
        }
    }

    LaunchedEffect(Unit) {
        onLoadSummary(selectedPeriod)
    }

    Scaffold(
        containerColor = ThemeConstants.CreamPrimary,
        bottomBar = {
            NavigationBar(containerColor = ThemeConstants.SurfaceLight) {
                NavigationBarItem(
                    selected = selectedSection == SellerSection.Summary,
                    onClick = {
                        selectedSection = SellerSection.Summary
                        onLoadSummary(selectedPeriod)
                    },
                    icon = {
                        val selected = selectedSection == SellerSection.Summary
                        val scale by animateFloatAsState(
                            targetValue = if (selected) 1.15f else 1f,
                            animationSpec = tween(durationMillis = 220),
                            label = "summary_icon_scale",
                        )
                        Icon(
                            imageVector = Icons.Filled.Home,
                            contentDescription = "Resumen",
                            tint = if (selected) ThemeConstants.ChocolateSecondary else ThemeConstants.TextMedium,
                            modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale),
                        )
                    },
                    label = { Text("Resumen") },
                )
                NavigationBarItem(
                    selected = selectedSection == SellerSection.Create,
                    onClick = { selectedSection = SellerSection.Create },
                    icon = {
                        val selected = selectedSection == SellerSection.Create
                        val scale by animateFloatAsState(
                            targetValue = if (selected) 1.15f else 1f,
                            animationSpec = tween(durationMillis = 220),
                            label = "create_icon_scale",
                        )
                        Icon(
                            imageVector = Icons.Filled.AddCircle,
                            contentDescription = "Crear",
                            tint = if (selected) ThemeConstants.ChocolateSecondary else ThemeConstants.TextMedium,
                            modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale),
                        )
                    },
                    label = { Text("Crear") },
                )
                NavigationBarItem(
                    selected = selectedSection == SellerSection.Inventory,
                    onClick = { selectedSection = SellerSection.Inventory },
                    icon = {
                        val selected = selectedSection == SellerSection.Inventory
                        val scale by animateFloatAsState(
                            targetValue = if (selected) 1.15f else 1f,
                            animationSpec = tween(durationMillis = 220),
                            label = "inventory_icon_scale",
                        )
                        Icon(
                            imageVector = Icons.Filled.ShoppingCart,
                            contentDescription = "Inventario",
                            tint = if (selected) ThemeConstants.ChocolateSecondary else ThemeConstants.TextMedium,
                            modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale),
                        )
                    },
                    label = { Text("Inventario") },
                )
                NavigationBarItem(
                    selected = selectedSection == SellerSection.Account,
                    onClick = { selectedSection = SellerSection.Account },
                    icon = {
                        val selected = selectedSection == SellerSection.Account
                        val scale by animateFloatAsState(
                            targetValue = if (selected) 1.15f else 1f,
                            animationSpec = tween(durationMillis = 220),
                            label = "account_icon_scale",
                        )
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = "Cuenta",
                            tint = if (selected) ThemeConstants.ChocolateSecondary else ThemeConstants.TextMedium,
                            modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale),
                        )
                    },
                    label = { Text("Cuenta") },
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .background(ThemeConstants.CreamPrimary)
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        "Panel del vendedor",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = ThemeConstants.ChocolateSecondary,
                    )
                    Button(
                        onClick = onRefresh,
                        modifier = Modifier.fillMaxWidth(),
                        colors = primaryButtonColors,
                    ) {
                        Text("Actualizar información", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            when (selectedSection) {
                SellerSection.Summary -> {
                    item {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                "Resumen de pedidos",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = ThemeConstants.ChocolateSecondary,
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                listOf("day" to "Día", "week" to "Semana", "month" to "Mes").forEach { (period, label) ->
                                    val selected = selectedPeriod == period
                                    FilledTonalButton(
                                        onClick = {
                                            selectedPeriod = period
                                            onLoadSummary(period)
                                        },
                                        colors = if (selected) {
                                            accentTonalColors
                                        } else {
                                            ButtonDefaults.filledTonalButtonColors(
                                                containerColor = ThemeConstants.SurfaceLighter,
                                                contentColor = ThemeConstants.OnCreamPrimary,
                                            )
                                        },
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        Text(label, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                            Card(
                                colors = CardDefaults.cardColors(containerColor = ThemeConstants.SurfaceLight),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Text(
                                        "Pedidos: ${adminSummary?.totalOrders ?: 0}",
                                        color = ThemeConstants.ChocolateSecondary,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Text(
                                        "Ventas: $${"%.2f".format(adminSummary?.totalSales ?: 0.0)}",
                                        color = ThemeConstants.ChocolateSecondary,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    val breakdown = adminSummary?.statusBreakdown.orEmpty()
                                    if (breakdown.isEmpty()) {
                                        Text("Sin pedidos en el período", color = ThemeConstants.TextMedium)
                                    } else {
                                        breakdown.forEach { (status, count) ->
                                            Text("${orderStatusLabel(status)}: $count", color = ThemeConstants.OnCreamPrimary)
                                        }
                                    }
                                }
                            }
                            Text(
                                "Pedidos activos: ${orders.count { it.order.status != "delivered" }}",
                                color = ThemeConstants.TextMedium,
                            )

                            Card(
                                colors = CardDefaults.cardColors(containerColor = ThemeConstants.SurfaceLight),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Text(
                                        "Gestión de pedidos",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ThemeConstants.ChocolateSecondary,
                                    )

                                    if (orders.isEmpty()) {
                                        Text("No hay pedidos por gestionar.", color = ThemeConstants.TextMedium)
                                    } else {
                                        OutlinedTextField(
                                            value = orderSearch,
                                            onValueChange = { orderSearch = it },
                                            label = { Text("Buscar por # pedido") },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = fieldColors,
                                        )

                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                            statusFilterChip(
                                                selected = selectedStatusFilter == "all",
                                                label = "Todos",
                                                onClick = { selectedStatusFilter = "all" },
                                                accentTonalColors = accentTonalColors,
                                            )
                                            statusFilterChip(
                                                selected = selectedStatusFilter == "created",
                                                label = "Confirmado",
                                                onClick = { selectedStatusFilter = "created" },
                                                accentTonalColors = accentTonalColors,
                                            )
                                            statusFilterChip(
                                                selected = selectedStatusFilter == "in_oven",
                                                label = "En horno",
                                                onClick = { selectedStatusFilter = "in_oven" },
                                                accentTonalColors = accentTonalColors,
                                            )
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                            statusFilterChip(
                                                selected = selectedStatusFilter == "decorating",
                                                label = "Decorando",
                                                onClick = { selectedStatusFilter = "decorating" },
                                                accentTonalColors = accentTonalColors,
                                            )
                                            statusFilterChip(
                                                selected = selectedStatusFilter == "on_the_way",
                                                label = "En camino",
                                                onClick = { selectedStatusFilter = "on_the_way" },
                                                accentTonalColors = accentTonalColors,
                                            )
                                            statusFilterChip(
                                                selected = selectedStatusFilter == "delivered",
                                                label = "Entregado",
                                                onClick = { selectedStatusFilter = "delivered" },
                                                accentTonalColors = accentTonalColors,
                                            )
                                        }

                                        val normalizedQuery = orderSearch.trim()
                                        val filteredOrders = orders
                                            .filter { order ->
                                                selectedStatusFilter == "all" || order.order.status == selectedStatusFilter
                                            }
                                            .filter { order ->
                                                normalizedQuery.isBlank() || order.order.id.toString().contains(normalizedQuery)
                                            }
                                            .sortedByDescending { it.order.createdAt }
                                        if (filteredOrders.isEmpty()) {
                                            Text("No hay pedidos que coincidan con el filtro.", color = ThemeConstants.TextMedium)
                                        }
                                        filteredOrders.forEach { orderDetail ->
                                                Card(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    colors = CardDefaults.cardColors(containerColor = ThemeConstants.SurfaceLighter),
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(10.dp),
                                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                                    ) {
                                                        Text(
                                                            "Pedido #${orderDetail.order.id}",
                                                            color = ThemeConstants.ChocolateSecondary,
                                                            fontWeight = FontWeight.SemiBold,
                                                        )
                                                        Text(
                                                            "Estado: ${orderStatusLabel(orderDetail.order.status)}",
                                                            color = ThemeConstants.OnCreamPrimary,
                                                        )
                                                        Text(
                                                            "Total: $${"%.2f".format(orderDetail.order.total)}",
                                                            color = ThemeConstants.TextMedium,
                                                        )

                                                        val nextStatuses = sellerNextOrderStatuses(orderDetail.order.status)
                                                        if (nextStatuses.isEmpty()) {
                                                            Text("Pedido finalizado", color = ThemeConstants.TextMedium)
                                                        } else {
                                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                                nextStatuses.forEach { (statusValue, label) ->
                                                                    FilledTonalButton(
                                                                        onClick = { onStageUpdate(orderDetail.order.id, statusValue) },
                                                                        colors = accentTonalColors,
                                                                    ) {
                                                                        Text(label, fontWeight = FontWeight.SemiBold)
                                                                    }
                                                                }
                                                            }
                                                        }

                                                        Button(
                                                            onClick = { onOpenOrder(orderDetail.order.id) },
                                                            colors = primaryButtonColors,
                                                        ) {
                                                            Text("Ver detalle", fontWeight = FontWeight.SemiBold)
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

                SellerSection.Create -> {
                    item {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                if (editingProductId == null) "Alta de producto" else "Editar producto #$editingProductId",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = ThemeConstants.ChocolateSecondary,
                            )
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Nombre") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = fieldColors,
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = ThemeConstants.OnCreamPrimary),
                            )
                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                label = { Text("Descripción") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = fieldColors,
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = ThemeConstants.OnCreamPrimary),
                            )
                            OutlinedTextField(
                                value = basePrice,
                                onValueChange = { basePrice = it },
                                label = { Text("Precio base") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = fieldColors,
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = ThemeConstants.OnCreamPrimary),
                            )
                            OutlinedTextField(
                                value = stock,
                                onValueChange = { stock = it },
                                label = { Text("Stock") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = fieldColors,
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = ThemeConstants.OnCreamPrimary),
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                            ) {
                                val uriString = selectedImageUri
                                if (uriString != null) {
                                    AsyncImage(
                                        model = Uri.parse(uriString),
                                        contentDescription = "Imagen seleccionada",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(140.dp),
                                        contentScale = ContentScale.Crop,
                                    )
                                } else {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(140.dp),
                                        colors = CardDefaults.cardColors(containerColor = ThemeConstants.SurfaceLight),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(140.dp),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text("Sin imagen seleccionada", color = ThemeConstants.TextMedium)
                                        }
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Button(
                                    onClick = { picker.launch("image/*") },
                                    modifier = Modifier.weight(1f),
                                    colors = primaryButtonColors,
                                ) {
                                    Text("Seleccionar", maxLines = 1, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                                FilledTonalButton(
                                    onClick = { selectedImageUri = null },
                                    modifier = Modifier.weight(1f),
                                    enabled = selectedImageUri != null,
                                    colors = accentTonalColors,
                                ) {
                                    Text("Quitar", maxLines = 1, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            Text("La imagen se sube automáticamente al publicar.", color = ThemeConstants.TextMedium)
                            Button(
                                onClick = {
                                    val price = basePrice.toDoubleOrNull() ?: return@Button
                                    val units = stock.toIntOrNull() ?: return@Button
                                    val editId = editingProductId
                                    if (editId == null) {
                                        onPublishProduct(name, description, price, units, selectedImageUri?.let(Uri::parse))
                                    } else {
                                        onEditProduct(editId, name, description, price, units)
                                    }
                                },
                                enabled = selectedImageUri != null || editingProductId != null,
                                modifier = Modifier.fillMaxWidth(),
                                colors = primaryButtonColors,
                            ) {
                                Text(if (editingProductId == null) "Publicar producto" else "Guardar cambios", fontWeight = FontWeight.SemiBold)
                            }
                            if (editingProductId != null) {
                                FilledTonalButton(
                                    onClick = {
                                        editingProductId = null
                                        selectedImageUri = null
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = accentTonalColors,
                                ) {
                                    Text("Cancelar edición", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }

                SellerSection.Inventory -> {
                    item {
                        Text(
                            "Gestión de inventario",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = ThemeConstants.ChocolateSecondary,
                            modifier = Modifier.padding(horizontal = 12.dp),
                        )
                    }

                    items(products) { product ->
                        val inStock = stockState[product.product.id] ?: (product.product.stock > 0)
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp)
                                .shadow(4.dp, RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = ThemeConstants.SurfaceLight),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                            shape = RoundedCornerShape(24.dp),
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(product.product.name, fontWeight = FontWeight.SemiBold, color = ThemeConstants.ChocolateSecondary)
                                Text("Stock: ${product.product.stock}", color = ThemeConstants.TextMedium)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        if (inStock) "Disponible" else "AGOTADO",
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (inStock) ThemeConstants.ChocolateSecondary else ThemeConstants.ErrorRed,
                                    )
                                    Switch(
                                        checked = !inStock,
                                        onCheckedChange = { isOut -> onToggleOutOfStock(product.product.id, isOut) },
                                    )
                                }
                                Button(
                                    onClick = { onToggleAvailability(product.product.id, !product.product.isActive) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (product.product.isActive) ThemeConstants.ChocolateSecondary else ThemeConstants.PastelAccent,
                                        contentColor = if (product.product.isActive) androidx.compose.ui.graphics.Color.White else ThemeConstants.ChocolateSecondary,
                                    ),
                                ) {
                                    Text(if (product.product.isActive) "Ocultar" else "Activar", fontWeight = FontWeight.SemiBold)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilledTonalButton(
                                        onClick = {
                                            editingProductId = product.product.id
                                            name = product.product.name
                                            description = product.product.description
                                            basePrice = product.product.basePrice.toString()
                                            stock = product.product.stock.toString()
                                            selectedImageUri = null
                                            selectedSection = SellerSection.Create
                                        },
                                        colors = accentTonalColors,
                                    ) {
                                        Text("Editar", fontWeight = FontWeight.SemiBold)
                                    }
                                    Button(
                                        onClick = { onDeleteProduct(product.product.id) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ThemeConstants.ErrorRed,
                                            contentColor = androidx.compose.ui.graphics.Color.White,
                                        ),
                                    ) {
                                        Text("Eliminar", fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }

                SellerSection.Account -> {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            colors = CardDefaults.cardColors(containerColor = ThemeConstants.SurfaceLight),
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Text(
                                    "Cuenta del vendedor",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ThemeConstants.ChocolateSecondary,
                                )
                                Text("Gestiona tu sesión desde aquí.", color = ThemeConstants.TextMedium)
                                Button(
                                    onClick = onRefresh,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = primaryButtonColors,
                                ) {
                                    Text("Actualizar datos", fontWeight = FontWeight.SemiBold)
                                }
                                Button(
                                    onClick = onLogout,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ThemeConstants.ErrorRed,
                                        contentColor = androidx.compose.ui.graphics.Color.White,
                                    ),
                                ) {
                                    Text("Cerrar sesión", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
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

@Composable
private fun RowScope.statusFilterChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    accentTonalColors: androidx.compose.material3.ButtonColors,
) {
    FilledTonalButton(
        onClick = onClick,
        colors = if (selected) {
            accentTonalColors
        } else {
            ButtonDefaults.filledTonalButtonColors(
                containerColor = ThemeConstants.SurfaceLighter,
                contentColor = ThemeConstants.OnCreamPrimary,
            )
        },
        modifier = Modifier.weight(1f),
    ) {
        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
    }
}
