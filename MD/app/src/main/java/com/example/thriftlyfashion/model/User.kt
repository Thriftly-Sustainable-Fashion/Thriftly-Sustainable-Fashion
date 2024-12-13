package com.example.thriftlyfashion.model

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val password: String,
    val isOwner: Boolean
) {
    companion object {
        const val CREATE_TABLE = """
            CREATE TABLE User (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                email TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL,
                isOwner INTEGER NOT NULL
            )
        """
    }
}
