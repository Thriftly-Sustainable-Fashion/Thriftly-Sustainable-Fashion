package com.example.thriftlyfashion.ui.homepage

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.thriftlyfashion.R

class ShopListAdapter(private val context: Context, private val itemList: List<String>) :
    RecyclerView.Adapter<ShopListAdapter.ShopViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_shop, parent, false)
        return ShopViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShopViewHolder, position: Int) {
        val productImage = holder.productImage

        Glide.with(context)
            .load(itemList[position])
            .placeholder(R.drawable.ic_shop)
            .error(R.drawable.ic_shop)
            .into(productImage)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class ShopViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productImage: ImageView = view.findViewById(R.id.product_image)
    }
}
