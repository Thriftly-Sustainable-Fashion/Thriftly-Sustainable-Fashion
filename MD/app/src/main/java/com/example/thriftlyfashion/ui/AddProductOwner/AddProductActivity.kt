package com.example.thriftlyfashion.ui.AddProductOwner

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.thriftlyfashion.R

class AddProductActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)

        val btnBack: ImageView = findViewById(R.id.id_btnBack)
        val etProductName: EditText = findViewById(R.id.et_product_name)
        val etProductPrice: EditText = findViewById(R.id.et_product_price)
        val btnUploadPhoto: Button = findViewById(R.id.btn_upload_photo)
        val btnAddProduct: Button = findViewById(R.id.btn_add_product)

        btnBack.setOnClickListener {
            finish()
        }

        btnUploadPhoto.setOnClickListener {
            Toast.makeText(this, "Fitur unggah foto belum tersedia", Toast.LENGTH_SHORT).show()
        }

        btnAddProduct.setOnClickListener {
            val productName = etProductName.text.toString()
            val productPrice = etProductPrice.text.toString()

            if (productName.isEmpty() || productPrice.isEmpty()) {
                Toast.makeText(this, "Harap isi semua field!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    this,
                    "Produk $productName dengan harga $productPrice berhasil ditambahkan!",
                    Toast.LENGTH_SHORT
                ).show()

                etProductName.text.clear()
                etProductPrice.text.clear()
            }
        }
    }
}
