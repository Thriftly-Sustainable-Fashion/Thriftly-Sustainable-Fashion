package com.example.thriftlyfashion.ui.product

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.thriftlyfashion.R
import com.example.thriftlyfashion.remote.SharedPrefManager
import com.example.thriftlyfashion.remote.UserSession
import com.example.thriftlyfashion.remote.api.ApiService
import com.example.thriftlyfashion.remote.api.RetrofitClient
import com.example.thriftlyfashion.remote.model.CartItemRequest
import com.example.thriftlyfashion.remote.model.Product
import com.example.thriftlyfashion.remote.model.ProductCard
import com.example.thriftlyfashion.ui.homepage.ProductListAdapter
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProductDetailActivity : AppCompatActivity() {

    private var pQuantity = 1
    private var productDetail: Product? = null
    private lateinit var amountProductPrice: TextView;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        val btnBack: ImageView = findViewById(R.id.id_btnBack)
        val amountProduct: TextView = findViewById(R.id.amountProduct)
        amountProductPrice = findViewById(R.id.amountProductPrice)
        val btnMinus: ImageView = findViewById(R.id.imageView5)
        val btnPlus: ImageView = findViewById(R.id.imageView7)
        val btnAddToCart: Button = findViewById(R.id.button)

        val productId = intent.getIntExtra("PRODUCT_ID", 0)

        fetchProductDetails(productId)

        btnBack.setOnClickListener {
            finish()
        }

        btnMinus.setOnClickListener {
            if (pQuantity > 1) {
                pQuantity--
                amountProduct.text = pQuantity.toString()

                productDetail?.let { product ->
                    val totalPrice = product.price * pQuantity
                    amountProductPrice.text = "Rp ${String.format("%,.0f", totalPrice)}"
                }
            } else {
                Toast.makeText(this, "Jumlah produk minimal adalah 1", Toast.LENGTH_SHORT).show()
            }
        }

        btnPlus.setOnClickListener {
            pQuantity++
            amountProduct.text = pQuantity.toString()

            productDetail?.let { product ->
                val totalPrice = product.price * pQuantity
                amountProductPrice.text = "Rp ${String.format("%,.0f", totalPrice)}"
            }
        }

        btnAddToCart.setOnClickListener {
            val sharedPrefManager = SharedPrefManager(this)
            val userId = sharedPrefManager.getUserId()
            val cartItem = CartItemRequest(product_id = productId, quantity = pQuantity)

            val apiService = RetrofitClient.createService(ApiService::class.java)
            apiService.addToCart(userId, cartItem).enqueue(object : Callback<Map<String, Any>> {
                override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        Toast.makeText(this@ProductDetailActivity, "Item added to cart: ${body?.get("cartItemId")}", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@ProductDetailActivity, "Failed to add item. Code: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                    Toast.makeText(this@ProductDetailActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }


        val productList = getDummyProductList()

        val recyclerView: RecyclerView = findViewById(R.id.id_recomendationProduct)
        val adapter = ProductListAdapter(this, productList)

        val productLayoutManager = FlexboxLayoutManager(this)
        productLayoutManager.flexWrap = FlexWrap.WRAP
        productLayoutManager.flexDirection = FlexDirection.ROW
        productLayoutManager.justifyContent = JustifyContent.SPACE_BETWEEN
        productLayoutManager.alignItems = AlignItems.CENTER

        recyclerView.setPadding(20, 20, 20, 20)

        recyclerView.layoutManager = productLayoutManager
        recyclerView.setHasFixedSize(true)
        recyclerView.isNestedScrollingEnabled = false

        recyclerView.adapter = adapter
        adjustRecyclerViewForScreenSize(recyclerView)
    }

    private fun fetchProductDetails(productId: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = RetrofitClient.createService(ApiService::class.java).getProductDetail(productId)
                if (response.isSuccessful) {
                    Log.d("ProductDetail", "Fetching product with ID: $productId")
                    productDetail = response.body()

                    productDetail?.let { product ->
                        val totalPrice = product.price * pQuantity
                        amountProductPrice.text = "Rp ${String.format("%,.0f", totalPrice)}"
                    }

                    updateUI()
                } else {
                    Toast.makeText(this@ProductDetailActivity, "Failed to load product details", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ProductDetailActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun updateUI() {
        productDetail?.let { product ->
            val productName: TextView = findViewById(R.id.id_productName)
            val productPrice: TextView = findViewById(R.id.id_productPrice)
            val productCategory: TextView = findViewById(R.id.id_productCategory)
            val productDescription: TextView = findViewById(R.id.id_productDescription)
            val productColor: TextView = findViewById(R.id.id_productColor)
            val productSize: TextView = findViewById(R.id.id_productSize)
            val productQuantity: TextView = findViewById(R.id.id_productQuantity)
            val productImage: ImageView = findViewById(R.id.id_productImage)

            productName.text = product.name
            productPrice.text = "Rp ${String.format("%,.0f", product.price)}"
            productCategory.text = product.category
            productDescription.text = product.description
            productColor.text = "Color: ${product.color}"
            productSize.text = "Size: ${product.size}"
            productQuantity.text = "Product quantity: ${product.quantity} item"

            Glide.with(this@ProductDetailActivity)
                .load(product.images)
                .placeholder(R.drawable.image)
                .into(productImage)
        }
    }

    fun getDummyProductList(): List<ProductCard> {
        return listOf(
            ProductCard(
                productId = 1,
                name = "T-Shirt Kasual",
                category = "Pakaian Pria",
                price = 150000.0,
                image = "https://example.com/images/tshirt_kasual.jpg"
            ),
            ProductCard(
                productId = 2,
                name = "Celana Jeans",
                category = "Pakaian Pria",
                price = 200000.0,
                image = "https://example.com/images/celana_jeans.jpg"
            ),
            ProductCard(
                productId = 3,
                name = "Sepatu Sneakers",
                category = "Sepatu Pria",
                price = 350000.0,
                image = "https://example.com/images/sepatu_sneakers.jpg"
            ),
            ProductCard(
                productId = 4,
                name = "Dress Wanita",
                category = "Pakaian Wanita",
                price = 250000.0,
                image = "https://example.com/images/dress_wanita.jpg"
            ),
            ProductCard(
                productId = 5,
                name = "Tas Ransel",
                category = "Aksesori",
                price = 120000.0,
                image = "https://example.com/images/tas_ransel.jpg"
            )
        )
    }

    private fun adjustRecyclerViewForScreenSize(recyclerView: RecyclerView) {
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels

        val columnCount = when {
            screenWidth >= 1800 -> 4
            screenWidth >= 1200 -> 3
            screenWidth >= 800 -> 2
            else -> 1
        }

        val itemWidth = (screenWidth / columnCount) - 20
        val layoutParams = recyclerView.layoutParams
        layoutParams.width = itemWidth * columnCount
        recyclerView.layoutParams = layoutParams
    }
}
