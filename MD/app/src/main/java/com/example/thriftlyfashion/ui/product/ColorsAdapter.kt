package com.example.thriftlyfashion.ui.product

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.thriftlyfashion.R

class ColorsAdapter(
    private val context: Context,
    private val colors: List<ColorItem>,
    private val onColorClick: (ColorItem) -> Unit
) : RecyclerView.Adapter<ColorsAdapter.ColorViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_colors, parent, false)
        return ColorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        val colorItem = colors[position]
        holder.bind(colorItem)
        holder.itemView.setOnClickListener { onColorClick(colorItem) }
    }

    override fun getItemCount(): Int = colors.size

    class ColorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val colorName: TextView = itemView.findViewById(R.id.colorName)
        private val colorView: CardView = itemView.findViewById(R.id.colorView)

        fun bind(colorItem: ColorItem) {
            colorView.setCardBackgroundColor(colorItem.colorCode)
            colorName.text = colorItem.name
        }
    }
}

data class ColorItem(
    val name: String,
    val colorCode: Int
)
