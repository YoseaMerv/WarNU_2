package com.imersa.warnu.ui.buyer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.imersa.warnu.databinding.ItemProductBuyerBinding
import java.text.NumberFormat
import java.util.Locale

class HomeBuyerAdapter(
    private val onAddToCartClick: (ProdukBuyer) -> Unit
) : RecyclerView.Adapter<HomeBuyerAdapter.ViewHolder>() {

    private val items = mutableListOf<ProdukBuyer>()
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

    fun submitList(newList: List<ProdukBuyer>?) {
        items.clear()
        if (newList != null) {
            items.addAll(newList)
        }
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemProductBuyerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ProdukBuyer) {
            binding.tvNamaProdukBuyer.text = item.name ?: "Produk Tanpa Nama"
            binding.tvHargaProdukBuyer.text = currencyFormat.format(item.price ?: 0.0)

            Glide.with(binding.imgProdukBuyer.context)
                .load(item.imageUrl)
                .into(binding.imgProdukBuyer)

            binding.btnAddToCart.setOnClickListener {
                onAddToCartClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProductBuyerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
