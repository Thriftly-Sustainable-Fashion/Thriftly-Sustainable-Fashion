package com.example.thriftlyfashion.ui.payment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.thriftlyfashion.PaymentMethod
import com.example.thriftlyfashion.R

class PaymentMethodAdapter(
    private val paymentMethods: List<PaymentMethod>,
    private val onClick: (PaymentMethod) -> Unit
) : RecyclerView.Adapter<PaymentMethodAdapter.PaymentMethodViewHolder>() {

    inner class PaymentMethodViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val logo: ImageView = view.findViewById(R.id.logo)
        val name: TextView = view.findViewById(R.id.nama)
        val cardView: CardView = view as CardView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentMethodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_payment_method, parent, false)
        return PaymentMethodViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaymentMethodViewHolder, position: Int) {
        val paymentMethod = paymentMethods[position]
        holder.logo.setImageResource(paymentMethod.logoResId)
        holder.name.text = paymentMethod.name

        holder.cardView.setOnClickListener {
            onClick(paymentMethod)
        }
    }

    override fun getItemCount(): Int = paymentMethods.size
}
