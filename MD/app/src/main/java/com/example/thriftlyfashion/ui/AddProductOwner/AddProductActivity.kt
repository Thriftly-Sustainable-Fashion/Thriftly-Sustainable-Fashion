package com.example.thriftlyfashion.ui.AddProductOwner

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.thriftlyfashion.R

class AddProductActivity : AppCompatActivity() {

    private var selectedImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            findViewById<ImageView>(R.id.img_preview).setImageURI(uri)
        } else {
            Toast.makeText(this, "Gagal memuat gambar", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)

        // Inisialisasi view
        val btnBack: ImageView = findViewById(R.id.id_btnBack)
        val etProductName: EditText = findViewById(R.id.et_product_name)
        val etProductPrice: EditText = findViewById(R.id.et_product_price)
        val btnUploadPhoto: Button = findViewById(R.id.btn_upload_photo)
        val btnAddProduct: Button = findViewById(R.id.btn_add_product)

        btnBack.setOnClickListener {
            finish()
        }

        btnUploadPhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnAddProduct.setOnClickListener {
            val productName = etProductName.text.toString()
            val productPrice = etProductPrice.text.toString()

            if (productName.isEmpty() || productPrice.isEmpty()) {
                Toast.makeText(this, "Harap isi semua field!", Toast.LENGTH_SHORT).show()
            } else if (selectedImageUri == null) {
                Toast.makeText(this, "Harap unggah foto produk!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Produk berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
