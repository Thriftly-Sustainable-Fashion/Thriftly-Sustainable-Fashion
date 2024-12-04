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

    // ViewHolder yang merepresentasikan satu item dalam RecyclerView
    inner class HeroViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.cardView)
        val heroImage: ImageView = itemView.findViewById(R.id.imageView)
    }

    // Membuat tampilan item menggunakan layout inflater
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeroViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_hero, parent, false)
        return HeroViewHolder(view)
    }

    // Menghubungkan data dengan ViewHolder
    override fun onBindViewHolder(holder: HeroViewHolder, position: Int) {
        val imageResource = heroImages[position]
        holder.heroImage.setImageResource(imageResource)

        // Tambahkan margin kiri 50dp untuk item pertama
        val layoutParams = holder.cardView.layoutParams as ViewGroup.MarginLayoutParams
        if (position == 0) {
            layoutParams.marginStart = (30 * context.resources.displayMetrics.density).toInt()
            layoutParams.marginEnd = (20 * context.resources.displayMetrics.density).toInt()
        }else{
            layoutParams.marginEnd = (20 * context.resources.displayMetrics.density).toInt()
        }
        holder.cardView.layoutParams = layoutParams
    }


    // Mendapatkan jumlah item dalam RecyclerView
    override fun getItemCount(): Int = heroImages.size
}
