package com.example.thriftlyfashion.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.thriftlyfashion.model.CartItem
import com.example.thriftlyfashion.model.Product

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(Product.CREATE_TABLE)
        db.execSQL(CartItem.CREATE_TABLE)
//        db.execSQL(Order.CREATE_TABLE)
//        db.execSQL(OrderDetail.CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
//        db.execSQL("DROP TABLE IF EXISTS order_details")
//        db.execSQL("DROP TABLE IF EXISTS orders")
        db.execSQL("DROP TABLE IF EXISTS products")
        db.execSQL("DROP TABLE IF EXISTS cart")
        onCreate(db)
    }

    fun insertProduct(
        productId: String,
        storeId: Int,
        name: String,
        description: String?,
        price: Double,
        quantity: Int,
        category: String,
        color: String,
        size: String,
        createdAt: String,
        images: String
    ): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("product_id", productId)
            put("store_id", storeId)
            put("name", name)
            put("description", description)
            put("price", price)
            put("quantity", quantity)
            put("category", category)
            put("color", color)
            put("size", size)
            put("created_at", createdAt)
            put("images", images)
        }
        val result = db.insert("products", null, values)
        db.close()
        return result
    }

    fun getAllProducts(): List<Product> {
        val db = this.readableDatabase
        val productList = mutableListOf<Product>()
        val query = "SELECT * FROM products"

        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                val product = Product(
                    productId = cursor.getString(cursor.getColumnIndexOrThrow("product_id")),
                    storeId = cursor.getInt(cursor.getColumnIndexOrThrow("store_id")),
                    images = cursor.getString(cursor.getColumnIndexOrThrow("images")),
                    name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    description = cursor.getString(cursor.getColumnIndexOrThrow("description")),
                    price = cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                    quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity")),
                    category = cursor.getString(cursor.getColumnIndexOrThrow("category")),
                    color = cursor.getString(cursor.getColumnIndexOrThrow("color")),
                    size = cursor.getString(cursor.getColumnIndexOrThrow("size")),
                    createdAt = cursor.getString(cursor.getColumnIndexOrThrow("created_at"))
                )
                productList.add(product)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return productList
    }

    fun insertIntoCart(
        productId: String,
        image: String,
        name: String,
        category: String,
        size: String,
        color: String,
        quantity: Int,
        totalPrice: Double
    ): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("product_id", productId)
            put("image", image)
            put("name", name)
            put("category", category)
            put("size", size)
            put("color", color)
            put("quantity", quantity)
            put("total_price", totalPrice)
        }
        return db.insert("cart", null, values)
    }

    fun getAllCartItems(): List<CartItem> {
        val cartItems = mutableListOf<CartItem>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM cart", null)
        cursor.use {
            while (cursor.moveToNext()) {
                cartItems.add(
                    CartItem(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        productId = cursor.getString(cursor.getColumnIndexOrThrow("product_id")),
                        image = cursor.getString(cursor.getColumnIndexOrThrow("image")),
                        name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        category = cursor.getString(cursor.getColumnIndexOrThrow("category")),
                        size = cursor.getString(cursor.getColumnIndexOrThrow("size")),
                        color = cursor.getString(cursor.getColumnIndexOrThrow("color")),
                        quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity")),
                        totalPrice = cursor.getDouble(cursor.getColumnIndexOrThrow("total_price"))
                    )
                )
            }
        }
        return cartItems
    }

    fun deleteCartItem(id: Int): Int {
        val db = writableDatabase
        return db.delete("cart", "id = ?", arrayOf(id.toString()))
    }

    fun updateCartItem(id: Int, quantity: Int, totalPrice: Double): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("quantity", quantity)
            put("total_price", totalPrice)
        }
        return db.update("cart", values, "id = ?", arrayOf(id.toString()))
    }

    companion object {
        private const val DATABASE_NAME = "thriftly.db"
        private const val DATABASE_VERSION = 6
    }
}
