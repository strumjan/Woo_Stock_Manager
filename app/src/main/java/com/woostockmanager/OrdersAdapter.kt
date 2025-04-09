package com.woostockmanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OrdersAdapter(
    private val orders: List<Order>,
    private val onResendClick: (Int) -> Unit
) : RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {

    class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val orderId: TextView = view.findViewById(R.id.textOrderId)
        val orderDate: TextView = view.findViewById(R.id.textOrderDate)
        val orderStatus: TextView = view.findViewById(R.id.textOrderStatus)
        val orderTotal: TextView = view.findViewById(R.id.textOrderTotal)
        val resendButton: Button = view.findViewById(R.id.buttonResend)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.orderId.text = holder.itemView.context.getString(R.string.order_number, order.id)
        holder.orderDate.text = holder.itemView.context.getString(R.string.order_date, order.date)
        holder.orderStatus.text = holder.itemView.context.getString(R.string.order_status, order.status)
        holder.orderTotal.text = holder.itemView.context.getString(
            R.string.order_total,
            order.total,
            order.currency
        )

        holder.resendButton.setOnClickListener {
            onResendClick(order.id)
        }
    }

    override fun getItemCount() = orders.size
}
