package com.example.dulcemoment.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DulceDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSession(session: SessionEntity)

    @Query("DELETE FROM session")
    suspend fun clearSession()

    @Transaction
    @Query("SELECT * FROM session WHERE id = 1 LIMIT 1")
    fun observeSession(): Flow<SessionWithUser?>

    @Transaction
    @Query("SELECT * FROM products ORDER BY id DESC")
    fun observeProductsWithOptions(): Flow<List<ProductWithOptions>>

    @Query("SELECT * FROM products WHERE id = :productId LIMIT 1")
    suspend fun findProduct(productId: Int): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOption(option: ProductOptionEntity)

    @Query("SELECT * FROM product_options WHERE productId = :productId AND category = :category AND value = :value LIMIT 1")
    suspend fun findOption(productId: Int, category: String, value: String): ProductOptionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    suspend fun findOrder(orderId: Int): OrderEntity?

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItem(item: OrderItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrackingEvent(event: TrackingEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity): Long

    @Query("SELECT * FROM payments WHERE orderId = :orderId LIMIT 1")
    suspend fun findPayment(orderId: Int): PaymentEntity?

    @Update
    suspend fun updatePayment(payment: PaymentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPushAlert(alert: PushAlertEntity)

    @Query("SELECT * FROM push_alerts WHERE userId = :userId ORDER BY id DESC")
    fun observeAlerts(userId: Int): Flow<List<PushAlertEntity>>

    @Transaction
    @Query("SELECT * FROM orders WHERE customerId = :customerId ORDER BY id DESC")
    fun observeCustomerOrders(customerId: Int): Flow<List<OrderWithDetails>>

    @Transaction
    @Query("SELECT * FROM orders ORDER BY id DESC")
    fun observeStoreOrders(): Flow<List<OrderWithDetails>>
}
