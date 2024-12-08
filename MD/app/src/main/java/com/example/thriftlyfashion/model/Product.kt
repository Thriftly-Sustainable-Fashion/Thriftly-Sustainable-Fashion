package com.example.thriftlyfashion.model

data class Product(
    val productId: String,
    val storeId: Int,
    val images: String,
    val name: String,
    val description: String?,
    val price: Double,
    val quantity: Int,
    val category: String,
    val color: String,
    val size: String,
    val createdAt: String
) {
    companion object {
        const val CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS products (
                product_id TEXT PRIMARY KEY,
                store_id INTEGER NOT NULL,
                images TEXT NOT NULL,
                name TEXT NOT NULL,
                description TEXT,
                price REAL NOT NULL,
                quantity INTEGER NOT NULL,
                category TEXT NOT NULL,
                color TEXT NOT NULL,
                size TEXT NOT NULL,
                created_at TEXT NOT NULL,
                FOREIGN KEY(store_id) REFERENCES users(id)
            );
        """
    }
}
