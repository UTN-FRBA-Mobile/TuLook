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
import com.example.tulook.model.Peluqueria
import com.example.tulook.services.APIService
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

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

        pRecyclerView = binding.rvFavoritos
        getPeluqueriasFavoritas()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
                            pAdapter = PeluqueriaListAdapter(peluqueriasFiltradas, this@MainFragment, "favoritoList")
                            val pLayoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
                            pRecyclerView.adapter = pAdapter
                            pRecyclerView.layoutManager = pLayoutManager
                        } else {
                            showError()
                        }
                    }
                    override fun onFailure(call: Call<List<Peluqueria>>, t: Throwable) {
                        Log.e(ContentValues.TAG, "onFailure: Ha fallado la llamada")
                    }
                })
            }
        }
    }

    private fun showError() {
        Toast.makeText(activity, "Ha ocurrido un error obteniendo los datos de las peluquerías", Toast.LENGTH_LONG).show()
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