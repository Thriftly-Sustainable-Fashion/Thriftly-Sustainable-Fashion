package com.example.thriftlyfashion.ui.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.thriftlyfashion.R
import com.example.thriftlyfashion.database.DatabaseHelper
import com.example.thriftlyfashion.model.CartItem
import java.text.NumberFormat
import java.util.Locale

class CartFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productCartAdapter: ProductCartAdapter
    private val selectedProducts = mutableListOf<Pair<String, Double>>()

    private lateinit var subtotalTextView: TextView
    private lateinit var discountTextView: TextView
    private lateinit var totalPriceTextView: TextView
    private lateinit var totalSelectedItemsTextView: TextView
    private lateinit var emptyCartTextView: LinearLayout
    private lateinit var cartView: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_cart, container, false)

        recyclerView = rootView.findViewById(R.id.id_productCart)
        subtotalTextView = rootView.findViewById(R.id.id_subtotal)
        discountTextView = rootView.findViewById(R.id.id_totalDiscount)
        totalPriceTextView = rootView.findViewById(R.id.id_totalPrice)
        totalSelectedItemsTextView = rootView.findViewById(R.id.id_total_selected_items)
        emptyCartTextView = rootView.findViewById(R.id.id_emptyCart)
        cartView = rootView.findViewById(R.id.id_cartView)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val dbHelper = DatabaseHelper(requireContext())
        val cartItems = dbHelper.getAllCartItems().toMutableList()

        productCartAdapter = ProductCartAdapter(requireContext(), cartItems,
            onDeleteClickListener = { id, position ->
                val productIdToRemove = cartItems[position].productId

                cartItems.removeAt(position)
                productCartAdapter.notifyItemRemoved(position)

                dbHelper.deleteCartItem(id)

                selectedProducts.removeIf { it.first == productIdToRemove }

                updateCartVisibility(cartItems)
                calculateTotals()
            },
            onCheckBoxClickListener = { productId, totalPrice, isChecked ->
                if (isChecked) {
                    selectedProducts.add(Pair(productId, totalPrice))
                } else {
                    selectedProducts.removeIf { it.first == productId }
                }
                calculateTotals()
            }
        )

        recyclerView.adapter = productCartAdapter

        updateCartVisibility(cartItems)

        val paymentMethodCard: View = rootView.findViewById(R.id.id_paymentMethod)
        paymentMethodCard.setOnClickListener {
            // Navigate to the payment page here
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        populateRecipientAddress(
            recipientName = "Jane Doe",
            address1 = "Jawa Barat - Bandung",
            address2 = "Cicendo - Pasteur - 40181"
        )
    }

    private fun calculateTotals() {
        val subtotal = selectedProducts.sumOf { it.second }
        val discount = subtotal * 0.10
        val totalPrice = subtotal - discount

        val itemCount = selectedProducts.size
        totalSelectedItemsTextView.text = "$itemCount barang terpilih"

        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        format.maximumFractionDigits = 0

        subtotalTextView.text = ": ${format.format(subtotal)}"
        discountTextView.text = ": ${format.format(discount)}"
        totalPriceTextView.text = ": ${format.format(totalPrice)}"
    }

    private fun populateRecipientAddress(
        recipientName: String,
        address1: String,
        address2: String
    ) {
        val recipientNameTextView: TextView = requireView().findViewById(R.id.id_recipientName)
        val address1TextView: TextView = requireView().findViewById(R.id.id_address1)
        val address2TextView: TextView = requireView().findViewById(R.id.id_address2)

        recipientNameTextView.text = recipientName
        address1TextView.text = address1
        address2TextView.text = address2
    }

    private fun updateCartVisibility(cartItems: List<CartItem>) {
        if (cartItems.isEmpty()) {
            emptyCartTextView.visibility = View.VISIBLE
            cartView.visibility = View.GONE
        } else {
            emptyCartTextView.visibility = View.GONE
            cartView.visibility = View.VISIBLE
        }
    }

}
