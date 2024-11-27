package com.example.thriftlyfashion.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.thriftlyfashion.Product
import com.example.thriftlyfashion.R
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexDirection

class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = view.findViewById(R.id.shopRecyclerView)

        val productImages = listOf(
            R.drawable.baseline_shop_24,
            R.drawable.baseline_shop_24,
            R.drawable.baseline_shop_24,
            R.drawable.baseline_shop_24,
            R.drawable.baseline_shop_24
        )

        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager

        val adapter = ShopListAdapter(requireContext(), productImages)
        recyclerView.adapter = adapter

        recyclerView.setHasFixedSize(true)

        // Hero RecyclerView
        val heroRecyclerView: RecyclerView = view.findViewById(R.id.heroRecyclerView)

        val heroImages = listOf(
            R.drawable.hero1,
            R.drawable.hero2,
            R.drawable.hero3,
            R.drawable.hero4
        )

        val heroLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        heroRecyclerView.layoutManager = heroLayoutManager

        val heroAdapter = HeroListAdapter(requireContext(), heroImages)
        heroRecyclerView.adapter = heroAdapter

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(heroRecyclerView)

        heroRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val centerX = recyclerView.width / 2

                for (i in 0 until recyclerView.childCount) {
                    val child = recyclerView.getChildAt(i) ?: continue
                    val childCenterX = (child.left + child.right) / 2
                    val distanceFromCenter = Math.abs(centerX - childCenterX)

                    val scale = 1 - (distanceFromCenter.toFloat() / recyclerView.width.toFloat())

                    val minScale = 0.6f
                    val maxScale = 1.0f
                    val interpolatedScale = minScale + scale * (maxScale - minScale)

                    child.scaleX = interpolatedScale
                    child.scaleY = interpolatedScale
                }
            }
        })


        val productLayoutManager = FlexboxLayoutManager(requireContext())
        productLayoutManager.flexWrap = FlexWrap.WRAP
        productLayoutManager.flexDirection = FlexDirection.ROW

        val productRecyclerView: RecyclerView = view.findViewById(R.id.id_productList)
        productRecyclerView.layoutManager = productLayoutManager

        val productList = listOf(
            Product(R.drawable.image, "Product 1", "Category 1", "Rp 500.000"),
            Product(R.drawable.image, "Product 1", "Category 1", "Rp 500.000"),
            Product(R.drawable.image, "Product 1", "Category 1", "Rp 500.000"),
            Product(R.drawable.image, "Product 1", "Category 1", "Rp 500.000"),
            Product(R.drawable.image, "Product 1", "Category 1", "Rp 500.000"),
            Product(R.drawable.image, "Product 2", "Category 2", "Rp 750.000")
        )

        val productAdapter = ProductListAdapter(requireContext(), productList)
        productRecyclerView.adapter = productAdapter
    }
}
