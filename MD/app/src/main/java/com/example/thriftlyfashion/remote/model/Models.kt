package com.example.thriftlyfashion.remote.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.google.gson.annotations.SerializedName

// User Data Model
@Parcelize
data class UserData(
    val name: String,
    val email: String,
    val password: String,
    val phone_number: String,
    val isOwner: Boolean
) : Parcelable

@Parcelize
data class LoginRequest(
    val email: String,
    val password: String
) : Parcelable

@Parcelize
data class LoginResponse(
    val message: String,
    val token: String,
    val userId: Int
) : Parcelable

@Parcelize
data class SignupRequest(
    val name: String,
    val email: String,
    val password: String,
    val isOwner: Boolean
) : Parcelable

@Parcelize
data class SignupResponse(
    val message: String,
    val userId: Int
) : Parcelable

@Parcelize
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val isOwner: Boolean
) : Parcelable

// Generic API Response
@Parcelize
data class ApiResponse(
    val message: String
) : Parcelable

// Product Data Model
@Parcelize
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
) : Parcelable

@Parcelize
data class ManageProduct(
    val id: String,
    val name: String,
    val category: String,
    val size: String,
    val color: String,
    val quantity: Int,
    val price: Double,
    val images: List<String>,
    val status: String
) : Parcelable


@Parcelize
data class ProductCard(
    @SerializedName("product_id") val productId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("category") val category: String,
    @SerializedName("image") val image: String,
    @SerializedName("price") val price: Double
) : Parcelable

// Cart Data Model
@Parcelize
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
) : Parcelable

@Parcelize
data class CartItemRequest(
    val product_id: Int,
    val quantity: Int
) : Parcelable

// Order Data Model
@Parcelize
data class OrderData(
    val totalAmount: Double
) : Parcelable
