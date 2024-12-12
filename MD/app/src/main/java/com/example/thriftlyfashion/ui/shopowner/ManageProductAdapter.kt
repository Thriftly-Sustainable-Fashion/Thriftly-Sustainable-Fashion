import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.thriftlyfashion.R
import com.example.thriftlyfashion.remote.model.ManageProduct

class ManageProductAdapter(
    private val context: Context,
    private val productList: List<ManageProduct>,
    private val listener: OnProductClickListener
) : RecyclerView.Adapter<ManageProductAdapter.ProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_manage_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]

        holder.productName.text = product.name
        holder.productCategory.text = product.category
        holder.productSize.text = "Size: ${product.size}"
        holder.productColor.text = "Color: ${product.color}"
        holder.productQuantity.text = "Qty: ${product.quantity}"
        holder.productPrice.text = "Rp ${product.price}"

        holder.productStatus.text = product.status

        if (product.images.isNotEmpty()) {
            Glide.with(context)
                .load(product.images[0]) // Load the first image
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.productImage)
        }

        holder.deleteProduct.setOnClickListener {
            listener.onDeleteClick(product)
        }
        holder.editProduct.setOnClickListener {
            listener.onEditClick(product)
        }
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productName: TextView = itemView.findViewById(R.id.productName)
        val productCategory: TextView = itemView.findViewById(R.id.productCategory)
        val productSize: TextView = itemView.findViewById(R.id.productSize)
        val productColor: TextView = itemView.findViewById(R.id.productColor)
        val productQuantity: TextView = itemView.findViewById(R.id.productQuantity)
        val productPrice: TextView = itemView.findViewById(R.id.productPrice)
        val productStatus: TextView = itemView.findViewById(R.id.Status)
        val productImage: ImageView = itemView.findViewById(R.id.productImage)
        val deleteProduct: ImageView = itemView.findViewById(R.id.ic_deleteProduct)
        val editProduct: ImageView = itemView.findViewById(R.id.ic_editProduct)
    }

    interface OnProductClickListener {
        fun onDeleteClick(product: ManageProduct)
        fun onEditClick(product: ManageProduct)
    }
}
