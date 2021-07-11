package com.example.tulook.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.recyclerview.selection.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tulook.adapters.TurnoListAdapter
import com.example.tulook.databinding.FragmentMainBinding
import com.example.tulook.model.Turno
import com.example.tulook.services.APIService
import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainFragment : Fragment(), TurnoListAdapter.onTurnoClickListener {
    private lateinit var turnosRecyclerView: RecyclerView
    private lateinit var turnosAdapter: TurnoListAdapter
    private lateinit var tracker: SelectionTracker<String>
    private lateinit var turnos: MutableList<Turno>

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

        binding.textIdPeluqueria.onFocusChangeListener  = View.OnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                hideKeyboard()
            }
        }

        binding.btnGuardarId.setOnClickListener {
            if (binding.textIdPeluqueria.text.isNullOrEmpty()) {
                Toast.makeText(context, "Debe ingresar un ID de peluquería", Toast.LENGTH_SHORT).show()
            } else {
                binding.idPeluqueria.text = binding.textIdPeluqueria.text
                binding.tituloIdPeluqueria.visibility = View.VISIBLE
                binding.idPeluqueria.visibility = View.VISIBLE
                binding.textPendientes.visibility = View.GONE
                binding.textIdPeluqueria.setText("")
                hideKeyboard()
                Toast.makeText(context, "Actualizando lista de turnos...", Toast.LENGTH_SHORT).show()
                pedirTurnos()
            }
        }

        binding.btnConfirmarTurnos.setOnClickListener {
            if (binding.idPeluqueria.text.isNullOrEmpty()) {
                Toast.makeText(context, "Debe ingresar un ID de peluquería", Toast.LENGTH_SHORT).show()
            } else {
                //confirmacion de los turnos seleccionados (mover estado 1 a 2)
                confirmarRechazarTurnos(2)
                pedirTurnos()
            }
        }

        binding.btnRechazarTurnos.setOnClickListener {
            if (binding.idPeluqueria.text.isNullOrEmpty()) {
                Toast.makeText(context, "Debe ingresar un ID de peluquería", Toast.LENGTH_SHORT).show()
            } else {
                //rechazo de los turnos seleccionados (eliminar los turnos de la lista)
                confirmarRechazarTurnos(3)
                pedirTurnos()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun hideKeyboard() {
        (context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).apply{
            hideSoftInputFromWindow(
                binding.textIdPeluqueria.windowToken,
                0
            )
        }
    }

    fun pedirTurnos(){
        Log.e("PEDIRTURNOS", binding.idPeluqueria.text.toString())
        APIService.create().getTurnosPorPeluqueria(binding.idPeluqueria.text.toString().toInt()).enqueue(object : Callback<MutableList<Turno>> {
            override fun onResponse(call: Call<MutableList<Turno>>, response: Response<MutableList<Turno>>) {
                if (response.isSuccessful) {
                    turnos = response.body()!!
                    llenarTurnos()
                } else {
                    Toast.makeText(activity, "Ha ocurrido un error obteniendo los turnos de la peluqueria", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<MutableList<Turno>>, t: Throwable) {
                Log.e("PedirTurnos", "onFailure: Ha fallado la llamada")
            }
        })
    }

    fun llenarTurnos(){
        //llena el recycler con los servicios de la peluqueria
        val turnosID = turnos.map { it.id.toString() } as MutableList<String>
        turnosAdapter = TurnoListAdapter(
            turnosID,
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
            StorageStrategy.createStringStorage()
        ).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build()
        turnosAdapter.tracker = tracker

        tracker.addObserver(object : SelectionTracker.SelectionObserver<String>() {
            override fun onSelectionChanged() {
                super.onSelectionChanged()
                Log.d("DEBUG", "selection=${tracker.selection}")
            }
        })
    }

    override fun onRowClick(){

    }

    private fun confirmarRechazarTurnos(estado: Int){
        if (!tracker.selection.isEmpty()){
            val turnosConfirmados = turnos.filter { tracker.selection.toList().contains(it.id.toString()) }
            val gson = GsonBuilder().create()
            var estadosRechazadosConExito: MutableList<Turno> = turnosConfirmados as MutableList<Turno>
            var body = gson.toJsonTree(Estado(estado)).asJsonObject
            for(turno in turnosConfirmados){
                APIService.create().modificarTurno(turno.id, body).enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            Log.e("Turno Modificado: ", turno.id.toString() + " response: " + response.body())
                        } else {
                            //if(estado == 3){ estadosRechazadosConExito.remove(turno) }
                            Toast.makeText(context, "No se obtuvo respuesta de la llamada", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Log.e("ConfirmarTurno", "onFailure: Ha fallado la llamada")
                        //if(estado == 3){ estadosRechazadosConExito.remove(turno) }
                    }
                })
            }

            //if(estado == 3 && estadosRechazadosConExito.isNotEmpty()){ turnosAdapter.deleteTurnos(estadosRechazadosConExito) }
        }
        else{
            Toast.makeText(activity, "Debe seleccionar al menos un turno", Toast.LENGTH_LONG).show()
        }
    }

    inner class TurnoKeyProvider(private val adapter: TurnoListAdapter) :
        ItemKeyProvider<String>(
            SCOPE_CACHED
        ) {
        override fun getKey(position: Int): String? =
            adapter.turnosList!![position]

        override fun getPosition(key: String): Int =
            adapter.turnosList!!.indexOfFirst { it == key }
    }

    private class MyItemDetailsLookup(private val recyclerView: RecyclerView) :
        ItemDetailsLookup<String>() {
        override fun getItemDetails(event: MotionEvent): ItemDetails<String>? {
            val view = recyclerView.findChildViewUnder(event.x, event.y)
            if (view != null) {
                return (recyclerView.getChildViewHolder(view) as TurnoListAdapter.TurnoViewHolder).getItemDetails()
            }
            return null
        }
    }

    private class Estado(
        @SerializedName("estado")
        @Expose
        var estado: Int
    )
}