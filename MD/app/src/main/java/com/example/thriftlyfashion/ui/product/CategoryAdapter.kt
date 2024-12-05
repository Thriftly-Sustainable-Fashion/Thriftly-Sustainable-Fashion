package com.example.thriftlyfashion.ui.product

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.thriftlyfashion.R

class CategoryAdapter(
    private val context: Context,
    private val categories: List<String>,
    private val onCategoryClick: (String) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.bind(category, position)
        holder.itemView.setOnClickListener {
            selectedPosition = if (selectedPosition == position) -1 else position
            onCategoryClick(category)

            // Send the selected category to ProductDetailActivity
            val intent = Intent(context, ProductDetailActivity::class.java).apply {
                putExtra("SELECTED_CATEGORY", category)
            }
            context.startActivity(intent)

            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int = categories.size

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryName: TextView = itemView.findViewById(R.id.categoryName)
        private val categoryCard: CardView = itemView.findViewById(R.id.categoryCard)

        fun bind(category: String, position: Int) {
            categoryName.text = category

            if (position == selectedPosition) {
                categoryCard.setCardBackgroundColor(context.getColor(R.color.primary))
            } else {
                categoryCard.setCardBackgroundColor(context.getColor(android.R.color.transparent))
            }
        }
    }
}
