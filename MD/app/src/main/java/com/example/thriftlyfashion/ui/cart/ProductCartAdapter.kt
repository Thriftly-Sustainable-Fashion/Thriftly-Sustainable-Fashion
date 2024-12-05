package com.example.thriftlyfashion.ui.cart

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.thriftlyfashion.Product
import com.example.thriftlyfashion.R

class ProductCartAdapter(
    private val context: Context,
    private val productList: List<Product>,
    private val onDeleteClickListener: (Int) -> Unit,
    private val onCheckBoxClickListener: (Int, Boolean) -> Unit
) : RecyclerView.Adapter<ProductCartAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
        val productName: TextView = itemView.findViewById(R.id.id_productName)
        val productCategory: TextView = itemView.findViewById(R.id.id_productCategory)
        val productSize: TextView = itemView.findViewById(R.id.id_productSize)
        val productColor: TextView = itemView.findViewById(R.id.id_productColor)
        val productAmount: TextView = itemView.findViewById(R.id.id_productAmount)
        val productPrice: TextView = itemView.findViewById(R.id.id_productPrice)
        val productImage: ImageView = itemView.findViewById(R.id.id_productImage)
        val deleteProduct: ImageView = itemView.findViewById(R.id.id_deleteProduct)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_product_cart, parent, false)
        return ProductViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]

        holder.productName.text = product.name
        holder.productCategory.text = product.category
        holder.productSize.text = ": " + product.size
        holder.productColor.text = ": " + product.color
        holder.productAmount.text = ": " + product.amount
        holder.productPrice.text = product.price
        holder.productImage.setImageResource(product.image)

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            onCheckBoxClickListener(position, isChecked)
        }

        holder.deleteProduct.setOnClickListener {
            onDeleteClickListener(position)
        }
    }

    override fun getItemCount(): Int {
        return productList.size
    }
}
