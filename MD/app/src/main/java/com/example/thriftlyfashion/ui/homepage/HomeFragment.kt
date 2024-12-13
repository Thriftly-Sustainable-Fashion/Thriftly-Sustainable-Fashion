package com.example.thriftlyfashion.ui.homepage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.thriftlyfashion.R
import com.example.thriftlyfashion.remote.api.ApiService
import com.example.thriftlyfashion.remote.api.RetrofitClient
import com.example.thriftlyfashion.remote.model.ProductCard
import com.example.thriftlyfashion.ui.search.SearchActivity
import com.github.javafaker.Faker
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.abs

class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = view.findViewById(R.id.shopRecyclerView)
        val productRecyclerView: RecyclerView = view.findViewById(R.id.id_productList)

        val heroRecyclerView: RecyclerView = view.findViewById(R.id.heroRecyclerView)
        val loadingHero: ProgressBar = view.findViewById(R.id.id_loadingHero)
        val emptyCardHero: CardView = view.findViewById(R.id.id_emptyHero)

        updateUI(loadingHero, heroRecyclerView, emptyCardHero, "loading")

        val productImages = listOf(
            "https://images.pexels.com/photos/14482086/pexels-photo-14482086.jpeg?auto=compress&cs=tinysrgb&w=600",
            "https://images.pexels.com/photos/4480460/pexels-photo-4480460.jpeg?auto=compress&cs=tinysrgb&w=600",
            "https://images.pexels.com/photos/3736292/pexels-photo-3736292.jpeg?auto=compress&cs=tinysrgb&w=600",
            "https://images.pexels.com/photos/11743259/pexels-photo-11743259.jpeg?auto=compress&cs=tinysrgb&w=600",
            "https://images.pexels.com/photos/4111072/pexels-photo-4111072.jpeg?auto=compress&cs=tinysrgb&w=600"
        )

        val heroImages = listOf(
            R.drawable.hero1,
            R.drawable.hero2,
            R.drawable.hero3,
            R.drawable.hero4
        )

        if (heroImages.isNotEmpty()) {
            updateUI(loadingHero, heroRecyclerView, emptyCardHero, "success")
        } else {
            updateUI(loadingHero, heroRecyclerView, emptyCardHero, "empty")
        }

        val apiService = RetrofitClient.createService(ApiService::class.java)

        apiService.getAllProducts().enqueue(object : Callback<List<ProductCard>> {
            override fun onResponse(call: Call<List<ProductCard>>, response: Response<List<ProductCard>>) {
                if (!isAdded) return

                val productList: List<ProductCard>

                if (response.isSuccessful) {
                    val allProducts = response.body() ?: emptyList()

                    productList = allProducts.map { product ->
                        ProductCard(
                            productId = product.productId,
                            image = product.image,
                            name = product.name,
                            category = product.category,
                            price = product.price,
                        )
                    }
                } else {
                    productList = getDummyProducts()
                }

                context?.let { ctx ->
                    val layoutManager = LinearLayoutManager(ctx, LinearLayoutManager.HORIZONTAL, false)
                    recyclerView.layoutManager = layoutManager

                    val heroLayoutManager = LinearLayoutManager(ctx, LinearLayoutManager.HORIZONTAL, false)
                    heroRecyclerView.layoutManager = heroLayoutManager

                    val productLayoutManager = FlexboxLayoutManager(ctx)
                    productLayoutManager.flexWrap = FlexWrap.WRAP
                    productLayoutManager.flexDirection = FlexDirection.ROW
                    productRecyclerView.layoutManager = productLayoutManager

                    val adapter = ShopListAdapter(ctx, productImages)
                    recyclerView.adapter = adapter

                    val heroAdapter = HeroListAdapter(ctx, heroImages)
                    heroRecyclerView.adapter = heroAdapter

                    val productAdapter = ProductListAdapter(ctx, productList)
                    productRecyclerView.adapter = productAdapter

                    adjustRecyclerViewForScreenSize(productRecyclerView)
                }
            }

            override fun onFailure(call: Call<List<ProductCard>>, t: Throwable) {
                if (!isAdded) return
                val productList = getDummyProducts()

                context?.let { ctx ->
                    val layoutManager = LinearLayoutManager(ctx, LinearLayoutManager.HORIZONTAL, false)
                    recyclerView.layoutManager = layoutManager

                    val heroLayoutManager = LinearLayoutManager(ctx, LinearLayoutManager.HORIZONTAL, false)
                    heroRecyclerView.layoutManager = heroLayoutManager

                    val productLayoutManager = FlexboxLayoutManager(ctx)
                    productLayoutManager.flexWrap = FlexWrap.WRAP
                    productLayoutManager.flexDirection = FlexDirection.ROW
                    productRecyclerView.layoutManager = productLayoutManager

                    val adapter = ShopListAdapter(ctx, productImages)
                    recyclerView.adapter = adapter

                    val heroAdapter = HeroListAdapter(ctx, heroImages)
                    heroRecyclerView.adapter = heroAdapter

                    val productAdapter = ProductListAdapter(ctx, productList)
                    productRecyclerView.adapter = productAdapter

                    adjustRecyclerViewForScreenSize(productRecyclerView)
                }
            }
        })

        recyclerView.setHasFixedSize(true)

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(heroRecyclerView)

        heroRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val centerX = recyclerView.width / 2

                for (i in 0 until recyclerView.childCount) {
                    val child = recyclerView.getChildAt(i) ?: continue
                    val layoutParams = child.layoutParams as RecyclerView.LayoutParams

                    val childCenterX = child.left + layoutParams.leftMargin + (child.width / 2)
                    val distanceFromCenter = abs(centerX - childCenterX)
                    val scale = 1 - (distanceFromCenter.toFloat() / recyclerView.width.toFloat())

                    val minScale = 0.8f
                    val maxScale = 1.0f
                    val interpolatedScale = minScale + scale * (maxScale - minScale)

                    child.scaleX = interpolatedScale
                    child.scaleY = interpolatedScale
                }
            }
        })

        val searchButton: LinearLayout = view.findViewById(R.id.id_search)
        searchButton.setOnClickListener {
            val intent = Intent(requireContext(), SearchActivity::class.java)
            startActivity(intent)
        }
    }

    private fun updateUI(progressBar: ProgressBar, recyclerView: RecyclerView, emptyCard: CardView, state: String) {
        when (state) {
            "loading" -> {
                progressBar.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
                emptyCard.visibility = View.GONE
            }
            "success" -> {
                progressBar.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                emptyCard.visibility = View.GONE
            }
            "empty" -> {
                progressBar.visibility = View.GONE
                recyclerView.visibility = View.GONE
                emptyCard.visibility = View.VISIBLE
            }
        }
    }

    private fun adjustRecyclerViewForScreenSize(recyclerView: RecyclerView) {
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels

        val columnCount = when {
            screenWidth >= 1800 -> 4
            screenWidth >= 1200 -> 3
            screenWidth >= 800 -> 2
            else -> 2
        }

        val itemWidth = (screenWidth / columnCount) - 20
        val layoutParams = recyclerView.layoutParams
        layoutParams.width = itemWidth * columnCount
        recyclerView.layoutParams = layoutParams
    }

    private fun getDummyProducts(): List<ProductCard> {
        return listOf(
            ProductCard(
                productId = 1,
                image = "https://images.pexels.com/photos/13316724/pexels-photo-13316724.jpeg?auto=compress&cs=tinysrgb&w=600",
                name = "Kemeja Flanel Bekas",
                category = "Fashion Pria",
                price = 50000.0
            ),
            ProductCard(
                productId = 2,
                image = "https://images.pexels.com/photos/8532616/pexels-photo-8532616.jpeg?auto=compress&cs=tinysrgb&w=600",
                name = "Kaos Polos Hitam",
                category = "Fashion Pria",
                price = 30000.0
            ),
            ProductCard(
                productId = 3,
                image = "https://images.pexels.com/photos/19852011/pexels-photo-19852011/free-photo-of-kota-mode-fashion-fesyen.jpeg?auto=compress&cs=tinysrgb&w=600",
                name = "Dress Cantik Bekas",
                category = "Fashion Wanita",
                price = 75000.0
            ),
            ProductCard(
                productId = 4,
                image = "https://images.pexels.com/photos/3324443/pexels-photo-3324443.jpeg?auto=compress&cs=tinysrgb&w=600",
                name = "Jeans Sobek Stylish",
                category = "Fashion Pria",
                price = 120000.0
            ),
            ProductCard(
                productId = 5,
                image = "https://images.pexels.com/photos/27035625/pexels-photo-27035625/free-photo-of-stiletto-dan-carteras.jpeg?auto=compress&cs=tinysrgb&w=600",
                name = "Tas Kulit Bekas",
                category = "Aksesoris",
                price = 150000.0
            ),
            ProductCard(
                productId = 6,
                image = "https://images.pexels.com/photos/6046212/pexels-photo-6046212.jpeg?auto=compress&cs=tinysrgb&w=600",
                name = "Sepatu Sneakers Bekas",
                category = "Fashion Pria",
                price = 200000.0
            ),
            ProductCard(
                productId = 7,
                image = "https://images.pexels.com/photos/29738017/pexels-photo-29738017.jpeg?auto=compress&cs=tinysrgb&w=600",
                name = "Jaket Denim Bekas",
                category = "Fashion Pria",
                price = 180000.0
            ),
            ProductCard(
                productId = 8,
                image = "https://images.pexels.com/photos/6069974/pexels-photo-6069974.jpeg?auto=compress&cs=tinysrgb&w=600",
                name = "Rok Mini Bekas",
                category = "Fashion Wanita",
                price = 60000.0
            ),
            ProductCard(
                productId = 9,
                image = "https://images.pexels.com/photos/1619779/pexels-photo-1619779.jpeg?auto=compress&cs=tinysrgb&w=600",
                name = "Topi Fedora Bekas",
                category = "Aksesoris",
                price = 45000.0
            ),
            ProductCard(
                productId = 10,
                image = "https://www.pexels.com/id-id/foto/tas-ransel-biru-dan-coklat-di-tanah-2905238/",
                name = "Tas Ransel Bekas",
                category = "Aksesoris",
                price = 125000.0
            ),
            ProductCard(
                productId = 11,
                image = "https://images.pexels.com/photos/29625971/pexels-photo-29625971/free-photo-of-potret-pria-bergaya-berbaju-batik-dengan-tas.jpeg?auto=compress&cs=tinysrgb&w=600",
                name = "Kemeja Batik Bekas",
                category = "Fashion Pria",
                price = 70000.0
            ),
            ProductCard(
                productId = 12,
                image = "https://images.pexels.com/photos/29717200/pexels-photo-29717200/free-photo-of-potret-wanita-muda-berhijab-kuning.jpeg?auto=compress&cs=tinysrgb&w=600",
                name = "Syal Wool Bekas",
                category = "Aksesoris",
                price = 50000.0
            ),
            ProductCard(
                productId = 13,
                image = "https://images.pexels.com/photos/60619/boot-leather-shoe-old-60619.jpeg?auto=compress&cs=tinysrgb&w=600",
                name = "Sepatu Boots Bekas",
                category = "Fashion Pria",
                price = 250000.0
            ),
            ProductCard(
                productId = 14,
                image = "https://images.pexels.com/photos/29716847/pexels-photo-29716847.jpeg?auto=compress&cs=tinysrgb&w=600",
                name = "Gaun Pesta Bekas",
                category = "Fashion Wanita",
                price = 350000.0
            ),
            ProductCard(
                productId = 15,
                image = "https://images.pexels.com/photos/3808128/pexels-photo-3808128.jpeg?auto=compress&cs=tinysrgb&w=600",
                name = "Kaos Motif Garis",
                category = "Fashion Pria",
                price = 40000.0
            ),
            ProductCard(
                productId = 16,
                image = "https://images.pexels.com/photos/1852482/pexels-photo-1852482.jpeg?auto=compress&cs=tinysrgb&w=600",
                name = "Jaket Kulit Bekas",
                category = "Fashion Pria",
                price = 220000.0
            ),
            ProductCard(
                productId = 17,
                image = "https://images.pexels.com/photos/1007018/pexels-photo-1007018.jpeg?auto=compress&cs=tinysrgb&w=600",
                name = "Rok Lipit Bekas",
                category = "Fashion Wanita",
                price = 65000.0
            ),
            ProductCard(
                productId = 18,
                image = "https://images.pexels.com/photos/29743784/pexels-photo-29743784/free-photo-of-wanita-muda-dengan-payung-daun-di-alam.jpeg?auto=compress&cs=tinysrgb&w=600",
                name = "Gaun Santai Bekas",
                category = "Fashion Wanita",
                price = 85000.0
            ),
            ProductCard(
                productId = 19,
                image = "https://images.pexels.com/photos/11606666/pexels-photo-11606666.jpeg?auto=compress&cs=tinysrgb&w=600",
                name = "Sandal Jepit Stylish",
                category = "Fashion Pria",
                price = 25000.0
            ),
            ProductCard(
                productId = 20,
                image = "https://images.pexels.com/photos/5698908/pexels-photo-5698908.jpeg?auto=compress&cs=tinysrgb&w=600",
                name = "Topi Snapback Bekas",
                category = "Aksesoris",
                price = 50000.0
            )
        )
    }

}
