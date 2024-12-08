package com.example.thriftlyfashion.model

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val password: String,
    val role: String
) {
    companion object {
        const val CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                email TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL,
                role TEXT NOT NULL DEFAULT 'user'
            );
        """
    }
}
