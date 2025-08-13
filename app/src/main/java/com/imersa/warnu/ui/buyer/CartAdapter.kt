package com.imersa.warnu.ui.buyer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.imersa.warnu.databinding.ItemCartBinding

class CartAdapter(
    private var cartList: List<CartItem>,
    private val onRemoveClick: (CartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(private val binding: ItemCartBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CartItem) {
            binding.tvNameCart.text = item.name
            binding.tvPriceCart.text = "Rp ${item.price}"
            binding.tvQuantityCart.text = "x${item.quantity}"
            Glide.with(binding.ivProductCart.context)
                .load(item.imageUrl)
                .into(binding.ivProductCart)

            binding.btnRemoveCart.setOnClickListener {
                onRemoveClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(cartList[position])
    }

    override fun getItemCount(): Int = cartList.size

    fun updateCartList(newList: List<CartItem>) {
        cartList = newList
        notifyDataSetChanged()
    }
}
