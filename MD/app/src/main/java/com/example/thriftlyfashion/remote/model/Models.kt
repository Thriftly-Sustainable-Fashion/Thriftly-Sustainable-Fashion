package com.example.thriftlyfashion.remote.model

import com.google.gson.annotations.SerializedName

// User Data Model
data class UserData(
    val name: String,
    val email: String,
    val password: String,
    val phone_number: String,
    val isOwner: Boolean
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val message: String,
    val token: String,
    val userId: Int
)

data class SignupRequest(
    val name: String,
    val email: String,
    val password: String,
    val isOwner: Boolean
)

data class SignupResponse(
    val message: String,
    val userId: Int
)

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val isOwner: Boolean
)

// Generic API Response
data class ApiResponse(
    val message: String
)

// Product Data Model
data class Product(
    @SerializedName("product_id") val productId: Int,
    @SerializedName("store_id") val storeId: Int,
    @SerializedName("images") val images: List<String>,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("price") val price: Double,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("category") val category: String,
    @SerializedName("color") val color: String,
    @SerializedName("size") val size: String,
    @SerializedName("created_at") val createdAt: String
)

data class ProductCard(
    @SerializedName("product_id") val productId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("category") val category: String,
    @SerializedName("image") val image: String,
    @SerializedName("price") val price: Double
)

// Cart Data Model
data class CartItem(
    val id: Int,
    val productId: Int,
    val image: String,
    val name: String,
    val category: String,
    val size: String,
    val color: String,
    val quantity: Int,
    val totalPrice: Double
)

data class CartItemRequest(
    val product_id: Int,
    val quantity: Int
)

// Order Data Model
data class OrderData(
    val totalAmount: Double
)
