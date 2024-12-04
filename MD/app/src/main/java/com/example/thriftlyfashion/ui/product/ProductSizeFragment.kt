package com.example.thriftlyfashion.ui.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.thriftlyfashion.R

class ProductSizeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var sizeAdapter: SizeAdapter
    private val sizes = listOf("S", "M", "L")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = inflater.inflate(R.layout.fragment_product_size, container, false)

        recyclerView = binding.findViewById(R.id.recyclerView)

        sizeAdapter = SizeAdapter(requireContext(), sizes) { size ->
            (activity as? ProductDetailActivity)?.updateSelectedData(color = null, size = size, category = null)

            Toast.makeText(context, "Pilih ukuran: $size", Toast.LENGTH_SHORT).show()
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = sizeAdapter

        return binding
    }
}
