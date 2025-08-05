package com.imersa.warnu.ui.seller.product

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.imersa.warnu.R

class ProductAdapter(
    private val products: List<Product>,
    private val onItemClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvProductName)
        val price: TextView = view.findViewById(R.id.tvProductPrice)
        val image: ImageView = view.findViewById(R.id.ivProductImage)
        val description: TextView = view.findViewById(R.id.tvProductDescription)
        val stock: TextView = view.findViewById(R.id.tvStokProduk)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_dashboard, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.name.text = product.name
        holder.price.text = "Rp${product.price.toInt()}"

        val maxLength = 20
        val desc = product.description
        holder.description.text = if (desc.length > maxLength) {
            desc.take(maxLength) + "..."
        } else {
            desc
        }

        holder.stock.text = "Stok: ${product.stock}"

        if (product.imageUrl.isNotBlank()) {
            Glide.with(holder.itemView.context)
                .load(product.imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(holder.image)
        } else {
            holder.image.setImageResource(R.drawable.placeholder_image)
        }

        // ⬅️ Tambahkan ini!
        holder.itemView.setOnClickListener {
            onItemClick(product)
        }
    }

    override fun getItemCount() = products.size
}
