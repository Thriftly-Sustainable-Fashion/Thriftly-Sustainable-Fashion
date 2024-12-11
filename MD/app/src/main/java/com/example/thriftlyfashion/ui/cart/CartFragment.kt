package com.example.thriftlyfashion.ui.cart

import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.thriftlyfashion.R
import com.example.thriftlyfashion.remote.SharedPrefManager
import com.example.thriftlyfashion.remote.api.ApiService
import com.example.thriftlyfashion.remote.api.RetrofitClient
import com.example.thriftlyfashion.remote.model.CartItem
import java.text.NumberFormat
import java.util.Locale
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
    private var cartItems: List<CartItem> = emptyList()
    val apiService = RetrofitClient.createService(ApiService::class.java)

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

        val sharedPrefManager = SharedPrefManager(requireContext())
        val userId = sharedPrefManager.getUserId()

        Log.d("CartFragment", "Fetching cart items for userId: $userId")

        apiService.getCartItems(userId).enqueue(object : Callback<List<CartItem>> {
            override fun onResponse(call: Call<List<CartItem>>, response: Response<List<CartItem>>) {
                if (response.isSuccessful) {
                    cartItems = response.body() ?: emptyList()
                    Log.d("CartFragment", "API response: $cartItems")
                    if (cartItems.isEmpty()) {
                        Toast.makeText(requireContext(), "Keranjang kosong", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Data keranjang berhasil diambil", Toast.LENGTH_SHORT).show()
                    }

                    productCartAdapter = ProductCartAdapter(
                        requireContext(),
                        cartItems,
                        onDeleteClickListener = { id, position ->
                            deleteCartItem(id, position, cartItems)
                        },
                        onCheckBoxClickListener = { productId, totalPrice, isChecked ->
                            if (isChecked) {
                                selectedProducts.add(Pair(productId.toString(), totalPrice))
                            } else {
                                selectedProducts.removeIf { it.first == productId.toString() }
                            }
                            calculateTotals()
                        }
                    )
                    recyclerView.adapter = productCartAdapter
                    updateCartVisibility(cartItems)
                    calculateTotals()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("CartFragment", "API error: ${response.code()}, $errorBody")
                    Toast.makeText(requireContext(), "Gagal mengambil data keranjang", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<CartItem>>, t: Throwable) {
                Log.e("CartFragment", "API call failed", t)
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })

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


    private fun deleteCartItem(id: Int, position: Int, cartItems: List<CartItem>) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.custom_dialog_confirmation, null)

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirm)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            val productIdToRemove = cartItems[position].productId

            apiService.deleteCartItem(id).enqueue(object : Callback<Map<String, String>> {
                override fun onResponse(
                    call: Call<Map<String, String>>,
                    response: Response<Map<String, String>>
                ) {
                    if (response.isSuccessful) {
                        // Remove the item from the local cart list
                        val updatedCartItems = cartItems.toMutableList()
                        updatedCartItems.removeAt(position)

                        // Update the adapter with the new cart items list
                        productCartAdapter.updateCartItems(updatedCartItems)
                        selectedProducts.removeIf { it.first == productIdToRemove.toString() }

                        updateCartVisibility(updatedCartItems)
                        calculateTotals()

                        Toast.makeText(context, "Item berhasil dihapus", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    } else {
                        Toast.makeText(context, "Gagal menghapus item: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                    Toast.makeText(context, "Terjadi kesalahan: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        dialog.show()
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

    private fun updateCartVisibility(cartItems: List<CartItem>) {
        if (cartItems.isEmpty()) {
            emptyCartTextView.visibility = View.VISIBLE
            cartView.visibility = View.GONE
        } else {
            emptyCartTextView.visibility = View.GONE
            cartView.visibility = View.VISIBLE
        }
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
}
