package com.example.tulook.adapters

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.example.tulook.R
import com.example.tulook.base.BaseViewHolder
import com.example.tulook.databinding.TurnoRowItemBinding
import com.example.tulook.model.Turno
import java.text.SimpleDateFormat

class TurnoListAdapter(
    var turnosList: MutableList<String>?,
    var turnos: MutableList<Turno>?,
    val itemClickListener: onTurnoClickListener
) :
    RecyclerView.Adapter<BaseViewHolder<*>>() {
    var tracker: SelectionTracker<String>? = null

    interface onTurnoClickListener {
        fun onRowClick()
    }

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return TurnoViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.turno_row_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        when (holder) {
            is TurnoViewHolder -> {
                val turno = turnosList!![position]
                holder.bind(getItem(position), position, tracker!!.isSelected(turno))
            }
        }
    }

    override fun getItemCount(): Int {
        return turnosList!!.size
    }

    override fun getItemId(position: Int): Long = position.toLong()

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    inner class TurnoViewHolder(itemView: View) : BaseViewHolder<String>(itemView) {
        val binding = TurnoRowItemBinding.bind(itemView)

        override fun bind(item: String, position: Int, isActivated: Boolean) {
            val turno = turnos!!.find { it.id.toString() == item }
            binding.idUsuario.text = turno!!.usuarioId
            binding.fecha.text = SimpleDateFormat("dd-MM-yyyy, HH:mm").format(turno!!.fecha)

            val serviciosIterator = turno!!.servicios.iterator()
            var textoServicios: String = ""
            while (serviciosIterator.hasNext()){
                textoServicios += serviciosIterator.next().toUpperCase()
                if (serviciosIterator.hasNext()){
                    textoServicios += ", "
                }
            }
            binding.servicios.text = textoServicios
            binding.turnoItem.setBackgroundResource( if(isActivated) R.color.light_blue_600 else R.color.white)
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<String> =
            object : ItemDetailsLookup.ItemDetails<String>() {
                override fun getPosition(): Int = absoluteAdapterPosition
                override fun getSelectionKey(): String? = getItem(absoluteAdapterPosition)
                override fun inSelectionHotspot(e: MotionEvent): Boolean { return true }
            }
    }

    fun getItem(index: Int): String {
        return turnosList!![index]
    }

    fun deleteTurnos(turnosDeleted: List<Turno>){
        turnos?.removeAll(turnosDeleted)
        turnosList = turnos?.map { it.id.toString() } as MutableList<String>?
    }
}