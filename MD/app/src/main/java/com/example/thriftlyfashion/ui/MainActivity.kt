package com.example.thriftlyfashion.ui

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.thriftlyfashion.R
import com.example.thriftlyfashion.ui.homepage.HomeFragment
import com.example.thriftlyfashion.ui.cart.CartFragment
import com.example.thriftlyfashion.ui.news.NewsFragment
import com.example.thriftlyfashion.ui.profile.ProfileFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHome = findViewById<CardView>(R.id.nav_home)
        val navNews = findViewById<CardView>(R.id.nav_news)
        val navCart = findViewById<CardView>(R.id.nav_cart)
        val navProfile = findViewById<CardView>(R.id.nav_profile)

        val navItems = listOf(navHome, navNews, navCart, navProfile)

        if (savedInstanceState == null) {
            setActiveNav(navHome, navItems)
            replaceFragment(HomeFragment())
        }

        navHome.setOnClickListener {
            setActiveNav(navHome, navItems)
            replaceFragment(HomeFragment())
        }
        navNews.setOnClickListener {
            setActiveNav(navNews, navItems)
            replaceFragment(NewsFragment())
        }
        navCart.setOnClickListener {
            setActiveNav(navCart, navItems)
            replaceFragment(CartFragment())
        }
        navProfile.setOnClickListener {
            setActiveNav(navProfile, navItems)
            replaceFragment(ProfileFragment())
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .commit()
    }

    private fun setActiveNav(activeItem: CardView, navItems: List<CardView>) {
        navItems.forEach { item ->
            val linearLayout = item.getChildAt(0) as LinearLayout
            val icon = linearLayout.getChildAt(0) as ImageView
            val label = linearLayout.getChildAt(1) as TextView

            if (item == activeItem) {
                when (item.id) {
                    R.id.nav_home -> icon.setImageResource(R.drawable.ic_home_active)
                    R.id.nav_news -> icon.setImageResource(R.drawable.ic_news_active)
                    R.id.nav_cart -> icon.setImageResource(R.drawable.ic_cart_active)
                    R.id.nav_profile -> icon.setImageResource(R.drawable.ic_profile_active)
                }
                item.setCardBackgroundColor(getColor(R.color.white))
                label.setTextColor(getColor(R.color.black))
            } else {
                when (item.id) {
                    R.id.nav_home -> icon.setImageResource(R.drawable.ic_home_inactive)
                    R.id.nav_news -> icon.setImageResource(R.drawable.ic_news_inactive)
                    R.id.nav_cart -> icon.setImageResource(R.drawable.ic_cart_inactive)
                    R.id.nav_profile -> icon.setImageResource(R.drawable.ic_profile_inactive)
                }
                item.setCardBackgroundColor(getColor(android.R.color.transparent))
                label.setTextColor(getColor(R.color.black))
            }
        }
    }
}
