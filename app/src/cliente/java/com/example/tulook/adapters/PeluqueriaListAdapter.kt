package com.example.tulook.adapters


import android.location.Location
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tulook.R
import com.example.tulook.base.BaseViewHolder
import com.example.tulook.databinding.PeluqueriaFavoritoRowItemBinding
import com.example.tulook.databinding.PeluqueriaRowItemBinding
import com.example.tulook.fileSystem.LocationData
import com.example.tulook.model.Peluqueria


class PeluqueriaListAdapter(
    private val peluqueriasList: MutableList<Peluqueria>?,
    val itemClickListener: onPeluqueriaClickListener,
    val typeOfAdapter: String
) :
    RecyclerView.Adapter<BaseViewHolder<*>>() {

    enum class SortStatus {
        NOTHING, NAME, RATING, DISTANCE
    }

    var sortStatus: SortStatus = SortStatus.NOTHING

    interface onPeluqueriaClickListener {
        fun onRowClick(id: Int)
        fun onFavClick(id: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {

        when (typeOfAdapter) {
            "peluqueriaList" -> return PeluqueriasViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.peluqueria_row_item, parent, false)
            )
            "favoritoList" -> return FavoritosViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.peluqueria_favorito_row_item, parent, false)
            )
            "recienteList" -> return RecientesViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.peluqueria_favorito_row_item, parent, false)
            )
            else -> return PeluqueriasViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.peluqueria_row_item, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        when (holder) {
            is PeluqueriasViewHolder -> holder.bind(peluqueriasList!![position], position, false)
            is FavoritosViewHolder -> holder.bind(peluqueriasList!![position], position, false)
            is RecientesViewHolder -> holder.bind(peluqueriasList!![position], position, false)
        }
    }

    override fun getItemCount(): Int {
        if (peluqueriasList == null) {
            return 0
        }
        return peluqueriasList!!.size
    }

    private fun replacePeluquerias(replacement: List<Peluqueria>) {
        peluqueriasList?.clear()
        peluqueriasList?.addAll(replacement)
        notifyDataSetChanged()
    }

    fun sortByRating(): String {
        if (sortStatus == SortStatus.RATING) {
            val sorted = peluqueriasList?.sortedWith(compareBy { it.rating })
            if (sorted != null) replacePeluquerias(sorted)
            sortStatus = SortStatus.NOTHING
            return "ASC"
        } else {
            val sorted = peluqueriasList?.sortedWith(compareByDescending { it.rating })
            if (sorted != null) replacePeluquerias(sorted)
            sortStatus = SortStatus.RATING
            return "DESC"
        }
    }

    fun sortByDistance(locationData: LocationData): String {
        val user = Location("")
        user.latitude = locationData.lat
        user.longitude = locationData.lng

        fun distanceToUser(peluqueria: Peluqueria): Comparable<*> {
            val location = Location("")
            location.latitude = peluqueria.lat
            location.longitude = peluqueria.lng

            return location.distanceTo(user)
        }

        if (sortStatus == SortStatus.DISTANCE) {
            val sorted = peluqueriasList?.sortedWith(compareBy { distanceToUser(it) })
            if (sorted != null) replacePeluquerias(sorted)
            sortStatus = SortStatus.NOTHING
            return "ASC"
        } else {
            val sorted = peluqueriasList?.sortedWith(compareByDescending { distanceToUser(it) })
            if (sorted != null) replacePeluquerias(sorted)
            sortStatus = SortStatus.DISTANCE
            return "DESC"
        }
    }

    fun sortByName(): String {
        if (sortStatus == SortStatus.NAME) {
            val sorted = peluqueriasList?.sortedWith(compareBy { it.nombre })
            if (sorted != null) replacePeluquerias(sorted)
            sortStatus = SortStatus.NOTHING
            return "ASC"
        } else {
            val sorted = peluqueriasList?.sortedWith(compareByDescending { it.nombre })
            if (sorted != null) replacePeluquerias(sorted)
            sortStatus = SortStatus.NAME
            return "DESC"
        }
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    inner class PeluqueriasViewHolder(itemView: View) : BaseViewHolder<Peluqueria>(itemView) {
        val binding = PeluqueriaRowItemBinding.bind(itemView)

        override fun bind(item: Peluqueria, position: Int, isActivated: Boolean) {
            itemView.setOnClickListener { itemClickListener.onRowClick(item.id) }
            binding.peluqueriaAddressTxt.setOnClickListener { itemClickListener.onFavClick(item.id) }

            binding.peluqueriaNameTxt.text = item.nombre
            binding.peluqueriaAddressTxt.text =
                "${item.direccion!!.calle} ${item.direccion!!.numero}"
            binding.peluqueriaRatingBar.rating = item.rating
            binding.icon.text = item.nombre.get(0).toString()
        }
    }

    inner class FavoritosViewHolder(itemView: View) : BaseViewHolder<Peluqueria>(itemView) {
        val binding = PeluqueriaFavoritoRowItemBinding.bind(itemView)

        override fun bind(item: Peluqueria, position: Int, isActivated: Boolean) {
            itemView.setOnClickListener { itemClickListener.onRowClick(item.id) }
            binding.peluqueriaImageView.text = item.nombre.get(0).toString()
            binding.peluqueriaAddressTxt.setOnClickListener { itemClickListener.onFavClick(item.id) }
            binding.peluqueriaNameTxt.text = item.nombre
            binding.peluqueriaAddressTxt.text =
                "${item.direccion!!.calle} ${item.direccion!!.numero}"
        }
    }

    inner class RecientesViewHolder(itemView: View) : BaseViewHolder<Peluqueria>(itemView) {
        val binding = PeluqueriaFavoritoRowItemBinding.bind(itemView)

        override fun bind(item: Peluqueria, position: Int, isActivated: Boolean) {
            itemView.setOnClickListener { itemClickListener.onRowClick(item.id) }
            binding.peluqueriaImageView.text = item.nombre.get(0).toString()
            binding.peluqueriaAddressTxt.setOnClickListener { itemClickListener.onFavClick(item.id) }
            binding.peluqueriaNameTxt.text = item.nombre
            binding.peluqueriaAddressTxt.text =
                "${item.direccion!!.calle} ${item.direccion!!.numero}"
        }
    }
}