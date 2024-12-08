package com.example.thriftlyfashion.ui.homepage

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.thriftlyfashion.R
import com.example.thriftlyfashion.model.Product
import com.example.thriftlyfashion.ui.product.ProductDetailActivity

class ProductListAdapter(
    private val context: Context,
    private val productList: List<Product>
) : RecyclerView.Adapter<ProductListAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.id_productImage)
        val favoriteIcon: ImageView = itemView.findViewById(R.id.id_favorite)
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

        Glide.with(context)
            .load(product.images)
            .placeholder(R.drawable.image)
            .into(holder.productImage)

        holder.productName.text = product.name
        holder.productCategory.text = product.category
        holder.productPrice.text = "Rp ${String.format("%,.0f", product.price)}"

        holder.favoriteIcon.setColorFilter(context.getColor(R.color.white))

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ProductDetailActivity::class.java).apply {
                putExtra("PRODUCT_ID", product.productId)
                putExtra("STORE_ID", product.storeId)
                putExtra("PRODUCT_NAME", product.name)
                putExtra("PRODUCT_CATEGORY", product.category)
                putExtra("PRODUCT_PRICE", product.price)
                putExtra("PRODUCT_IMAGE", product.images)
                putExtra("PRODUCT_DESCRIPTION", product.description)
                putExtra("PRODUCT_COLOR", product.color)
                putExtra("PRODUCT_SIZE", product.size)
                putExtra("PRODUCT_QUANTITY", product.quantity)
                putExtra("PRODUCT_CREATEAT", product.createdAt)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = productList.size
}
