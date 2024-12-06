import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        // Enable foreign key support
        db.execSQL("PRAGMA foreign_keys = ON;")

        // Execute SQL commands from the thriftly.sql file
        db.execSQL("""CREATE TABLE IF NOT EXISTS users ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "email TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL);""")

        db.execSQL("CREATE TABLE IF NOT EXISTS products ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "description TEXT, " +
                "price REAL NOT NULL, " +
                "user_id INTEGER NOT NULL, " +
                "FOREIGN KEY(user_id) REFERENCES users(id));")

        db.execSQL("CREATE TABLE IF NOT EXISTS orders ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "order_date TEXT NOT NULL, " +
                "user_id INTEGER NOT NULL," +
                "total_price REAL NOT NULL, " +
                "FOREIGN KEY(user_id) REFERENCES users(id));")

        db.execSQL("CREATE TABLE IF NOT EXISTS order_details ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "order_id INTEGER NOT NULL, " +
                "product_id INTEGER NOT NULL, " +
                "quantity INTEGER NOT NULL, " +
                "price REAL NOT NULL, " +
                "FOREIGN KEY(order_id) REFERENCES orders(id), " +
                "FOREIGN KEY(product_id) REFERENCES products(id));")
        db.execSQL("CREATE TABLE IF NOT EXISTS cart ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "category TEXT, " +
                "price TEXT NOT NULL, " +
                "quantity INTEGER NOT NULL, " +
                "image INTEGER NOT NULL);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS order_details")
        db.execSQL("DROP TABLE IF EXISTS orders")
        db.execSQL("DROP TABLE IF EXISTS products")
        db.execSQL("DROP TABLE IF EXISTS users")
        db.execSQL("DROP TABLE IF EXISTS cart")

        onCreate(db)
    }

    fun insertUser(name: String, email: String, password: String): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put("name", name)
            put("email", email)
            put("password", password)
        }
        return db.insert("users", null, contentValues)
    }

    fun insertProduct(name: String, description: String?, price: Double, userId: Int): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put("name", name)
            put("description", description)
            put("price", price)
            put("user_id", userId)
        }
        return db.insert("products", null, contentValues)
    }

    fun insertOrder(orderDate: String, userId: Int, totalPrice: Double): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put("order_date", orderDate)
            put("user_id", userId)
            put("total_price", totalPrice)
        }
        return db.insert("orders", null, contentValues)
    }

    fun insertOrderDetail(orderId: Int, productId: Int, quantity: Int, price: Double): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put("order_id", orderId)
            put("product_id", productId)
            put("quantity", quantity)
            put("price", price)
        }
        return db.insert("order_details", null, contentValues)
    }

    fun insertIntoCart(name: String, category: String, price: String, quantity: Int, image: Int): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("category", category)
            put("price", price)
            put("quantity", quantity)
            put("image", image)
        }
        val result = db.insert("cart", null, values)
        db.close()
        return result
    }

    companion object {
        private const val DATABASE_NAME = "thriftly.db"
        private const val DATABASE_VERSION = 1
    }
}