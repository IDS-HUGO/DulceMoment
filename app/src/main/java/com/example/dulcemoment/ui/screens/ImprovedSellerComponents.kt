package com.example.dulcemoment.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dulcemoment.ui.components.PremiumButton
import com.example.dulcemoment.ui.components.PremiumCard
import com.example.dulcemoment.ui.components.StatusBadge

/**
 * Admin Panel Header - Encabezado del panel administrativo
 */
@Composable
fun AdminPanelHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Panel del vendedor",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            "Gestiona tu inventario y pedidos",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Product Form Card - Formulario mejorado para crear productos
 */
@Composable
fun ImprovedProductFormCard(
    name: String,
    description: String,
    basePrice: String,
    stock: String,
    sourceUrl: String,
    imageUrl: String,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onStockChange: (String) -> Unit,
    onSourceUrlChange: (String) -> Unit,
    onPickImage: () -> Unit,
    onUploadUrl: () -> Unit,
    onAddProduct: () -> Unit,
    isLoading: Boolean = false,
) {
    PremiumCard {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                "➕ Nuevo producto",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Name field
            ProductFormField(
                label = "Nombre del producto",
                value = name,
                onValueChange = onNameChange,
                placeholder = "Ej: Torta de chocolate",
                icon = "🍰"
            )

            // Description field
            ProductFormField(
                label = "Descripción",
                value = description,
                onValueChange = onDescriptionChange,
                placeholder = "Describe tu producto...",
                icon = "📝",
                maxLines = 3
            )

            // Price field
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProductFormField(
                    label = "Precio base",
                    value = basePrice,
                    onValueChange = onPriceChange,
                    placeholder = "0.00",
                    modifier = Modifier.weight(1f),
                    icon = "💵"
                )

                ProductFormField(
                    label = "Stock",
                    value = stock,
                    onValueChange = onStockChange,
                    placeholder = "0",
                    modifier = Modifier.weight(1f),
                    icon = "📦"
                )
            }

            // Image section
            Text(
                "🖼️ Imagen del producto",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Image preview
            if (imageUrl.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✓ Imagen cargada", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                        .border(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📷", fontSize = 48.sp)
                }
            }

            // Upload options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PremiumButton(
                    text = "Seleccionar imagen",
                    onClick = onPickImage,
                    modifier = Modifier.weight(1f),
                    icon = "🖼️"
                )
            }

            // URL upload
            ProductFormField(
                label = "O pega URL de imagen",
                value = sourceUrl,
                onValueChange = onSourceUrlChange,
                placeholder = "https://...",
                icon = "🔗"
            )

            if (sourceUrl.isNotEmpty()) {
                PremiumButton(
                    text = "Subir desde URL",
                    onClick = onUploadUrl,
                    icon = "⬆️"
                )
            }

            // Add product button
            Spacer(modifier = Modifier.height(8.dp))
            PremiumButton(
                text = "Crear producto",
                onClick = onAddProduct,
                enabled = name.isNotEmpty() && basePrice.isNotEmpty() && stock.isNotEmpty(),
                isLoading = isLoading,
                icon = "✨"
            )
        }
    }
}

/**
 * Product Form Field - Campo de formulario mejorado
 */
@Composable
fun ProductFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    icon: String = "📝",
    maxLines: Int = 1,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(icon, fontSize = 16.sp)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, style = MaterialTheme.typography.bodySmall) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            maxLines = maxLines,
            minLines = maxLines,
            textStyle = MaterialTheme.typography.bodySmall
        )
    }
}

/**
 * Inventory Status Card - Card de estado del inventario
 */
@Composable
fun InventoryStatusCard(
    productName: String,
    currentStock: Int,
    isOutOfStock: Boolean = false,
    isAvailable: Boolean = true,
    onToggleOutOfStock: () -> Unit,
    onToggleAvailability: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOutOfStock) MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        productName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Stock status
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (currentStock > 0) Icons.Default.CheckCircle else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (currentStock > 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "Stock: $currentStock unidades",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (currentStock > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                        )
                    }
                }

                // Status badges
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    StatusBadge(
                        status = if (isOutOfStock) "Agotado" else "En stock",
                        statusColor = if (isOutOfStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary,
                        icon = if (isOutOfStock) "⚠️" else "✓"
                    )
                    StatusBadge(
                        status = if (isAvailable) "Disponible" else "No disponible",
                        statusColor = if (isAvailable) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                        icon = if (isAvailable) "🟢" else "🔴"
                    )
                }
            }

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Marcar agotado",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Switch(
                            checked = isOutOfStock,
                            onCheckedChange = { onToggleOutOfStock() }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Disponibilidad",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Switch(
                            checked = isAvailable,
                            onCheckedChange = { onToggleAvailability() }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Order Card for Sellers - Card mejorada para órdenes del vendedor
 */
@Composable
fun SellerOrderCard(
    orderId: Int,
    customerName: String,
    orderDate: String,
    status: String,
    totalPrice: String,
    onViewDetails: () -> Unit,
    onMarkProcessing: () -> Unit,
    onMarkInTransit: () -> Unit,
    onMarkDelivered: () -> Unit,
) {
    PremiumCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Order header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Orden #$orderId",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        customerName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                ) {
                    Text(
                        totalPrice,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            // Date and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "📅 $orderDate",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                StatusBadge(
                    status = status,
                    statusColor = when (status.lowercase()) {
                        "pendiente" -> MaterialTheme.colorScheme.error
                        "procesando" -> MaterialTheme.colorScheme.secondary
                        "entregado" -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.outline
                    },
                    icon = when (status.lowercase()) {
                        "pendiente" -> "⏳"
                        "procesando" -> "🔄"
                        "entregado" -> "✓"
                        else -> "❓"
                    }
                )
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PremiumButton(
                    text = "Ver detalles",
                    onClick = onViewDetails,
                    modifier = Modifier.weight(1f),
                    icon = "👁️"
                )
                PremiumButton(
                    text = "Actualizar",
                    onClick = onMarkProcessing,
                    modifier = Modifier.weight(1f),
                    icon = "⬆️"
                )
            }
        }
    }
}
