package com.example.thriftlyfashion.ui.product

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.thriftlyfashion.R

class ProductColorFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ColorsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_product_color, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)

        // Sample list of colors
        val colors = listOf(
            ColorItem("Red", android.graphics.Color.parseColor("#FF0000")),
            ColorItem("Blue", android.graphics.Color.parseColor("#0000FF")),
            ColorItem("Green", android.graphics.Color.parseColor("#008000")),
            ColorItem("Yellow", android.graphics.Color.parseColor("#FFFF00")),
            ColorItem("Purple", android.graphics.Color.parseColor("#800080"))
        )

        adapter = ColorsAdapter(requireContext(), colors) { colorItem ->
            (activity as? ProductDetailActivity)?.updateSelectedData(color = colorItem.name, size = null, category = null)

            Toast.makeText(requireContext(), "Selected: ${colorItem.name}", Toast.LENGTH_SHORT).show()
        }

        // Set up the RecyclerView
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)

        return view
    }
}
