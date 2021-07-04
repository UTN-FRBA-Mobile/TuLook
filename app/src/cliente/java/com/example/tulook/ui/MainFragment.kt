package com.example.tulook.ui

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tulook.R
import com.example.tulook.adapters.PeluqueriaListAdapter
import com.example.tulook.databinding.FragmentMainBinding
import com.example.tulook.fileSystem.InternalStorage
import com.example.tulook.fileSystem.LocationStorage
import com.example.tulook.model.Peluqueria
import com.example.tulook.model.Turno
import com.example.tulook.services.APIService
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

class MainFragment : Fragment(), PeluqueriaListAdapter.onPeluqueriaClickListener {
    private lateinit var pRecyclerView: RecyclerView
    private lateinit var pAdapter: PeluqueriaListAdapter

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

        val btn_lista_peluquerias = binding.btnListaPeluquerias

        btn_lista_peluquerias.setOnClickListener {
            findNavController().navigate(R.id.peluqueriaListFragment)
        }
        getProximoTurno()
        pRecyclerView = binding.rvFavoritos
        getPeluqueriasFavoritas()
    }

    // onresume para cosas que pueden cambiar cuando vuelve a la pantalla
    // (por ejemplo, cambia la dirección guardada en preferences)
    override fun onResume() {
        super.onResume()

        val loc = LocationStorage.getLocation(requireActivity().applicationContext)

        Log.d("LOCATION", "$loc")

        if (loc != null) {
            val locationText = binding.layDireccion.textDireccion
            locationText.text = loc.addr
        } else {
            val locationLayout = binding.layDireccion
            locationLayout.root.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getProximoTurno(){
        val idUsuario = 2 //AHORA ESTA HARCODEADO, CAMBIAR !!!!!!!!!!!!!!!!!!!!!!!
        APIService.create().getTurnosPorUsuario(idUsuario).enqueue(object : Callback<List<Turno>> {
            override fun onResponse(call: Call<List<Turno>>, response: Response<List<Turno>>) {
                if (response.isSuccessful) {
                    //ordeno las fechas ascendente
                    val turnosOrdenadosPorFecha =
                        response.body()!!.sortedBy { getTime(it.fecha).time }
                    //filtro las fechas mayores que hoy
                    val proximosTurnos =
                        turnosOrdenadosPorFecha.filter { Calendar.getInstance().time <= getTime(it.fecha).time }

                    if (proximosTurnos.isNotEmpty()) {
                        val fechaProximoTurno =
                            SimpleDateFormat("dd-MM-yyyy, hh:mm").format(proximosTurnos[0].fecha)

                        binding.textProxTurno.text = fechaProximoTurno.toString()
                    } else {
                        binding.textProxTurno.text = "Sin Prox. Turno"
                    }
                } else {
                    showErrorTurnos()
                }
            }

            override fun onFailure(call: Call<List<Turno>>, t: Throwable) {
                Log.e(ContentValues.TAG, "onFailure: Ha fallado la llamada")
            }
        })
    }

    private fun getTime(fecha: Date): Calendar{
        val calendario = Calendar.getInstance()
        calendario.time = fecha
        return calendario
    }

    private fun getPeluqueriasFavoritas() {
        val fileNameFavoritos = "Favoritos"

        if(InternalStorage.getFileUri(requireContext(), fileNameFavoritos) != null){
            val readedText = InternalStorage.readFile(requireContext(), fileNameFavoritos)
            if(readedText != "[]"){
                val gson = Gson()
                val array = gson.fromJson(readedText, Array<String>::class.java)
                val arrayPeluquerias = ArrayList(array.toMutableList())

                APIService.create().getPeluquerias().enqueue(object : Callback<List<Peluqueria>> {
                    override fun onResponse(call: Call<List<Peluqueria>>, response: Response<List<Peluqueria>>) {
                        if (response.isSuccessful) {

                            val peluqueriasFiltradas = response.body()?.filter { peluqueria -> arrayPeluquerias.contains(peluqueria.id.toString()) }

                            Log.e(ContentValues.TAG, peluqueriasFiltradas.toString())

                            pAdapter = PeluqueriaListAdapter(peluqueriasFiltradas?.toMutableList(), this@MainFragment, "favoritoList")
                            val pLayoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
                            pRecyclerView.adapter = pAdapter
                            pRecyclerView.layoutManager = pLayoutManager
                        } else {
                            showErrorPeluquerias()
                        }
                    }
                    override fun onFailure(call: Call<List<Peluqueria>>, t: Throwable) {
                        Log.e(ContentValues.TAG, "onFailure: Ha fallado la llamada")
                    }
                })
            }
        }
    }

    private fun showErrorPeluquerias() {
        Toast.makeText(activity, "Ha ocurrido un error obteniendo los datos de las peluquerías", Toast.LENGTH_LONG).show()
    }

    private fun showErrorTurnos() {
        Toast.makeText(activity, "Ha ocurrido un error obteniendo los turnos del usuario", Toast.LENGTH_LONG).show()
    }

    override fun onRowClick(id: Int) {
        Log.e("RowClick", "Id de pelu: ${id}")
        val action = PeluqueriaListFragmentDirections.actionPeluqueriaListFragmentToPeluqueriaDetailFragment(peluqueriaId = id)
        findNavController().navigate(action)
    }

    override fun onFavClick(id: Int) {
        Log.e("FavClick", "Id de pelu: ${id}")
    }

}