package com.example.thriftlyfashion.ui.product

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.thriftlyfashion.R

class ProductDetailActivity : AppCompatActivity() {
    private var activeButton: Button? = null

    private var selectedColor: String? = null
    private var selectedSize: String? = null
    private var selectedCategory: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        val btnBack: ImageView = findViewById(R.id.id_btnBack)
        val productImage: ImageView = findViewById(R.id.id_productImage)
        val productName: TextView = findViewById(R.id.id_productName)
        val productPrice: TextView = findViewById(R.id.id_productName2)
        val colorButton: Button = findViewById(R.id.id_colors)
        val sizeButton: Button = findViewById(R.id.id_size)
        val categoryButton: Button = findViewById(R.id.id_category)

        val name = intent.getStringExtra("PRODUCT_NAME") ?: "Nama Produk"
        val category = intent.getStringExtra("PRODUCT_CATEGORY") ?: "Kategori"
        val price = intent.getStringExtra("PRODUCT_PRICE") ?: "Rp. 0"
        val image = intent.getIntExtra("PRODUCT_IMAGE", R.drawable.image)

        productName.text = name
        productPrice.text = price
        productImage.setImageResource(image)
        categoryButton.text = category

        btnBack.setOnClickListener {
            finish()
        }

        colorButton.setOnClickListener {
            setActiveButton(colorButton)
            loadFragment(ProductColorFragment())
        }

        sizeButton.setOnClickListener {
            setActiveButton(sizeButton)
            loadFragment(ProductSizeFragment())
        }

        categoryButton.setOnClickListener {
            setActiveButton(categoryButton)
            loadFragment(ProductCategoryFragment())
        }

        // Set initial values if any
        setActiveButton(colorButton)
        loadFragment(ProductColorFragment())
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun setActiveButton(button: Button) {
        activeButton?.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
        button.backgroundTintList = ContextCompat.getColorStateList(this, R.color.full_white)

        activeButton = button
    }

    // Method to update selected data
    fun updateSelectedData(color: String?, size: String?, category: String?) {
        selectedColor = color
        selectedSize = size
        selectedCategory = category

        // Update the UI in the activity
        findViewById<Button>(R.id.id_colors).text = selectedColor ?: "Select Color"
        findViewById<Button>(R.id.id_size).text = selectedSize ?: "Select Size"
        findViewById<Button>(R.id.id_category).text = selectedCategory ?: "Select Category"
    }
}
