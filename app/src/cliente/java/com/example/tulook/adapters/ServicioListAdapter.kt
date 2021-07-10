package com.example.tulook.adapters

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.example.tulook.R
import com.example.tulook.base.BaseViewHolder
import com.example.tulook.databinding.ServiceRowItemBinding


class ServicioListAdapter(
    val servicesList: List<String>?,
    val itemClickListener: onServiceClickListener
) :
    RecyclerView.Adapter<BaseViewHolder<*>>() {

    var tracker: SelectionTracker<String>? = null

    interface onServiceClickListener {
        fun onRowClick()
    }

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return ServiceViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.service_row_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        when (holder) {
            is ServiceViewHolder -> {
                val service = servicesList!![position]
                holder.bind(getItem(position), position, tracker!!.isSelected(service))
//                holder.bind(servicesList!![position], position)
            }
        }
    }

    override fun getItemCount(): Int {
        return servicesList!!.size
    }

    override fun getItemId(position: Int): Long = position.toLong()

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    inner class ServiceViewHolder(itemView: View) : BaseViewHolder<String>(itemView) {
        val binding = ServiceRowItemBinding.bind(itemView)

        override fun bind(item: String, position: Int, isActivated: Boolean) {
            // itemView.setOnClickListener { itemClickListener.onRowClick()}//item.id) }
            binding.serviceName.text = item
            binding.peluqueriaItem.setBackgroundResource( if(isActivated) R.color.light_red else R.color.white)

            when (item) {
                "corte" -> binding.serviceImage.setImageResource(R.drawable.ic_baseline_content_cut)
                "color" -> binding.serviceImage.setImageResource(R.drawable.ic_hair_dye_kit)
                "peinado" -> binding.serviceImage.setImageResource(R.drawable.ic_hair_straightener)
                "barba" -> binding.serviceImage.setImageResource(R.drawable.ic_beard)
                "depilación" -> binding.serviceImage.setImageResource(R.drawable.ic_wax)
                "manicura" -> binding.serviceImage.setImageResource(R.drawable.ic_manicure)
                "pedicura" -> binding.serviceImage.setImageResource(R.drawable.ic_pedicure)
            }
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<String> =
            object : ItemDetailsLookup.ItemDetails<String>() {
                override fun getPosition(): Int = absoluteAdapterPosition
                override fun getSelectionKey(): String? = getItem(absoluteAdapterPosition)
                override fun inSelectionHotspot(e: MotionEvent): Boolean { return true }
            }
    }

    fun getItem(index: Int): String {
        return servicesList!![index]
    }
}
/*
class MyItemDetailsLookup(private val recyclerView: RecyclerView) :
    ItemDetailsLookup<String>() {
    override fun getItemDetails(event: MotionEvent): ItemDetails<String>? {
        val view = recyclerView.findChildViewUnder(event.x, event.y)
        if (view != null) {
            return (recyclerView.getChildViewHolder(view) as ServicioListAdapter.ServiceViewHolder).getItemDetails()
        }
        return null
    }
}
*/