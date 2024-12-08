package com.example.thriftlyfashion.model

data class Subcategory(
    val id: Int,
    val name: String,
    val categoryId: Int
) {
    companion object {
        const val CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS subcategories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                category_id INTEGER NOT NULL,
                FOREIGN KEY(category_id) REFERENCES categories(id)
            );
        """
    }
}
