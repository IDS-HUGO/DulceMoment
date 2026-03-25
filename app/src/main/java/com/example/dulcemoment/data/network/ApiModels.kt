package com.example.dulcemoment.data.network

data class AuthRequest(
    val name: String? = null,
    val email: String,
    val password: String,
    val role: String? = null,
)

data class UserDto(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
)

data class AuthResponse(
    val user: UserDto,
    val access_token: String,
    val refresh_token: String,
    val token_type: String,
)

data class RefreshTokenRequest(
    val refresh_token: String,
)

data class LogoutRequest(
    val refresh_token: String,
)

data class CloudinaryUploadRequest(
    val source_url: String,
)

data class CloudinaryUploadResponse(
    val image_url: String,
    val public_id: String,
)

data class ProductOptionDto(
    val id: Int,
    val category: String,
    val value: String,
    val price_delta: Double,
)

data class ProductDto(
    val id: Int,
    val name: String,
    val description: String,
    val base_price: Double,
    val stock: Int,
    val is_active: Boolean,
    val image_url: String,
    val options: List<ProductOptionDto>,
)

data class CreateProductRequest(
    val name: String,
    val description: String,
    val base_price: Double,
    val stock: Int,
    val image_url: String = "",
)

data class UpdateProductRequest(
    val stock: Int? = null,
    val is_active: Boolean? = null,
)

data class OrderItemRequest(
    val product_id: Int,
    val quantity: Int,
    val custom_ingredients: String,
    val custom_size: String,
    val custom_shape: String,
    val custom_flavor: String,
    val custom_color: String,
)

data class CreateOrderRequest(
    val customer_id: Int,
    val delivery_address: String,
    val notes: String,
    val items: List<OrderItemRequest>,
)

data class TrackingEventDto(
    val id: Int,
    val status: String,
    val message: String,
    val eta_minutes: Int,
)

data class OrderItemDto(
    val id: Int,
    val product_id: Int,
    val quantity: Int,
    val unit_price: Double,
    val custom_ingredients: String,
    val custom_size: String,
    val custom_shape: String,
    val custom_flavor: String,
    val custom_color: String,
)

data class OrderDto(
    val id: Int,
    val customer_id: Int,
    val status: String,
    val total: Double,
    val delivery_address: String,
    val notes: String,
    val items: List<OrderItemDto>,
    val events: List<TrackingEventDto>,
)

data class UpdateOrderStatusRequest(
    val status: String,
    val message: String,
    val eta_minutes: Int,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
)

data class CardPaymentRequest(
    val order_id: Int,
    val card_number: String,
    val holder_name: String,
    val security_code: String,
    val expiry_month: Int,
    val expiry_year: Int,
)
