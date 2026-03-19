package com.example.dulcemoment.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        SessionEntity::class,
        ProductEntity::class,
        ProductOptionEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        TrackingEventEntity::class,
        PaymentEntity::class,
        PushAlertEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dulceDao(): DulceDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dulce_moment.db",
                )
                    .fallbackToDestructiveMigration(false)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                            super.onCreate(db)
                            seed(db)
                        }
                    })
                    .build()
                instance = db
                db
            }
        }

        private fun seed(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            db.execSQL("INSERT INTO users (name, email, password, role) VALUES ('Cliente Demo', 'cliente@dulce.com', '123456', 'customer')")
            db.execSQL("INSERT INTO users (name, email, password, role) VALUES ('Tienda Demo', 'tienda@dulce.com', '123456', 'store')")

            db.execSQL("INSERT INTO products (name, description, basePrice, stock, isActive) VALUES ('Pastel Personalizado', 'Elige tamaño, forma, sabor, color e ingredientes', 320, 12, 1)")
            db.execSQL("INSERT INTO products (name, description, basePrice, stock, isActive) VALUES ('Cupcakes Premium (6)', 'Caja de cupcakes decorados', 180, 20, 1)")

            db.execSQL("INSERT INTO product_options (productId, category, value, priceDelta) VALUES (1, 'size', 'chico', 0)")
            db.execSQL("INSERT INTO product_options (productId, category, value, priceDelta) VALUES (1, 'size', 'mediano', 90)")
            db.execSQL("INSERT INTO product_options (productId, category, value, priceDelta) VALUES (1, 'size', 'grande', 170)")
            db.execSQL("INSERT INTO product_options (productId, category, value, priceDelta) VALUES (1, 'shape', 'redondo', 0)")
            db.execSQL("INSERT INTO product_options (productId, category, value, priceDelta) VALUES (1, 'shape', 'cuadrado', 30)")
            db.execSQL("INSERT INTO product_options (productId, category, value, priceDelta) VALUES (1, 'shape', 'corazon', 50)")
            db.execSQL("INSERT INTO product_options (productId, category, value, priceDelta) VALUES (1, 'flavor', 'vainilla', 0)")
            db.execSQL("INSERT INTO product_options (productId, category, value, priceDelta) VALUES (1, 'flavor', 'chocolate', 20)")
            db.execSQL("INSERT INTO product_options (productId, category, value, priceDelta) VALUES (1, 'flavor', 'red_velvet', 35)")
            db.execSQL("INSERT INTO product_options (productId, category, value, priceDelta) VALUES (1, 'color', 'blanco', 0)")
            db.execSQL("INSERT INTO product_options (productId, category, value, priceDelta) VALUES (1, 'color', 'rosa', 15)")
            db.execSQL("INSERT INTO product_options (productId, category, value, priceDelta) VALUES (1, 'color', 'azul', 15)")
            db.execSQL("INSERT INTO product_options (productId, category, value, priceDelta) VALUES (1, 'ingredient', 'fresa', 12)")
            db.execSQL("INSERT INTO product_options (productId, category, value, priceDelta) VALUES (1, 'ingredient', 'oreo', 18)")
            db.execSQL("INSERT INTO product_options (productId, category, value, priceDelta) VALUES (1, 'ingredient', 'nutella', 25)")
        }
    }
}
