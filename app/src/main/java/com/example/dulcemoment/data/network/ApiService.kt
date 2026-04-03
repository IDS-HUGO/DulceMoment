package com.example.dulcemoment.data.network

import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.Part
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("api/v1/auth/register")
    suspend fun register(@Body body: AuthRequest): AuthResponse

    @POST("api/v1/auth/login")
    suspend fun login(@Body body: AuthRequest): AuthResponse

    @POST("api/v1/auth/refresh")
    suspend fun refresh(@Body body: RefreshTokenRequest): AuthResponse

    @POST("api/v1/auth/logout")
    suspend fun logout(
        @Header("Authorization") authHeader: String,
        @Body body: LogoutRequest,
    ): Map<String, Any>

    @POST("api/v1/auth/logout-all")
    suspend fun logoutAll(@Header("Authorization") authHeader: String): Map<String, Any>

    @POST("api/v1/media/cloudinary/upload-url")
    suspend fun uploadImageToCloudinary(
        @Header("Authorization") authHeader: String,
        @Body body: CloudinaryUploadRequest,
    ): CloudinaryUploadResponse

    @Multipart
    @POST("api/v1/media/cloudinary/upload-file")
    suspend fun uploadImageFileToCloudinary(
        @Header("Authorization") authHeader: String,
        @Part file: MultipartBody.Part,
    ): CloudinaryUploadResponse

    @GET("api/v1/auth/me")
    suspend fun me(@Header("Authorization") authHeader: String): UserDto

    @PATCH("api/v1/auth/me")
    suspend fun updateMe(
        @Header("Authorization") authHeader: String,
        @Body body: UpdateUserRequest,
    ): UserDto

    @GET("api/v1/store/public-profile")
    suspend fun storePublicProfile(@Header("Authorization") authHeader: String): StorePublicProfileDto

    @GET("api/v1/products")
    suspend fun products(@Query("only_active") onlyActive: Boolean = true): List<ProductDto>

    @POST("api/v1/products")
    suspend fun createProduct(
        @Header("Authorization") authHeader: String,
        @Body body: CreateProductRequest,
    ): ProductDto

    @PATCH("api/v1/products/{productId}")
    suspend fun updateProduct(
        @Header("Authorization") authHeader: String,
        @Path("productId") productId: Int,
        @Body body: UpdateProductRequest,
    ): ProductDto

    @POST("api/v1/orders")
    suspend fun createOrder(
        @Header("Authorization") authHeader: String,
        @Body body: CreateOrderRequest,
    ): OrderDto

    @GET("api/v1/orders")
    suspend fun orders(
        @Header("Authorization") authHeader: String,
        @Query("customer_id") customerId: Int? = null,
    ): List<OrderDto>

    @POST("api/v1/orders/{orderId}/cancel")
    suspend fun cancelOrder(
        @Header("Authorization") authHeader: String,
        @Path("orderId") orderId: Int,
    ): OrderDto

    @POST("api/v1/orders/{orderId}/status")
    suspend fun updateOrderStatus(
        @Header("Authorization") authHeader: String,
        @Path("orderId") orderId: Int,
        @Body body: UpdateOrderStatusRequest,
    ): OrderDto

    @POST("api/v1/payments/create-payment-method")
    suspend fun createPaymentMethod(
        @Header("Authorization") authHeader: String,
        @Body body: Map<String, String>,
    ): Map<String, Any>

    @POST("api/v1/payments/card")
    suspend fun payCard(
        @Header("Authorization") authHeader: String,
        @Header("X-Payment-Provider") provider: String,
        @Body body: Map<String, String>,
    ): Map<String, Any>

    @GET("api/v1/payments/{orderId}/diagnostics")
    suspend fun paymentDiagnostics(
        @Header("Authorization") authHeader: String,
        @Path("orderId") orderId: Int,
    ): Map<String, Any>
}
