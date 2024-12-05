package com.example.thriftlyfashion.ui.homepage

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.thriftlyfashion.R

class ShopListAdapter(private val context: Context, private val itemList: List<Int>) :
    RecyclerView.Adapter<ShopListAdapter.ShopViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_shop, parent, false)
        return ShopViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShopViewHolder, position: Int) {
        val productImage = holder.productImage
        productImage.setImageResource(itemList[position])
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class ShopViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productImage: ImageView = view.findViewById(R.id.product_image)
    }
}

