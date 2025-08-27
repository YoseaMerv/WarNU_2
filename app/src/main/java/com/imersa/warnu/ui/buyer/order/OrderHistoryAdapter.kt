package com.imersa.warnu.ui.buyer.order

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.imersa.warnu.R
import com.imersa.warnu.data.model.Order
import com.imersa.warnu.databinding.ItemOrderHistoryBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class OrderHistoryAdapter : ListAdapter<Order, OrderHistoryAdapter.OrderViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class OrderViewHolder(private val binding: ItemOrderHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(order: Order) {
            binding.apply {
                tvOrderId.text = order.orderId

                // Format tanggal
                order.createdAt?.toDate()?.let { date ->
                    val format = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("in", "ID"))
                    tvOrderDate.text = format.format(date)
                }

                // Format harga
                val localeID = Locale("in", "ID")
                val numberFormat = NumberFormat.getCurrencyInstance(localeID)
                tvTotalPrice.text = numberFormat.format(order.totalAmount ?: 0.0)

                // Atur status & warna
                val status = order.paymentStatus?.uppercase(Locale.ROOT)
                tvStatus.text = status
                when (status) {
                    "SETTLEMENT" -> {
                        tvStatus.background = ContextCompat.getDrawable(itemView.context, R.drawable.circle_green) // Buat drawable hijau
                    }
                    "PENDING" -> {
                        tvStatus.background = ContextCompat.getDrawable(itemView.context, R.drawable.status_pending_background)
                    }
                    else -> { // FAILED, EXPIRE, CANCEL
                        tvStatus.background = ContextCompat.getDrawable(itemView.context, R.drawable.status_cancelled_background) // Buat drawable merah
                    }
                }
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Order>() {
            override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean = oldItem.orderId == newItem.orderId
            override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean = oldItem == newItem
        }
    }
}