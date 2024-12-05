package com.example.thriftlyfashion.ui.homepage

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.thriftlyfashion.Product
import com.example.thriftlyfashion.R
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexDirection

class HomepageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage)

        val recyclerView: RecyclerView = findViewById(R.id.shopRecyclerView)

        val productImages = listOf(
            R.drawable.ic_shop,
            R.drawable.ic_shop,
            R.drawable.ic_shop,
            R.drawable.ic_shop,
            R.drawable.ic_shop
        )

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager

        val adapter = ShopListAdapter(this, productImages)
        recyclerView.adapter = adapter

        recyclerView.setHasFixedSize(true)


        val heroRecyclerView: RecyclerView = findViewById(R.id.heroRecyclerView)

        val heroImages = listOf(
            R.drawable.hero1,
            R.drawable.hero2,
            R.drawable.hero3,
            R.drawable.hero4
        )

        val heroLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        heroRecyclerView.layoutManager = heroLayoutManager

        val heroAdapter = HeroListAdapter(this, heroImages)
        heroRecyclerView.adapter = heroAdapter


        val productLayoutManager = FlexboxLayoutManager(this)
        productLayoutManager.flexWrap = FlexWrap.WRAP
        productLayoutManager.flexDirection = FlexDirection.ROW
        val productRecyclerView: RecyclerView = findViewById(R.id.id_productList)
        productRecyclerView.layoutManager = productLayoutManager

        val productList = listOf(
            Product(R.drawable.image, "Product 1", "Category 1", "Rp 500.000", "", "", ""),
            Product(R.drawable.image, "Product 1", "Category 1", "Rp 500.000", "", "", ""),
            Product(R.drawable.image, "Product 1", "Category 1", "Rp 500.000", "", "", ""),
            Product(R.drawable.image, "Product 1", "Category 1", "Rp 500.000", "", "", ""),
            Product(R.drawable.image, "Product 1", "Category 1", "Rp 500.000", "", "", ""),
            Product(R.drawable.image, "Product 2", "Category 2", "Rp 750.000", "", "", "")
        )

        val productAdapter = ProductListAdapter(this, productList)
        productRecyclerView.adapter = productAdapter



    }
}
