package com.codility.gpslocation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.codility.gpslocation.databinding.ItemCardCustomerBinding


class CustomerAdapter(
    private val customers: List<CustomerEntity>,
    private val onEditClick: (CustomerEntity) -> Unit
) : RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder>() {

    inner class CustomerViewHolder(val binding: ItemCardCustomerBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerViewHolder {
        val binding =
            ItemCardCustomerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomerViewHolder, position: Int) {
        val customer = customers[position]
        with(holder.binding) {
            tvName.text = customer.customerName
            tvCategory.text = customer.category
            tvAddress.text = customer.address
            tvLatLng.text = "${customer.latitude}, ${customer.longitude}"
            btnEdit.setOnClickListener { onEditClick(customer) }
        }
    }

    override fun getItemCount() = customers.size
}