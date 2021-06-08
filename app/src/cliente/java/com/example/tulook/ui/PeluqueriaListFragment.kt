package com.example.tulook.ui

import android.content.ContentValues.TAG
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
import com.example.tulook.databinding.FragmentPeluqueriaListBinding
import com.example.tulook.model.Peluqueria
import com.example.tulook.services.APIService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.log

class PeluqueriaListFragment : Fragment(), PeluqueriaListAdapter.onPeluqueriaClickListener {

    private val listadoPeluquerias = mutableListOf<Peluqueria>()

    private lateinit var pRecyclerView: RecyclerView
    private lateinit var pAdapter: PeluqueriaListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private var _binding: FragmentPeluqueriaListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPeluqueriaListBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //se utiliza el boton ubicacion peluqueria para llevarnos al fragment de detalle, como aun no esta diseñado, es solo una prueba.
        val btn_lista_peluquerias = binding.btnUbicacionPeluquerias

        btn_lista_peluquerias.setOnClickListener {
            findNavController().navigate(R.id.peluqueriaDetailFragment)
        }

        pRecyclerView = binding.rvPeluquerias
        getPeluquerias()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getPeluquerias() {
        APIService.create().getPeluquerias().enqueue(object : Callback<List<Peluqueria>> {
            override fun onResponse(call: Call<List<Peluqueria>>, response: Response<List<Peluqueria>>) {
                if (response.isSuccessful) {
                    Log.e(TAG, response.body().toString())
                    pAdapter = PeluqueriaListAdapter(response.body(), this@PeluqueriaListFragment)
                    val pLayoutManager = LinearLayoutManager(activity)
                    pRecyclerView.adapter = pAdapter
                    pRecyclerView.layoutManager = pLayoutManager
                } else {
                    showError()
                }
            }
            override fun onFailure(call: Call<List<Peluqueria>>, t: Throwable) {
                Log.e(TAG, "onFailure: Ha fallado la llamada")
            }
        })
    }

    private fun showError() {
        Toast.makeText(activity, "Ha ocurrido un error", Toast.LENGTH_LONG).show()
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