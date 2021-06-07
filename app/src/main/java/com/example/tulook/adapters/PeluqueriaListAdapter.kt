package com.example.tulook.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tulook.R
import com.example.tulook.base.BaseViewHolder
import com.example.tulook.databinding.PeluqueriaRowItemBinding
import com.example.tulook.model.Peluqueria


class PeluqueriaListAdapter(val peluqueriasList: List<Peluqueria>?) :
    RecyclerView.Adapter<BaseViewHolder<*>>() {

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

            // Your holder should contain and initialize a member variable
            // for any view that will be set as you render a row
            binding.peluqueriaNameTxt.text = item.nombre
            binding.peluqueriaAddressTxt.text = item.direccion
        }
    }
}