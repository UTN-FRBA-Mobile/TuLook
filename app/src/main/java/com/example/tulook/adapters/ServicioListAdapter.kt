package com.example.tulook.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tulook.R
import com.example.tulook.base.BaseViewHolder
import com.example.tulook.databinding.ServiceRowItemBinding


class ServicioListAdapter(
    private val servicesList: List<String>?,
    val itemClickListener: onServiceClickListener
) :
    RecyclerView.Adapter<BaseViewHolder<*>>() {

    interface onServiceClickListener {
        fun onRowClick(id: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return ServiceViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.service_row_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        when (holder) {
            is ServiceViewHolder -> holder.bind(servicesList!![position], position)
        }
    }

    override fun getItemCount(): Int {
        return servicesList!!.size
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    inner class ServiceViewHolder(itemView: View) : BaseViewHolder<String>(itemView) {
        val binding = ServiceRowItemBinding.bind(itemView)

        override fun bind(item: String, position: Int) {
           // itemView.setOnClickListener { itemClickListener.onRowClick(item.id) }
            binding.serviceName.text = item

            when (item) {
                "corte" ->  binding.serviceImage.setImageResource(R.drawable.ic_baseline_content_cut)
                "color" ->  binding.serviceImage.setImageResource(R.drawable.ic_hair_dye_kit)
                "peinado" ->  binding.serviceImage.setImageResource(R.drawable.ic_hair_straightener)
                "barba" ->  binding.serviceImage.setImageResource(R.drawable.ic_beard)
                "depilación" ->  binding.serviceImage.setImageResource(R.drawable.ic_wax)
                "manicura" ->  binding.serviceImage.setImageResource(R.drawable.ic_manicure)
                "pedicura" ->  binding.serviceImage.setImageResource(R.drawable.ic_pedicure)
            }
        }
    }
}
