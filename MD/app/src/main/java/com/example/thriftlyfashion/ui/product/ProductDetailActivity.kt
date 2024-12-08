package com.example.thriftlyfashion.ui.product

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.thriftlyfashion.R
import com.example.thriftlyfashion.database.DatabaseHelper

class ProductDetailActivity : AppCompatActivity() {

    private var pQuantity = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        val btnBack: ImageView = findViewById(R.id.id_btnBack)
        val productImage: ImageView = findViewById(R.id.id_productImage)
        val productName: TextView = findViewById(R.id.id_productName)
        val productPrice: TextView = findViewById(R.id.id_productPrice)
        val productCategory: TextView = findViewById(R.id.id_productCategory)
        val productDescription: TextView = findViewById(R.id.id_productDescription)
        val productColor: TextView = findViewById(R.id.id_productColor)
        val productSize: TextView = findViewById(R.id.id_productSize)
        val productQuantity: TextView = findViewById(R.id.id_productQuantity)
        val amountProduct: TextView = findViewById(R.id.amountProduct)
        val btnMinus: ImageView = findViewById(R.id.imageView5)
        val btnPlus: ImageView = findViewById(R.id.imageView7)
        val btnAddToCart: Button = findViewById(R.id.button)

        val productId = intent.getStringExtra("PRODUCT_ID") ?: ""
        val storeId = intent.getIntExtra("STORE_ID", -1)
        val name = intent.getStringExtra("PRODUCT_NAME") ?: "Nama Produk"
        val category = intent.getStringExtra("PRODUCT_CATEGORY") ?: "Kategori"
        val price = intent.getDoubleExtra("PRODUCT_PRICE", 0.0)
        val image = intent.getStringExtra("PRODUCT_IMAGE") ?: ""
        val description = intent.getStringExtra("PRODUCT_DESCRIPTION") ?: "Deskripsi tidak tersedia"
        val color = intent.getStringExtra("PRODUCT_COLOR") ?: "Warna tidak tersedia"
        val size = intent.getStringExtra("PRODUCT_SIZE") ?: "Ukuran tidak tersedia"
        val quantity = intent.getIntExtra("PRODUCT_QUANTITY", 0)
        val createAt = intent.getStringExtra("PRODUCT_CREATEAT") ?: "Create at tidak tersedia"


        productName.text = name
        productPrice.text = "Rp ${String.format("%,.0f", price)}"
        productCategory.text = category
        productDescription.text = description
        productColor.text = "Color : $color"
        productSize.text = "Size : $size"
        productQuantity.text = "Product quantity : $quantity item"

        Glide.with(this)
            .load(image)
            .placeholder(R.drawable.image)
            .into(productImage)

        btnBack.setOnClickListener {
            finish()
        }

        btnMinus.setOnClickListener {
            if (pQuantity > 1) {
                pQuantity--
                amountProduct.text = pQuantity.toString()
            } else {
                Toast.makeText(this, "Jumlah produk minimal adalah 1", Toast.LENGTH_SHORT).show()
            }
        }

        btnPlus.setOnClickListener {
            pQuantity++
            amountProduct.text = pQuantity.toString()
        }

        btnAddToCart.setOnClickListener {
            val dbHelper = DatabaseHelper(this)
            val totalPrice = price * pQuantity

            val result = dbHelper.insertIntoCart(
                productId = productId,
                image = image,
                name = name,
                category = category,
                size = size,
                color = color,
                quantity = pQuantity,
                totalPrice = totalPrice
            )

            if (result != -1L) {
                val message = "$name telah ditambahkan ke keranjang sebanyak $pQuantity item. Total: Rp ${String.format("%,.0f", totalPrice)}"
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Gagal menambahkan ke keranjang", Toast.LENGTH_SHORT).show()
            }
        }

    }
}
