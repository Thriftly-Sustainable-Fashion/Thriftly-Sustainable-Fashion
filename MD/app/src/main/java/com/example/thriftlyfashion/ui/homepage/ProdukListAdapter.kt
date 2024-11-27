package com.example.thriftlyfashion.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.thriftlyfashion.Product
import com.example.thriftlyfashion.R

class ProductListAdapter(
    private val context: Context,
    private val productList: List<Product>
) : RecyclerView.Adapter<ProductListAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.imageView3)
        val favoriteIcon: ImageView = itemView.findViewById(R.id.imageView4)
        val productName: TextView = itemView.findViewById(R.id.id_productName)
        val productCategory: TextView = itemView.findViewById(R.id.id_productCategory)
        val productPrice: TextView = itemView.findViewById(R.id.id_productName3)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]

        holder.productImage.setImageResource(product.image)
        holder.productName.text = product.name
        holder.productCategory.text = product.category
        holder.productPrice.text = product.price

        holder.favoriteIcon.setColorFilter(context.getColor(R.color.white))
    }

    override fun getItemCount(): Int = productList.size
}
