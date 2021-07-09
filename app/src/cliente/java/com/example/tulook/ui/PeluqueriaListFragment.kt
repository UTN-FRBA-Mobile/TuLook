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
import com.example.tulook.adapters.PeluqueriaListAdapter
import com.example.tulook.databinding.FragmentPeluqueriaListBinding
import com.example.tulook.fileSystem.MyPreferenceManager
import com.example.tulook.model.Peluqueria
import com.example.tulook.services.APIService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PeluqueriaListFragment : Fragment(), PeluqueriaListAdapter.onPeluqueriaClickListener {
    private lateinit var pRecyclerView: RecyclerView
    private var pAdapter: PeluqueriaListAdapter? = null

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
//        val btn_lista_peluquerias = binding.btnUbicacionPeluquerias

//        btn_lista_peluquerias.setOnClickListener {
//            findNavController().navigate(R.id.peluqueriaDetailFragment)
//        }

        pRecyclerView = binding.rvPeluquerias
        getPeluquerias()

        val btnSortDistance = binding.btnSortDistance
        btnSortDistance.setOnClickListener {
            val location = MyPreferenceManager.getLocation(requireActivity().applicationContext)
            if (location != null) {
                if(pAdapter?.sortByDistance(location) == "ASC"){
                    binding.textSortDistance.text = "↑"
                }else{
                    binding.textSortDistance.text = "↓"
                }
                binding.textSortRating.text = ""
                binding.textSortName.text = ""
            }else{
                Toast.makeText(activity, "Primero debe establecer su dirección", Toast.LENGTH_SHORT).show()
            }
        }

        val btnSortRating = binding.btnSortRating
        btnSortRating.setOnClickListener {
            if(pAdapter?.sortByRating() == "ASC"){
                binding.textSortRating.text = "↑"
            }else{
                binding.textSortRating.text = "↓"
            }
            binding.textSortDistance.text = ""
            binding.textSortName.text = ""
        }

        val btnSortName = binding.btnSortName
        btnSortName.setOnClickListener {
            if(pAdapter?.sortByName()  == "ASC"){
                binding.textSortName.text = "↑"
            }else{
                binding.textSortName.text = "↓"
            }
            binding.textSortRating.text = ""
            binding.textSortDistance.text = ""
        }
    }

    // onresume para cosas que pueden cambiar cuando vuelve a la pantalla
    // (por ejemplo, cambia la dirección guardada en preferences)
    override fun onResume() {
        super.onResume()

        val loc = MyPreferenceManager.getLocation(requireActivity().applicationContext)

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

    private fun getPeluquerias() {
        APIService.create().getPeluquerias().enqueue(object : Callback<List<Peluqueria>> {
            override fun onResponse(call: Call<List<Peluqueria>>, response: Response<List<Peluqueria>>) {
                if (response.isSuccessful) {
                    Log.e(TAG, response.body().toString())
                    pAdapter = PeluqueriaListAdapter(response.body()?.toMutableList(), this@PeluqueriaListFragment, "peluqueriaList")
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