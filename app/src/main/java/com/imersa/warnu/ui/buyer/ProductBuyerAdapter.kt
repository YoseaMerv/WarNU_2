package com.imersa.warnu.ui.buyer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.imersa.warnu.R

class ProductBuyerAdapter(
    private val onItemClick: (ProductBuyer) -> Unit
) : ListAdapter<ProductBuyer, ProductBuyerAdapter.ViewHolder>(ProdukDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_buyer, parent, false)
        return ViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        itemView: View,
        val onItemClick: (ProductBuyer) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val imgProduk: ImageView = itemView.findViewById(R.id.imgProdukBuyer)
        private val tvNama: TextView = itemView.findViewById(R.id.tvNamaProdukBuyer)
        private val tvHarga: TextView = itemView.findViewById(R.id.tvHargaProdukBuyer)

        fun bind(produk: ProductBuyer) {
            tvNama.text = produk.name ?: "-"
            val priceText = if (produk.price != null) {
                "Rp ${String.format("%,.0f", produk.price)}"
            } else {
                "Harga belum tersedia"
            }
            tvHarga.text = priceText

            Glide.with(itemView.context)
                .load(produk.imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .into(imgProduk)

            itemView.setOnClickListener {
                onItemClick(produk)
            }
        }
    }

    class ProdukDiffCallback : DiffUtil.ItemCallback<ProductBuyer>() {
        override fun areItemsTheSame(oldItem: ProductBuyer, newItem: ProductBuyer): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ProductBuyer, newItem: ProductBuyer): Boolean {
            return oldItem == newItem
        }
    }
}
