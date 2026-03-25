package com.example.dulcemoment.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dulcemoment.data.local.OrderWithDetails
import com.example.dulcemoment.data.local.ProductWithOptions
import com.example.dulcemoment.ui.theme.ThemeConstants

@Composable
fun SellerModuleScreen(
    products: List<ProductWithOptions>,
    stockState: Map<Int, Boolean>,
    orders: List<OrderWithDetails>,
    suggestedImageUrl: String,
    onUploadImageFile: (Uri) -> Unit,
    onUploadImageUrl: (String) -> Unit,
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
    var imageSourceUrl by rememberSaveable { mutableStateOf("") }
    var imageUrl by rememberSaveable { mutableStateOf("") }
    var selectedImageUri by rememberSaveable { mutableStateOf<String?>(null) }

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

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri.toString()
        }
    }

    LaunchedEffect(suggestedImageUrl) {
        if (suggestedImageUrl.isNotBlank()) {
            imageUrl = suggestedImageUrl
        }
    }

    LazyColumn(
        modifier = Modifier.background(ThemeConstants.CreamPrimary),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Panel administrativo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ThemeConstants.ChocolateSecondary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilledTonalButton(
                        onClick = onLogout,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = ThemeConstants.PastelAccent
                        )
                    ) {
                        Text("Cerrar sesión", maxLines = 1, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = onRefresh,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ThemeConstants.ChocolateSecondary
                        )
                    ) {
                        Text("Actualizar", maxLines = 1, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
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
                    "Alta de producto",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = ThemeConstants.ChocolateSecondary
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors,
                    textStyle = MaterialTheme.typography.bodyLarge,
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors,
                    textStyle = MaterialTheme.typography.bodyLarge,
                )
                OutlinedTextField(
                    value = basePrice,
                    onValueChange = { basePrice = it },
                    label = { Text("Precio base") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors,
                    textStyle = MaterialTheme.typography.bodyLarge,
                )
                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it },
                    label = { Text("Stock") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors,
                    textStyle = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = "Imagen del producto",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = ThemeConstants.ChocolateSecondary,
                )
                OutlinedTextField(
                    value = imageSourceUrl,
                    onValueChange = { imageSourceUrl = it },
                    label = { Text("URL de imagen (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    singleLine = true,
                )
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)) {
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
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Sin imagen seleccionada",
                                    color = ThemeConstants.TextMedium
                                )
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
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ThemeConstants.ChocolateSecondary
                        )
                    ) {
                        Text("Seleccionar imagen", maxLines = 1, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                    FilledTonalButton(
                        onClick = { selectedImageUri?.let { onUploadImageFile(Uri.parse(it)) } },
                        modifier = Modifier.weight(1f),
                        enabled = selectedImageUri != null,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = ThemeConstants.PastelAccent
                        )
                    ) {
                        Text("Subir", maxLines = 1, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                FilledTonalButton(
                    onClick = { onUploadImageUrl(imageSourceUrl.trim()) },
                    enabled = imageSourceUrl.startsWith("http", ignoreCase = true),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = ThemeConstants.PastelAccent,
                        contentColor = ThemeConstants.ChocolateSecondary,
                    )
                ) {
                    Text("Usar URL directa", fontWeight = FontWeight.SemiBold)
                }
                if (imageUrl.isNotBlank()) {
                    Text(
                        "✓ Imagen lista para publicar",
                        color = ThemeConstants.ChocolateSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Text(
                        "Selecciona una imagen y súbela",
                        color = ThemeConstants.TextMedium
                    )
                }
                Button(
                    onClick = {
                        val price = basePrice.toDoubleOrNull() ?: return@Button
                        val units = stock.toIntOrNull() ?: return@Button
                        onAddProduct(name, description, price, units, imageUrl)
                    },
                    enabled = imageUrl.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ThemeConstants.ChocolateSecondary
                    )
                ) {
                    Text("Publicar producto", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        item {
            Text(
                "Gestión de inventario",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = ThemeConstants.ChocolateSecondary,
                modifier = Modifier.padding(horizontal = 12.dp)
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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        product.product.name,
                        fontWeight = FontWeight.SemiBold,
                        color = ThemeConstants.ChocolateSecondary
                    )
                    Text(
                        "Stock: ${product.product.stock}",
                        color = ThemeConstants.TextMedium
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (inStock) "✓ Disponible" else "⚠ AGOTADO",
                            fontWeight = FontWeight.SemiBold,
                            color = if (inStock) ThemeConstants.ChocolateSecondary else ThemeConstants.ErrorRed
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
                            contentColor = if (product.product.isActive) androidx.compose.ui.graphics.Color.White else ThemeConstants.ChocolateSecondary
                        )
                    ) {
                        Text(if (product.product.isActive) "Ocultar" else "Activar", fontWeight = FontWeight.SemiBold)
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
