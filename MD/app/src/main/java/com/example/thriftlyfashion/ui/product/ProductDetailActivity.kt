package com.example.thriftlyfashion.ui.product

import android.content.ContentValues
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.thriftlyfashion.R
import com.example.thriftlyfashion.data.CartDatabaseHelper

class ProductDetailActivity : AppCompatActivity() {

    private var productQuantity = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        // Inisialisasi views
        val btnBack: ImageView = findViewById(R.id.id_btnBack)
        val productImage: ImageView = findViewById(R.id.id_productImage)
        val productName: TextView = findViewById(R.id.id_productName)
        val productPrice: TextView = findViewById(R.id.id_productName2)
        val categoryButton: TextView = findViewById(R.id.id_productCategory)
        val amountProduct: TextView = findViewById(R.id.amountProduct)
        val btnMinus: ImageView = findViewById(R.id.imageView5)
        val btnPlus: ImageView = findViewById(R.id.imageView7)
        val btnAddToCart: Button = findViewById(R.id.button)

        // Mendapatkan data dari intent
        val name = intent.getStringExtra("PRODUCT_NAME") ?: "Nama Produk"
        val category = intent.getStringExtra("PRODUCT_CATEGORY") ?: "Kategori"
        val price = intent.getStringExtra("PRODUCT_PRICE") ?: "Rp. 0"
        val image = intent.getIntExtra("PRODUCT_IMAGE", R.drawable.image)

        // Mengisi data ke views
        productName.text = name
        productPrice.text = price
        categoryButton.text = category
        productImage.setImageResource(image)

        // Tombol kembali
        btnBack.setOnClickListener {
            finish()
        }

        // Mengurangi jumlah produk
        btnMinus.setOnClickListener {
            if (productQuantity > 1) {
                productQuantity--
                amountProduct.text = productQuantity.toString()
            } else {
                Toast.makeText(this, "Jumlah produk minimal adalah 1", Toast.LENGTH_SHORT).show()
            }
        }

        // Menambah jumlah produk
        btnPlus.setOnClickListener {
            productQuantity++
            amountProduct.text = productQuantity.toString()
        }

        // Menambahkan ke keranjang
        btnAddToCart.setOnClickListener {
            val dbHelper = CartDatabaseHelper(this)
            val db = dbHelper.writableDatabase

            val values = ContentValues().apply {
                put("name", name)
                put("category", category)
                put("price", price)
                put("quantity", productQuantity)
            }

            val newRowId = db.insert("cart", null, values)

            if (newRowId != -1L) {
                val message = "$name telah ditambahkan ke keranjang sebanyak $productQuantity item"
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Gagal menambahkan ke keranjang", Toast.LENGTH_SHORT).show()
            }

            db.close()
        }
    }
}