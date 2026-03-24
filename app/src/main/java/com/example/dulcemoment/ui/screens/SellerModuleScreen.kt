package com.example.dulcemoment.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dulcemoment.data.local.OrderWithDetails
import com.example.dulcemoment.data.local.ProductWithOptions

@Composable
fun SellerModuleScreen(
    products: List<ProductWithOptions>,
    stockState: Map<Int, Boolean>,
    orders: List<OrderWithDetails>,
    suggestedImageUrl: String,
    onUploadImage: (String) -> Unit,
    onAddProduct: (String, String, Double, Int, String) -> Unit,
    onToggleOutOfStock: (Int, Boolean) -> Unit,
    onToggleAvailability: (Int, Boolean) -> Unit,
    onStageUpdate: (Int, String) -> Unit,
    onOpenOrder: (Int) -> Unit,
    onLogout: () -> Unit,
    onLogoutAll: () -> Unit,
    onRefresh: () -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("Nuevo pastel") }
    var description by rememberSaveable { mutableStateOf("Pastel artesanal") }
    var basePrice by rememberSaveable { mutableStateOf("350") }
    var stock by rememberSaveable { mutableStateOf("8") }
    var imageUrl by rememberSaveable { mutableStateOf("") }
    var sourceUrl by rememberSaveable { mutableStateOf("") }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            imageUrl = uri.toString()
        }
    }

    LaunchedEffect(suggestedImageUrl) {
        if (suggestedImageUrl.isNotBlank()) {
            imageUrl = suggestedImageUrl
        }
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("Panel administrativo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onLogout) { Text("Cerrar sesión") }
                Button(onClick = onLogoutAll) { Text("Cerrar en todos") }
                Button(onClick = onRefresh) { Text("Actualizar") }
            }
        }

        item {
            Text("Alta de producto", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
            )
            OutlinedTextField(
                value = basePrice,
                onValueChange = { basePrice = it },
                label = { Text("Precio base") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
            )
            OutlinedTextField(
                value = stock,
                onValueChange = { stock = it },
                label = { Text("Stock") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
            )
            OutlinedTextField(
                value = sourceUrl,
                onValueChange = { sourceUrl = it },
                label = { Text("URL para Cloudinary") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { picker.launch("image/*") }) { Text("Picker galería") }
                Button(onClick = { if (sourceUrl.isNotBlank()) onUploadImage(sourceUrl) }) { Text("Subir URL") }
            }
            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = { Text("Imagen seleccionada") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    val price = basePrice.toDoubleOrNull() ?: return@Button
                    val units = stock.toIntOrNull() ?: return@Button
                    onAddProduct(name, description, price, units, imageUrl)
                }
            ) {
                Text("Publicar producto")
            }
        }

        item {
            Text("Gestión de inventario", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }

        items(products) { product ->
            val inStock = stockState[product.product.id] ?: (product.product.stock > 0)
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(24.dp),
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(product.product.name, fontWeight = FontWeight.SemiBold)
                    Text("Stock: ${product.product.stock}")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(if (inStock) "Disponible" else "AGOTADO")
                        Switch(
                            checked = !inStock,
                            onCheckedChange = { isOut -> onToggleOutOfStock(product.product.id, isOut) },
                        )
                    }
                    Button(onClick = { onToggleAvailability(product.product.id, !product.product.isActive) }) {
                        Text(if (product.product.isActive) "Ocultar" else "Activar")
                    }
                }
            }
        }

        item {
            Text("Control de pedidos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            orders.forEach { order ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(24.dp),
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Pedido #${order.order.id} - ${order.order.status}")
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(onClick = { onStageUpdate(order.order.id, "in_oven") }) { Text("ESTADO_HORNO") }
                            Button(onClick = { onStageUpdate(order.order.id, "decorating") }) { Text("ESTADO_DECORACION") }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(onClick = { onStageUpdate(order.order.id, "on_the_way") }) { Text("ESTADO_TRANSITO") }
                            Button(onClick = { onStageUpdate(order.order.id, "delivered") }) { Text("ESTADO_ENTREGADO") }
                        }
                        Button(onClick = { onOpenOrder(order.order.id) }) { Text("Ver detalle") }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}
