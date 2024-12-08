package com.example.thriftlyfashion.model

data class Category(
    val id: Int,
    val name: String
) {
    companion object {
        const val CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL
            );
        """
    }
}
