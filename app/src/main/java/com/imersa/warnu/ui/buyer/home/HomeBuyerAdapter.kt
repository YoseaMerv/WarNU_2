package com.imersa.warnu.ui.buyer.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.imersa.warnu.R
import com.imersa.warnu.data.model.Product
import java.text.NumberFormat
import java.util.Locale

// Tambahkan lambda onAddToCartClick
class HomeBuyerAdapter(
    private val onItemClick: (Product) -> Unit,
    private val onAddToCartClick: (Product) -> Unit
) : ListAdapter<Product, HomeBuyerAdapter.ProductViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product_buyer, parent, false)
        return ProductViewHolder(view, onItemClick, onAddToCartClick)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ProductViewHolder(
        itemView: View,
        private val onItemClick: (Product) -> Unit,
        private val onAddToCartClick: (Product) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val ivProductImage: ImageView = itemView.findViewById(R.id.iv_product_buyer)
        private val tvProductName: TextView = itemView.findViewById(R.id.tv_product_name_buyer)
        private val tvProductPrice: TextView = itemView.findViewById(R.id.tv_product_price_buyer)
        // Referensi ke tombol baru
        private val btnAddToCart: Button = itemView.findViewById(R.id.btn_add_to_cart)

        @SuppressLint("SetTextI18n")
        fun bind(product: Product) {
            tvProductName.text = product.name
            val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            tvProductPrice.text = formatter.format(product.price)

            Glide.with(itemView.context)
                .load(product.imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .into(ivProductImage)

            // Listener untuk seluruh item
            itemView.setOnClickListener {
                onItemClick(product)
            }

            // Listener khusus untuk tombol "Add to Cart"
            btnAddToCart.setOnClickListener {
                onAddToCartClick(product)
            }
        }
    }

    class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
}