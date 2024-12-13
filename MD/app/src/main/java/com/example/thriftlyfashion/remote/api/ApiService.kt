package com.example.thriftlyfashion.remote.api

import com.example.thriftlyfashion.remote.model.Product
import com.example.thriftlyfashion.remote.model.ApiResponse
import com.example.thriftlyfashion.remote.model.CartItem
import com.example.thriftlyfashion.remote.model.CartItemRequest
import com.example.thriftlyfashion.remote.model.LoginRequest
import com.example.thriftlyfashion.remote.model.LoginResponse
import com.example.thriftlyfashion.remote.model.OrderData
import com.example.thriftlyfashion.remote.model.ProductCard
import com.example.thriftlyfashion.remote.model.SignupRequest
import com.example.thriftlyfashion.remote.model.SignupResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("/api/users/register")
    suspend fun registerUser(@Body signupRequest: SignupRequest): Response<SignupResponse>

    @POST("/api/users/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @GET("/api/products")
    fun getAllProducts(): Call<List<ProductCard>>

    @GET("/api/products/{id}")
    suspend fun getProductDetail(@Path("id") productId: Int): Response<Product>


    @POST("/api/products")
    fun createProduct(@Body productData: Product): Call<ApiResponse>

    @PUT("/api/products/{id}")
    fun updateProduct(@Path("id") productId: Int, @Body productData: Product): Call<ApiResponse>

    @DELETE("/api/products/{id}")
    fun deleteProduct(@Path("id") productId: Int): Call<ApiResponse>


    @POST("/api/cart")
    fun addToCart(@Query("user_id") userId: Int, @Body cartItemRequest: CartItemRequest): Call<Map<String, Any>>

    @GET("api/cart")
    fun getCartItems(@Query("user_id") userId: Int): Call<List<CartItem>>

    @DELETE("/api/cart/{itemId}")
    fun deleteCartItem(@Path("itemId") itemId: Int): Call<Map<String, String>>


    @POST("order")
    fun placeOrder(@Body orderData: OrderData): Call<ApiResponse>
}

