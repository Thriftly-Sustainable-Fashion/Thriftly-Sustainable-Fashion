import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.thriftlyfashion.Product

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""CREATE TABLE IF NOT EXISTS users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            email TEXT UNIQUE NOT NULL,
            password TEXT NOT NULL,
            role TEXT NOT NULL DEFAULT 'user'
        );""")

        db.execSQL("""CREATE TABLE IF NOT EXISTS products (
            product_id INTEGER PRIMARY KEY AUTOINCREMENT,
            store_id INTEGER NOT NULL,
            name TEXT NOT NULL,
            description TEXT,
            price REAL NOT NULL,
            quantity INTEGER NOT NULL,
            category_id INTEGER NOT NULL,
            subcategory_id INTEGER NOT NULL,
            created_at TEXT NOT NULL,
            FOREIGN KEY(store_id) REFERENCES users(id),
            FOREIGN KEY(category_id) REFERENCES categories(id),
            FOREIGN KEY(subcategory_id) REFERENCES subcategories(id)
        );""")

        db.execSQL("""CREATE TABLE IF NOT EXISTS orders (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            order_date TEXT NOT NULL,
            user_id INTEGER NOT NULL,
            total_price REAL NOT NULL,
            FOREIGN KEY(user_id) REFERENCES users(id)
        );""")

        db.execSQL("""CREATE TABLE IF NOT EXISTS order_details (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            order_id INTEGER NOT NULL,
            product_id INTEGER NOT NULL,
            quantity INTEGER NOT NULL,
            price REAL NOT NULL,
            FOREIGN KEY(order_id) REFERENCES orders(id),
            FOREIGN KEY(product_id) REFERENCES products(product_id)
        );""")

        db.execSQL("""CREATE TABLE IF NOT EXISTS cart (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            category TEXT,
            price TEXT NOT NULL,
            quantity INTEGER NOT NULL,
            image INTEGER NOT NULL
        );""")

        db.execSQL("""CREATE TABLE IF NOT EXISTS categories (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL
        );""")

        db.execSQL("""CREATE TABLE IF NOT EXISTS subcategories (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            category_id INTEGER NOT NULL,
            FOREIGN KEY(category_id) REFERENCES categories(id)
        );""")
    }


    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drop older tables if they exist
        db.execSQL("DROP TABLE IF EXISTS order_details")
        db.execSQL("DROP TABLE IF EXISTS orders")
        db.execSQL("DROP TABLE IF EXISTS products")
        db.execSQL("DROP TABLE IF EXISTS users")
        db.execSQL("DROP TABLE IF EXISTS cart")

        // Create tables again
        db.execSQL("DROP TABLE IF EXISTS categories")
        db.execSQL("DROP TABLE IF EXISTS subcategories")
        onCreate(db)
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

    fun getAllCartItems(): List<Product> {
        val cartItems = mutableListOf<Product>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM cart", null)

        if (cursor.moveToFirst()) {
            do {
//                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                val category = cursor.getString(cursor.getColumnIndexOrThrow("category"))
                val price = cursor.getString(cursor.getColumnIndexOrThrow("price"))
                val quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"))
                val image = cursor.getInt(cursor.getColumnIndexOrThrow("image"))

                // Tambahkan data ke daftar
                cartItems.add(Product(image, name, category, price, "", "", quantity.toString()))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return cartItems
    }

    fun insertProduct(name: String, description: String, price: Double, userId: Int): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("description", description)
            put("price", price)
            put("user_id", userId)
        }
        val result = db.insert("products", null, values)
        db.close()
        return result
    }

    companion object {
        private const val DATABASE_NAME = "thriftly.db"
        private const val DATABASE_VERSION = 1
    }
}
