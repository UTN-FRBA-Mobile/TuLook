package com.example.tulook.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tulook.R
import com.example.tulook.base.BaseViewHolder
import com.example.tulook.databinding.PeluqueriaRowItemBinding
import com.example.tulook.model.Peluqueria


class PeluqueriaListAdapter(
    private val peluqueriasList: List<Peluqueria>?,
    val itemClickListener: onPeluqueriaClickListener
) :
    RecyclerView.Adapter<BaseViewHolder<*>>() {

    interface onPeluqueriaClickListener {
        fun onRowClick(id: Int)
        fun onFavClick(id: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return PelqueriasViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.peluqueria_row_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        when (holder) {
            is PelqueriasViewHolder -> holder.bind(peluqueriasList!![position], position)
        }
    }

    override fun getItemCount(): Int {
        return peluqueriasList!!.size
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    inner class PelqueriasViewHolder(itemView: View) : BaseViewHolder<Peluqueria>(itemView) {
        val binding = PeluqueriaRowItemBinding.bind(itemView)

        override fun bind(item: Peluqueria, position: Int) {
            itemView.setOnClickListener { itemClickListener.onRowClick(item.id) }
            binding.peluqueriaAddressTxt.setOnClickListener { itemClickListener.onFavClick(item.id) }

            binding.peluqueriaNameTxt.text = item.nombre
            binding.peluqueriaAddressTxt.text = "${item.direccion.calle} ${item.direccion.numero}"
            binding.peluqueriaRatingBar.rating = item.rating
        }
    }
}