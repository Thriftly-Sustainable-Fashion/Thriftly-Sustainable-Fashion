package com.example.thriftlyfashion.ui.AddBulkProduct

import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.thriftlyfashion.R

class AddBulkProductActivity : AppCompatActivity() {

    private var selectedImageUri: Uri? = null
    private var productQuantity = 10

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
        setContentView(R.layout.activity_add_bulk_product)

        val btnBack: ImageView = findViewById(R.id.id_btnBack)
        val etProductName: EditText = findViewById(R.id.et_product_name)
        val etProductPrice: EditText = findViewById(R.id.et_product_price)
        val tvProductQuantity: TextView = findViewById(R.id.tv_product_quantity)
        val btnDecrease: Button = findViewById(R.id.btn_decrease)
        val btnIncrease: Button = findViewById(R.id.btn_increase)
        val btnUploadPhoto: Button = findViewById(R.id.btn_upload_photo)
        val btnAddProduct: Button = findViewById(R.id.btn_add_product)

        btnBack.setOnClickListener {
            finish()
        }

        btnDecrease.setOnClickListener {
            if (productQuantity > 1) {
                productQuantity--
                tvProductQuantity.text = productQuantity.toString()
            } else {
                Toast.makeText(this, "Jumlah tidak bisa kurang dari 1", Toast.LENGTH_SHORT).show()
            }
        }

        btnIncrease.setOnClickListener {
            productQuantity++
            tvProductQuantity.text = productQuantity.toString()
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
