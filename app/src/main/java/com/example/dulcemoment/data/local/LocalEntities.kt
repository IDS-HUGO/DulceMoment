package com.example.dulcemoment.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val password: String,
    val role: String,
)

@Entity(tableName = "session")
data class SessionEntity(
    @PrimaryKey val id: Int = 1,
    val userId: Int,
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val basePrice: Double,
    val stock: Int,
    val imageUrl: String = "",
    val isActive: Boolean = true,
)

@Entity(
    tableName = "product_options",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("productId"), Index("category")],
)
data class ProductOptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: Int,
    val category: String,
    val value: String,
    val priceDelta: Double,
)

@Entity(tableName = "orders", indices = [Index("customerId")])
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerId: Int,
    val status: String,
    val total: Double,
    val deliveryAddress: String,
    val notes: String,
    val createdAt: Long,
)

@Entity(
    tableName = "order_items",
    foreignKeys = [
        ForeignKey(entity = OrderEntity::class, parentColumns = ["id"], childColumns = ["orderId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = ProductEntity::class, parentColumns = ["id"], childColumns = ["productId"], onDelete = ForeignKey.NO_ACTION),
    ],
    indices = [Index("orderId"), Index("productId")],
)
data class OrderItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val orderId: Int,
    val productId: Int,
    val quantity: Int,
    val unitPrice: Double,
    val ingredients: String,
    val size: String,
    val shape: String,
    val flavor: String,
    val color: String,
)

@Entity(
    tableName = "tracking_events",
    foreignKeys = [
        ForeignKey(entity = OrderEntity::class, parentColumns = ["id"], childColumns = ["orderId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("orderId")],
)
data class TrackingEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val orderId: Int,
    val status: String,
    val message: String,
    val etaMinutes: Int,
    val createdAt: Long,
)

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(entity = OrderEntity::class, parentColumns = ["id"], childColumns = ["orderId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("orderId", unique = true)],
)
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val orderId: Int,
    val amount: Double,
    val status: String,
    val cardLast4: String = "",
    val createdAt: Long,
)

@Entity(tableName = "push_alerts", indices = [Index("userId")])
data class PushAlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val orderId: Int? = null,
    val title: String,
    val body: String,
    val createdAt: Long,
)

data class ProductWithOptions(
    @Embedded val product: ProductEntity,
    @Relation(parentColumn = "id", entityColumn = "productId")
    val options: List<ProductOptionEntity>,
)

data class OrderWithDetails(
    @Embedded val order: OrderEntity,
    @Relation(parentColumn = "id", entityColumn = "orderId")
    val items: List<OrderItemEntity>,
    @Relation(parentColumn = "id", entityColumn = "orderId")
    val events: List<TrackingEventEntity>,
)

data class SessionWithUser(
    @Embedded val session: SessionEntity,
    @Relation(parentColumn = "userId", entityColumn = "id")
    val user: UserEntity,
)
