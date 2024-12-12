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
            R.drawable.ic_shop,
            R.drawable.ic_shop,
            R.drawable.ic_shop,
            R.drawable.ic_shop,
            R.drawable.ic_shop
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

                if (response.isSuccessful) {
                    val allProducts = response.body() ?: emptyList()

                    val productList = allProducts.map { product ->
                        ProductCard(
                            productId = product.productId,
                            image = product.image,
                            name = product.name,
                            category = product.category,
                            price = product.price,
                        )
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
                } else {
                    Toast.makeText(requireContext(), "Gagal mengambil data produk", Toast.LENGTH_SHORT).show()
                    Log.e("HomeFragment", "API error: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<List<ProductCard>>, t: Throwable) {
                if (!isAdded) return
                Toast.makeText(requireContext(), "Terjadi kesalahan: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("HomeFragment", "API failure: ${t.message}")
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
            else -> 1
        }

        val itemWidth = (screenWidth / columnCount) - 40
        val layoutParams = recyclerView.layoutParams
        layoutParams.width = itemWidth * columnCount
        recyclerView.layoutParams = layoutParams
    }
}
