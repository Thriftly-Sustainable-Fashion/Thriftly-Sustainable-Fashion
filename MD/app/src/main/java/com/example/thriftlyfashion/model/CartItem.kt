package com.example.thriftlyfashion.model

data class CartItem(
    val id: Int,
    val productId: String,
    val image: String,
    val name: String,
    val category: String,
    val size: String,
    val color: String,
    val quantity: Int,
    val totalPrice: Double
) {
    companion object {
        const val CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS cart (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                product_id TEXT NOT NULL,
                image TEXT NOT NULL,
                name TEXT NOT NULL,
                category TEXT NOT NULL,
                size TEXT NOT NULL,
                color TEXT NOT NULL,
                quantity INTEGER NOT NULL,
                total_price REAL NOT NULL
            );
        """
    }
}
