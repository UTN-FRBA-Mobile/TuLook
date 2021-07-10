package com.example.tulook.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.selection.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tulook.R
import com.example.tulook.adapters.TurnoListAdapter
import com.example.tulook.databinding.FragmentMainBinding
import com.example.tulook.model.Turno
import com.example.tulook.services.APIService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainFragment : Fragment(), TurnoListAdapter.onTurnoClickListener {
    private lateinit var turnosRecyclerView: RecyclerView
    private lateinit var turnosAdapter: TurnoListAdapter
    private lateinit var tracker: SelectionTracker<Turno>
    private lateinit var turnos: List<Turno>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private var _binding: FragmentMainBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    //En el ciclo de vida esto viene después de onCreateView => desde acá podemos acceder a todos los controles del fragment
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        turnosRecyclerView = binding.rvTurnosPendientes

        binding.btnGuardarId.setOnClickListener {
            if (binding.idPeluqueria.text.isBlank()) {
                Toast.makeText(context, "Debe ingresar un ID de peluquería", Toast.LENGTH_SHORT).show()
            } else {
                binding.idPeluqueria.text = binding.idPeluqueria.text
                pedirTurnos()
                llenarTurnos()
            }
        }

        binding.btnConfirmarTurnos.setOnClickListener {
            if (binding.idPeluqueria.text.isBlank()) {
                Toast.makeText(context, "Debe ingresar un ID de peluquería", Toast.LENGTH_SHORT).show()
            } else {
                //confirmacion de los turnos seleccionados (mover estado 1 a 2)
            }
        }

        binding.btnRechazarTurnos.setOnClickListener {
            if (binding.idPeluqueria.text.isBlank()) {
                Toast.makeText(context, "Debe ingresar un ID de peluquería", Toast.LENGTH_SHORT).show()
            } else {
                //rechazo de los turnos seleccionados (eliminar los turnos de la lista)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun pedirTurnos(){
        APIService.create().getTurnosPorPeluqueria(binding.idPeluqueria.text.toString().toInt()).enqueue(object : Callback<List<Turno>> {
            override fun onResponse(call: Call<List<Turno>>, response: Response<List<Turno>>) {
                if (response.isSuccessful) {
                    turnos = response.body()!!
                } else {
                    Toast.makeText(activity, "Ha ocurrido un error obteniendo los turnos de la peluqueria", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<List<Turno>>, t: Throwable) {
                Log.e(ContentValues.TAG, "onFailure: Ha fallado la llamada")
            }
        })
    }

    fun llenarTurnos(){
        //llena el recycler con los servicios de la peluqueria
        turnosAdapter = TurnoListAdapter(
            turnos,
            this@MainFragment
        )

        val pLayoutManager = LinearLayoutManager(activity)
        turnosRecyclerView.adapter = turnosAdapter
        turnosRecyclerView.layoutManager = pLayoutManager

        tracker = SelectionTracker.Builder<String>(
            "turnoSelection",
            turnosRecyclerView,
            TurnoKeyProvider(turnosAdapter),
            MyItemDetailsLookup(turnosRecyclerView),
            StorageStrategy.createParcelableStorage(Turno).createStringStorage()
        ).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build()
        turnosAdapter.tracker = tracker

        tracker.addObserver(object : SelectionTracker.SelectionObserver<Turno>() {
            override fun onSelectionChanged() {
                super.onSelectionChanged()
                Log.d("DEBUG", "selection=${tracker.selection}")
            }
        })
    }

    override fun onRowClick(){

    }

    inner class TurnoKeyProvider(private val adapter: TurnoListAdapter) :
        ItemKeyProvider<Turno>(
            SCOPE_CACHED
        ) {
        override fun getKey(position: Int): Turno? =
            adapter.turnos!![position]

        override fun getPosition(key: Turno): Int =
            adapter.turnos!!.indexOfFirst { it == key }
    }

    private class MyItemDetailsLookup(private val recyclerView: RecyclerView) :
        ItemDetailsLookup<Turno>() {
        override fun getItemDetails(event: MotionEvent): ItemDetails<Turno>? {
            val view = recyclerView.findChildViewUnder(event.x, event.y)
            if (view != null) {
                return (recyclerView.getChildViewHolder(view) as TurnoListAdapter.TurnoViewHolder).getItemDetails()
            }
            return null
        }
    }
}