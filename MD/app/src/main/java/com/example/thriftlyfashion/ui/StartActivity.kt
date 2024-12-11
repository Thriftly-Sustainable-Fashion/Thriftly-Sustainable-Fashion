package com.example.thriftlyfashion.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
//        val dataProduct = listOf(
//            Product(
//                productId = "P001",
//                storeId = 101,
//                images = "https://example.com/image1.jpg, https://example.com/image2.jpg",
//                name = "Kemeja Kasual",
//                description = "Kemeja kasual dengan bahan katun yang nyaman.",
//                price = 250000.0,
//                quantity = 15,
//                category = "Kemeja",
//                color = "Putih",
//                size = "L",
//                createdAt = "2024-12-07"
//            ),
//            Product(
//                productId = "P002",
//                storeId = 102,
//                images = "https://example.com/image3.jpg, https://example.com/image4.jpg",
//                name = "Jaket Denim",
//                description = "Jaket denim klasik yang stylish.",
//                price = 450000.0,
//                quantity = 20,
//                category = "Jaket",
//                color = "Biru",
//                size = "M",
//                createdAt = "2024-12-07"
//            ),
//            Product(
//                productId = "P003",
//                storeId = 103,
//                images = "https://example.com/image5.jpg, https://example.com/image6.jpg",
//                name = "Celana Jeans",
//                description = "Celana jeans slim fit dengan bahan stretch.",
//                price = 300000.0,
//                quantity = 30,
//                category = "Celana",
//                color = "Hitam",
//                size = "32",
//                createdAt = "2024-12-07"
//            ),
//            Product(
//                productId = "P004",
//                storeId = 104,
//                images = "https://example.com/image7.jpg, https://example.com/image8.jpg",
//                name = "Kaos Polos",
//                description = "Kaos polos sederhana dan nyaman.",
//                price = 100000.0,
//                quantity = 50,
//                category = "Kaos",
//                color = "Abu-abu",
//                size = "XL",
//                createdAt = "2024-12-07"
//            ),
//            Product(
//                productId = "P005",
//                storeId = 105,
//                images = "https://example.com/image9.jpg, https://example.com/image10.jpg",
//                name = "Sepatu Sneakers",
//                description = "Sepatu sneakers trendi untuk sehari-hari.",
//                price = 600000.0,
//                quantity = 10,
//                category = "Sepatu",
//                color = "Putih",
//                size = "42",
//                createdAt = "2024-12-07"
//            ),
//            Product(
//                productId = "P006",
//                storeId = 106,
//                images = "https://example.com/image11.jpg, https://example.com/image12.jpg",
//                name = "Blazer Formal",
//                description = "Blazer formal untuk acara penting.",
//                price = 800000.0,
//                quantity = 5,
//                category = "Blazer",
//                color = "Navy",
//                size = "L",
//                createdAt = "2024-12-07"
//            ),
//            Product(
//                productId = "P007",
//                storeId = 107,
//                images = "https://example.com/image13.jpg, https://example.com/image14.jpg",
//                name = "Topi Trucker",
//                description = "Topi trucker untuk gaya kasual.",
//                price = 75000.0,
//                quantity = 25,
//                category = "Aksesoris",
//                color = "Hitam",
//                size = "All Size",
//                createdAt = "2024-12-07"
//            ),
//            Product(
//                productId = "P008",
//                storeId = 108,
//                images = "https://example.com/image15.jpg, https://example.com/image16.jpg",
//                name = "Sweater Rajut",
//                description = "Sweater rajut hangat untuk musim dingin.",
//                price = 350000.0,
//                quantity = 12,
//                category = "Sweater",
//                color = "Merah Maroon",
//                size = "L",
//                createdAt = "2024-12-07"
//            )
//        )
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