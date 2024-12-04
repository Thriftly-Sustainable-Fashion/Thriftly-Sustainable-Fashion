package com.example.thriftlyfashion.ui.cart

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.thriftlyfashion.Product
import com.example.thriftlyfashion.R

class CartFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productCartAdapter: ProductCartAdapter
    private val productList = mutableListOf<Product>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_cart, container, false)

        // Initialize RecyclerView
        recyclerView = rootView.findViewById(R.id.id_productCart)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        productList.add(Product(R.drawable.image, "Product 1", "Category 1", "$100", "M", "Red", "10"))
        productList.add(Product(R.drawable.image, "Product 2", "Category 2", "$120", "L", "Blue", "5"))

        // Setup adapter
        productCartAdapter = ProductCartAdapter(requireContext(), productList,
            onDeleteClickListener = { position ->
                productList.removeAt(position)
                productCartAdapter.notifyItemRemoved(position)
            },
            onCheckBoxClickListener = { position, isChecked ->
                val product = productList[position]
            }
        )

        recyclerView.adapter = productCartAdapter

        val paymentMethodCard: View = rootView.findViewById(R.id.id_paymentMethod)
        paymentMethodCard.setOnClickListener {
            // Intent to navigate to ActivityPaymentMethod
            val intent = Intent(requireContext(), PaymentMethodActivity::class.java)
            startActivity(intent)
        }

        return rootView
    }
}
