package com.imersa.warnu.ui.buyer.cart

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.imersa.warnu.data.model.CartItem
import com.imersa.warnu.databinding.ItemCartBinding
import java.text.NumberFormat
import java.util.Locale

class CartAdapter(
    private val onUpdateQuantity: (CartItem, Int) -> Unit,
    private val onRemoveItem: (CartItem) -> Unit
) : ListAdapter<CartItem, CartAdapter.CartViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class CartViewHolder(private val binding: ItemCartBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(cartItem: CartItem) {
            binding.apply {
                tvProductName.text = cartItem.name
                tvQuantity.text = cartItem.quantity.toString()

                val localeID = Locale("in", "ID")
                val numberFormat = NumberFormat.getCurrencyInstance(localeID)
                tvProductPrice.text = numberFormat.format(cartItem.price ?: 0.0)

                Glide.with(itemView.context).load(cartItem.imageUrl).into(ivProductImage)

                btnIncreaseQuantity.setOnClickListener {
                    onUpdateQuantity(cartItem, cartItem.quantity + 1)
                }

                btnDecreaseQuantity.setOnClickListener {
                    if (cartItem.quantity > 1) {
                        onUpdateQuantity(cartItem, cartItem.quantity - 1)
                    } else {
                        onRemoveItem(cartItem)
                    }
                }

                btnRemoveItem.setOnClickListener {
                    onRemoveItem(cartItem)
                }
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CartItem>() {
            override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
                return oldItem.productId == newItem.productId
            }

            override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}