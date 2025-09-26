package com.imersa.warnu.ui.buyer.cart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.imersa.warnu.R
import com.imersa.warnu.data.model.CartItem
import java.text.NumberFormat
import java.util.Locale
import java.text.DecimalFormat

class CartAdapter(
    private val onIncrease: (CartItem) -> Unit,
    private val onDecrease: (CartItem) -> Unit,
    private val onRemove: (CartItem) -> Unit // Fungsi baru untuk hapus item
) : ListAdapter<CartItem, CartAdapter.CartViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view, onIncrease, onDecrease, onRemove)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CartViewHolder(
        itemView: View,
        private val onIncrease: (CartItem) -> Unit,
        private val onDecrease: (CartItem) -> Unit,
        private val onRemove: (CartItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val ivProductImage: ImageView = itemView.findViewById(R.id.iv_product_image)
        private val tvProductName: TextView = itemView.findViewById(R.id.tv_product_name)
        private val tvProductPrice: TextView = itemView.findViewById(R.id.tv_product_price)
        private val tvQuantity: TextView = itemView.findViewById(R.id.tv_quantity)
        private val btnIncrease: ImageButton = itemView.findViewById(R.id.btn_increase_quantity)
        private val btnDecrease: ImageButton = itemView.findViewById(R.id.btn_decrease_quantity)
        private val btnRemove: ImageButton = itemView.findViewById(R.id.btn_remove_item)

        fun bind(cartItem: CartItem) {
            tvProductName.text = cartItem.name
            tvQuantity.text = cartItem.quantity.toString()
            val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID")) as DecimalFormat
            formatter.maximumFractionDigits = 0
            formatter.minimumFractionDigits = 0

            tvProductPrice.text = formatter.format(cartItem.price ?: 0.0)
            Glide.with(itemView.context)
                .load(cartItem.imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .into(ivProductImage)

            btnIncrease.setOnClickListener { onIncrease(cartItem) }
            btnDecrease.setOnClickListener { onDecrease(cartItem) }
            btnRemove.setOnClickListener { onRemove(cartItem) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem.productId == newItem.productId
        }

        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem == newItem
        }
    }
}