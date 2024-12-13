package com.example.thriftlyfashion.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.thriftlyfashion.R
import com.example.thriftlyfashion.ui.login.LoginActivity
import com.example.thriftlyfashion.ui.signup.SignupActivity

class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_start)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val tvHello = findViewById<TextView>(R.id.tvHello)
        val btnMasuk = findViewById<Button>(R.id.btnMasuk)
        val btnDaftar = findViewById<Button>(R.id.btnDaftar)

        btnMasuk.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        btnDaftar.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)

        }

        tvHello.text = getString(R.string.hello_text)


        // ====== Seeder ======
//        val dbHelper = DatabaseHelper(this)
//
//        private fun getDummyProducts(): List<Product> {
//    return listOf(
//        Product(
//            productId = 1,
//            name = "Kemeja Flanel Bekas",
//            description = "Kemeja flanel bekas dengan kualitas terbaik. Cocok untuk acara santai.",
//            category = "Fashion Pria",
//            price = 50000.0,
//            stock = 10,
//            image = "https://images.pexels.com/photos/13316724/pexels-photo-13316724.jpeg?auto=compress&cs=tinysrgb&w=600"
//        ),
//        Product(
//            productId = 2,
//            name = "Kaos Polos Hitam",
//            description = "Kaos polos hitam bekas, nyaman dipakai sehari-hari.",
//            category = "Fashion Pria",
//            price = 30000.0,
//            stock = 15,
//            image = "https://images.pexels.com/photos/8532616/pexels-photo-8532616.jpeg?auto=compress&cs=tinysrgb&w=600"
//        ),
//        Product(
//            productId = 3,
//            name = "Dress Cantik Bekas",
//            description = "Dress cantik untuk acara formal atau semi-formal.",
//            category = "Fashion Wanita",
//            price = 75000.0,
//            stock = 8,
//            image = "https://images.pexels.com/photos/19852011/pexels-photo-19852011/free-photo-of-kota-mode-fashion-fesyen.jpeg?auto=compress&cs=tinysrgb&w=600"
//        ),
//        Product(
//            productId = 4,
//            name = "Jeans Sobek Stylish",
//            description = "Jeans bekas dengan desain sobek yang stylish.",
//            category = "Fashion Pria",
//            price = 120000.0,
//            stock = 12,
//            image = "https://images.pexels.com/photos/3324443/pexels-photo-3324443.jpeg?auto=compress&cs=tinysrgb&w=600"
//        ),
//        Product(
//            productId = 5,
//            name = "Tas Kulit Bekas",
//            description = "Tas kulit berkualitas, cocok untuk kebutuhan sehari-hari.",
//            category = "Aksesoris",
//            price = 150000.0,
//            stock = 5,
//            image = "https://images.pexels.com/photos/27035625/pexels-photo-27035625/free-photo-of-stiletto-dan-carteras.jpeg?auto=compress&cs=tinysrgb&w=600"
//        ),
//        Product(
//            productId = 6,
//            name = "Sepatu Sneakers Bekas",
//            description = "Sneakers bekas yang nyaman dan stylish.",
//            category = "Fashion Pria",
//            price = 200000.0,
//            stock = 7,
//            image = "https://images.pexels.com/photos/6046212/pexels-photo-6046212.jpeg?auto=compress&cs=tinysrgb&w=600"
//        ),
//        Product(
//            productId = 7,
//            name = "Jaket Denim Bekas",
//            description = "Jaket denim bekas dengan kualitas premium.",
//            category = "Fashion Pria",
//            price = 180000.0,
//            stock = 6,
//            image = "https://images.pexels.com/photos/29738017/pexels-photo-29738017.jpeg?auto=compress&cs=tinysrgb&w=600"
//        ),
//        Product(
//            productId = 8,
//            name = "Rok Mini Bekas",
//            description = "Rok mini bekas yang modis dan nyaman dipakai.",
//            category = "Fashion Wanita",
//            price = 60000.0,
//            stock = 9,
//            image = "https://images.pexels.com/photos/6069974/pexels-photo-6069974.jpeg?auto=compress&cs=tinysrgb&w=600"
//        )
//    )
//}
//
//        dataProduct.forEach { product ->
//            val result = dbHelper.insertProduct(
//                product.productId,
//                product.storeId,
//                product.name,
//                product.description,
//                product.price,
//                product.quantity,
//                product.category,
//                product.color,
//                product.size,
//                product.createdAt,
//                product.images
//            )
//
//            if (result == -1L) {
//                Log.e("Database", "Gagal menambahkan produk: ${product.name}")
//            } else {
//                Log.i("Database", "Produk berhasil ditambahkan: ${product.name}")
//            }
//        }
    }

}