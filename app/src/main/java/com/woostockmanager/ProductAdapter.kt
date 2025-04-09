package com.woostockmanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView

interface ProductUpdateListener {
    fun onUpdateProduct(productId: Int, newQuantity: Int, newStockStatus: String)
}


class ProductAdapter(
    private var productList: List<Product>,
    private val listener: ProductUpdateListener,
    private val onUpdateClick: (Product, Int) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productName: TextView = itemView.findViewById(R.id.productName)
        val productCategory: TextView = itemView.findViewById(R.id.productCategory)
        val stockQuantity: EditText = itemView.findViewById(R.id.stockQuantity)
        val stockStatusSpinner: Spinner = itemView.findViewById(R.id.stockStatusSpinner) // Додадено!
        val updateButton: Button = itemView.findViewById(R.id.updateButton)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        holder.productName.text = product.name
        holder.productCategory.text = product.category
        holder.stockQuantity.setText(product.stockQuantity.toString())

        // Highlight if it's the current search result
        if (position == highlightedPosition) {
            holder.productName.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.orange))
            holder.productName.paint.isUnderlineText = true
        } else {
            holder.productName.setTextColor("#FFFFFF".toColorInt())
            holder.productName.paint.isUnderlineText = false
        }

        // Опции за stock статус
        val stockStatusOptions = arrayOf("instock", "outofstock", "onbackorder")
        val adapter = ArrayAdapter(holder.itemView.context, android.R.layout.simple_spinner_item, stockStatusOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        holder.stockStatusSpinner.adapter = adapter

        // Поставување на тековната вредност во Spinner
        val selectedIndex = stockStatusOptions.indexOf(product.stockStatus)
        holder.stockStatusSpinner.setSelection(if (selectedIndex != -1) selectedIndex else 0)

        // Listener за промена на stock status
        holder.stockStatusSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                product.stockStatus = stockStatusOptions[position] // Овде сега работи!
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        holder.updateButton.setOnClickListener {
            val newQuantity = holder.stockQuantity.text.toString().toIntOrNull() ?: product.stockQuantity
            val newStockStatus = product.stockStatus

            listener.onUpdateProduct(product.id, newQuantity, newStockStatus) // Сега работи!
        }

    }

    private var highlightedPosition: Int? = null

    fun setHighlightedPosition(position: Int?) {
        highlightedPosition = position
        notifyDataSetChanged()
    }


    override fun getItemCount() = productList.size

    fun updateList(newList: List<Product>) {
        productList = newList
        notifyDataSetChanged()
    }
}
