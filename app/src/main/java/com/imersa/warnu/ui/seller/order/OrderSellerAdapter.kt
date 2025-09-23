package com.imersa.warnu.ui.seller.order

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.imersa.warnu.R
import com.imersa.warnu.data.model.Order
import com.imersa.warnu.databinding.ItemOrdersBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class OrderSellerAdapter :
    ListAdapter<Order, OrderSellerAdapter.OrderViewHolder>(OrderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrdersBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = getItem(position)
        holder.bind(order)
    }

    inner class OrderViewHolder(private val binding: ItemOrdersBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        @Suppress("DEPRECATION")
        fun bind(order: Order) {
            val localeID = Locale("in", "ID")
            val numberFormat = NumberFormat.getCurrencyInstance(localeID)
            val formattedPrice = numberFormat.format(order.totalAmount ?: 0.0)

            val sdf = SimpleDateFormat("dd MMMM yyyy, HH:mm", localeID)

            binding.apply {
                tvOrderId.text = "Order #${order.orderId ?: "-"}"
                tvCustomerName.text = order.customerName ?: "Nama Pelanggan Tidak Tersedia"
                tvOrderTotal.text = formattedPrice
                tvOrderDate.text =
                    order.createdAt?.toDate()?.let { sdf.format(it) } ?: "Tanggal tidak tersedia"

                val status = order.paymentStatus?.uppercase(Locale.ROOT) ?: "UNKNOWN"
                tvOrderStatus.text = status

                when (status) {
                    "SETTLEMENT" -> {
                        tvOrderStatus.background = ContextCompat.getDrawable(
                            itemView.context,
                            R.drawable.status_settlement_background
                        )
                    }

                    "PENDING" -> {
                        tvOrderStatus.background = ContextCompat.getDrawable(
                            itemView.context,
                            R.drawable.status_pending_background
                        )
                    }

                    else -> {
                        tvOrderStatus.background = ContextCompat.getDrawable(
                            itemView.context,
                            R.drawable.status_cancelled_background
                        )
                    }
                }
            }
        }
    }
}

class OrderDiffCallback : DiffUtil.ItemCallback<Order>() {
    override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
        return oldItem.orderId == newItem.orderId
    }

    override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
        return oldItem == newItem
    }
}