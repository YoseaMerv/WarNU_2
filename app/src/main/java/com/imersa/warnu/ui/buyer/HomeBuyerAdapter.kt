package com.imersa.warnu.ui.buyer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.imersa.warnu.R
import com.imersa.warnu.databinding.ItemProductBuyerBinding
import java.text.NumberFormat
import java.util.Locale

class HomeBuyerAdapter(
    private val onItemClick: (ProductBuyer) -> Unit,
    private val onAddToCartClick: (ProductBuyer) -> Unit
) : RecyclerView.Adapter<HomeBuyerAdapter.ViewHolder>() {

    private val items = mutableListOf<ProductBuyer>()

    fun submitList(list: List<ProductBuyer>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }


    inner class ViewHolder(private val binding: ItemProductBuyerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: ProductBuyer) {
            binding.tvNamaProdukBuyer.text = product.name

            // Format harga Rupiah
            val formattedPrice = NumberFormat.getCurrencyInstance(Locale("in", "ID")).format(product.price ?: 0.0)
            binding.tvHargaProdukBuyer.text = formattedPrice

            // Load image pakai Glide
            Glide.with(binding.imgProdukBuyer.context)
                .load(product.imageUrl)
                .placeholder(R.drawable.placeholder_image) // gambar default
                .error(R.drawable.placeholder_image)       // kalau gagal load
                .into(binding.imgProdukBuyer)

            // Klik item → buka detail
            binding.root.setOnClickListener {
                onItemClick(product)
            }

            // Klik tombol tambah → add to cart
            binding.btnAddToCart.setOnClickListener {
                onAddToCartClick(product)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProductBuyerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }
}
