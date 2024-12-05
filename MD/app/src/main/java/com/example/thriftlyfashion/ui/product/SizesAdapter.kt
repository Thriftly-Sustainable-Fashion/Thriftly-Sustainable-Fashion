package com.example.thriftlyfashion.ui.product

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.thriftlyfashion.R

class SizeAdapter(
    private val context: Context,
    private val sizes: List<String>,
    private val onSizeClick: (String) -> Unit
) : RecyclerView.Adapter<SizeAdapter.SizeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SizeViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_sizes, parent, false)
        return SizeViewHolder(view)
    }

    override fun onBindViewHolder(holder: SizeViewHolder, position: Int) {
        val size = sizes[position]
        holder.bind(size)
        holder.itemView.setOnClickListener { onSizeClick(size) }
        holder.itemView.setOnClickListener {
            onSizeClick(size)
        }
    }

    override fun getItemCount(): Int = sizes.size

    class SizeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val sizeName: TextView = itemView.findViewById(R.id.sizeName)
        private val sizeView: CardView = itemView.findViewById(R.id.sizeView)

        fun bind(size: String) {
            sizeName.text = size
        }
    }
}
