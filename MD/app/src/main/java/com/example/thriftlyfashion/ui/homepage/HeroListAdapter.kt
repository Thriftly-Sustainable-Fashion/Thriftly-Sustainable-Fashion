package com.example.thriftlyfashion.ui.homepage

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.thriftlyfashion.R

class HeroListAdapter(
    private val context: Context,
    private val heroImages: List<Int>
) : RecyclerView.Adapter<HeroListAdapter.HeroViewHolder>() {

    inner class HeroViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.cardView)
        val heroImage: ImageView = itemView.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeroViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_hero, parent, false)
        return HeroViewHolder(view)
    }

    override fun onBindViewHolder(holder: HeroViewHolder, position: Int) {
        val imageResource = heroImages[position]
        holder.heroImage.setImageResource(imageResource)

        val layoutParams = holder.cardView.layoutParams as ViewGroup.MarginLayoutParams
        if (position == 0) {
            layoutParams.marginStart = (35 * context.resources.displayMetrics.density).toInt()
        }

        if (position == heroImages.size - 1) {
            layoutParams.marginEnd = (35 * context.resources.displayMetrics.density).toInt()
        }

        holder.cardView.layoutParams = layoutParams
    }

    override fun getItemCount(): Int = heroImages.size
}
