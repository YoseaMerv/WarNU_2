package com.imersa.warnu.ui.buyer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.imersa.warnu.databinding.ItemCartBinding

class CartAdapter(
    private var cartList: List<CartItem>,
    private val onRemoveClick: (CartItem) -> Unit,
    private val onIncreaseQty: (CartItem) -> Unit,
    private val onDecreaseQty: (CartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(val binding: ItemCartBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CartItem) {
            binding.tvCartName.text = item.name
            binding.tvCartPrice.text = "Rp ${item.price?.toInt() ?: 0}"
            binding.tvCartQty.text = item.quantity.toString()

            Glide.with(binding.root.context)
                .load(item.imageUrl)
                .into(binding.ivCartImage)

            binding.btnIncrease.setOnClickListener { onIncreaseQty(item) }
            binding.btnDecrease.setOnClickListener { onDecreaseQty(item) }
            binding.btnRemove.setOnClickListener { onRemoveClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(cartList[position])
    }

    override fun getItemCount() = cartList.size

    fun updateCartList(newList: List<CartItem>) {
        cartList = newList
        notifyDataSetChanged()
    }
}
