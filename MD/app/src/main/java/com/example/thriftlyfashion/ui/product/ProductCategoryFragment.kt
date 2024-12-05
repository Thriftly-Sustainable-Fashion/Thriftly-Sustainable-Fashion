package com.example.thriftlyfashion.ui.product

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.thriftlyfashion.R

class ProductCategoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter
    private val categories = listOf("Shirts", "Pants", "Jackets", "Shoes")

    private var selectedCategory: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_product_category, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)

        categoryAdapter = CategoryAdapter(requireContext(), categories) { category ->
            selectedCategory = category
            Toast.makeText(requireContext(), "Selected Category: $category", Toast.LENGTH_SHORT).show()
        }

        recyclerView.adapter = categoryAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)

        return view
    }

    private fun sendDataToProductDetailActivity() {
        val intent = Intent(requireContext(), ProductDetailActivity::class.java).apply {
            putExtra("SELECTED_CATEGORY", selectedCategory)
        }
        startActivity(intent)
    }
}
