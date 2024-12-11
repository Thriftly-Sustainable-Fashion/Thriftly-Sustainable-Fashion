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
import com.bumptech.glide.Glide
import com.example.thriftlyfashion.R
import com.example.thriftlyfashion.remote.model.CartItem

class ProductCartAdapter(
    private val context: Context,
    private var cartItems: List<CartItem>,
    private val onDeleteClickListener: (Int, Int) -> Unit,
    private val onCheckBoxClickListener: (Int, Double, Boolean) -> Unit
) : RecyclerView.Adapter<ProductCartAdapter.CartViewHolder>() {

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productName: TextView = itemView.findViewById(R.id.id_productName)
        val productPrice: TextView = itemView.findViewById(R.id.id_productPrice)
        val productCategory: TextView = itemView.findViewById(R.id.id_productCategory)
        val productSize: TextView = itemView.findViewById(R.id.id_productSize)
        val productColor: TextView = itemView.findViewById(R.id.id_productColor)
        val productAmount: TextView = itemView.findViewById(R.id.id_productAmount)
        val productImage: ImageView = itemView.findViewById(R.id.id_productImage)
        val deleteButton: ImageView = itemView.findViewById(R.id.id_deleteProduct)
        val checkBox: CheckBox = itemView.findViewById(R.id.id_checkBox)
    }

    fun updateCartItems(newCartItems: List<CartItem>) {
        this.cartItems = newCartItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_product_cart, parent, false)
        return CartViewHolder(view)
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = cartItems[position]

        holder.productName.text = item.name
        holder.productPrice.text = "Rp ${String.format("%,.0f", item.totalPrice)}"
        holder.productCategory.text = item.category
        holder.productSize.text = ": ${item.size}"
        holder.productColor.text = ": ${item.color}"
        holder.productAmount.text = ": ${item.quantity}"

        Glide.with(context)
            .load(item.image)
            .placeholder(R.drawable.image)
            .into(holder.productImage)

        holder.deleteButton.setOnClickListener {
            onDeleteClickListener(item.id, position)
        }

        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = false
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            onCheckBoxClickListener(item.productId, item.totalPrice, isChecked)
        }
    }


    override fun getItemCount(): Int {
        return cartItems.size
    }
}
