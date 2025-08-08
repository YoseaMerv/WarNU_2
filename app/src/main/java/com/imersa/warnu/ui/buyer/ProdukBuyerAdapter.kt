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

class ProdukBuyerAdapter(
    private val onItemClick: (ProdukBuyer) -> Unit
) : ListAdapter<ProdukBuyer, ProdukBuyerAdapter.ViewHolder>(ProdukDiffCallback()) {

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
        val onItemClick: (ProdukBuyer) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val imgProduk: ImageView = itemView.findViewById(R.id.imgProdukBuyer)
        private val tvNama: TextView = itemView.findViewById(R.id.tvNamaProdukBuyer)
        private val tvHarga: TextView = itemView.findViewById(R.id.tvHargaProdukBuyer)

        fun bind(produk: ProdukBuyer) {
            tvNama.text = produk.nama
            tvHarga.text = "Rp ${produk.harga}"
            Glide.with(itemView.context)
                .load(produk.gambarUrl)
                .placeholder(R.drawable.placeholder_image)
                .into(imgProduk)

            itemView.setOnClickListener {
                onItemClick(produk)
            }
        }
    }

    class ProdukDiffCallback : DiffUtil.ItemCallback<ProdukBuyer>() {
        override fun areItemsTheSame(oldItem: ProdukBuyer, newItem: ProdukBuyer): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ProdukBuyer, newItem: ProdukBuyer): Boolean {
            return oldItem == newItem
        }
    }
}
