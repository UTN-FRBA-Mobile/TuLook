package com.example.tulook.ui

import android.content.ContentValues
import android.content.res.ColorStateList
import com.example.tulook.R
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tulook.adapters.BannerListAdapter
import com.example.tulook.adapters.ServicioListAdapter
import com.example.tulook.databinding.FragmentPeluqueriaDetailBinding
import com.example.tulook.fileSystem.InternalStorage
import com.example.tulook.model.Peluqueria
import com.example.tulook.services.APIService
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class PeluqueriaDetailFragment : Fragment(), ServicioListAdapter.onServiceClickListener {


    val args: PeluqueriaDetailFragmentArgs by navArgs()
    private lateinit var peluqueria: Peluqueria
    private lateinit var bannerRecyclerView: RecyclerView
    private lateinit var bannerAdapter: BannerListAdapter
    private lateinit var sRecyclerView: RecyclerView
    private lateinit var sAdapter: ServicioListAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private var _binding: FragmentPeluqueriaDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPeluqueriaDetailBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val peluqueriaId = args.peluqueriaId
        Log.e("PeluDetail", "Pelu ID: ${peluqueriaId}")

        bannerRecyclerView = binding.rvFotosSalon
        sRecyclerView = binding.rvServicios

        getPeluqueria(peluqueriaId)
        checkOrEditFavoritos(peluqueriaId.toString(), false)

        val btn_nuevoTurno = binding.btnNuevoTurno

        btn_nuevoTurno.setOnClickListener {
            findNavController().navigate(R.id.nuevoTurnoServiciosFragment)
        }

        val btn_agregar_favoritos = binding.btnAgregarFavoritos

        btn_agregar_favoritos.setOnClickListener {
            checkOrEditFavoritos(peluqueriaId.toString(), true)
        }

        val btn_ver_comentarios = binding.btnVerComentarios
        btn_ver_comentarios.setOnClickListener {
            findNavController().navigate(R.id.comentariosDetailFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun getPeluqueria(id: Int) {
        APIService.create().getPeluqueria(id).enqueue(object : Callback<Peluqueria> {
            override fun onResponse(call: Call<Peluqueria>, response: Response<Peluqueria>) {
                if (response.isSuccessful) {
                    Log.e(ContentValues.TAG, response.body().toString())
                    peluqueria = response.body() ?: Peluqueria()

                    Log.e("Peluqueria", peluqueria.toString())

                    //completa el banner con las fotos de la peluqueria
                    bannerAdapter = BannerListAdapter(peluqueria.imagenes)
                    val bannerLayoutManager = LinearLayoutManager(activity , LinearLayoutManager.HORIZONTAL, false)
                    bannerRecyclerView.adapter = bannerAdapter
                    bannerRecyclerView.layoutManager = bannerLayoutManager


                    //llena el recycler con los servicios de la peluqueria
                    sAdapter = ServicioListAdapter(peluqueria.servicios, this@PeluqueriaDetailFragment)
                    val pLayoutManager = LinearLayoutManager(activity)
                    sRecyclerView.adapter = sAdapter
                    sRecyclerView.layoutManager = pLayoutManager


                    renderPeluqueria(peluqueria)
                } else {
                    showError()
                }
            }

            override fun onFailure(call: Call<Peluqueria>, t: Throwable) {
                Log.e(ContentValues.TAG, "onFailure: Ha fallado la llamada")
            }
        })
    }

    private fun showError() {
        Toast.makeText(activity, "Ha ocurrido un error", Toast.LENGTH_LONG).show()
    }

    private fun renderPeluqueria(peluqueria: Peluqueria){
        val calApertura = Calendar.getInstance()
        calApertura.time = peluqueria.horarioApertura
        val hourApertura = calApertura.get(Calendar.HOUR_OF_DAY)

        val calCierre = Calendar.getInstance()
        calCierre.time = peluqueria.horarioCierre
        val hourCierre = calCierre.get(Calendar.HOUR_OF_DAY)

        binding.peluName.text = peluqueria.nombre
        binding.peluDireccion.text = "${peluqueria.direccion?.calle} ${peluqueria.direccion?.numero}"
        binding.peluHorario.text = "${hourApertura?.toString()}hs a ${hourCierre?.toString()}hs"

    }

    override fun onRowClick(){//id: Int) {
         Log.e("RowClick", "Id de servicio: ${id}")
         val action = PeluqueriaDetailFragmentDirections.actionPeluqueriaDetailFragmentToServicioDetailFragment() //(peluqueriaId = id)
         findNavController().navigate(action)
    }

    private fun checkOrEditFavoritos(id: String, editEnabled: Boolean){
        val btn_agregar_favoritos = binding.btnAgregarFavoritos
        val fileNameFavoritos = "Favoritos"

        var readedText = "[]"
        if(InternalStorage.getFileUri(requireContext(), fileNameFavoritos) != null){
            readedText = InternalStorage.readFile(requireContext(), fileNameFavoritos)
        }

        Log.e("Favoritos","Lista favoritos anterior: " + readedText)

        val gson = Gson()
        val array = gson.fromJson(readedText, Array<String>::class.java)
        val arrayPeluquerias = ArrayList(array.toMutableList())
        var stringToastFavoritos = ""
        if(arrayPeluquerias.contains(id)){
            if(editEnabled){
                arrayPeluquerias.remove(id)
                stringToastFavoritos = "Eliminado de favoritos"
                ImageViewCompat.setImageTintList(btn_agregar_favoritos, ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.grey_ligth)));

            }else{
                stringToastFavoritos = "Añadido a favoritos"
                ImageViewCompat.setImageTintList(btn_agregar_favoritos, ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext() , R.color.red)));
            }
        }else{
            if(editEnabled){
                arrayPeluquerias.add(id)
                stringToastFavoritos = "Añadido a favoritos"
                ImageViewCompat.setImageTintList(btn_agregar_favoritos, ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext() , R.color.red)));

            }else{
                stringToastFavoritos = "Eliminado de favoritos"
                ImageViewCompat.setImageTintList(btn_agregar_favoritos, ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.grey_ligth)));
            }
        }

        if(editEnabled) {
            val json: String = gson.toJson(arrayPeluquerias).replace("\\n", "\n")

            InternalStorage.saveFile(requireContext(), json, fileNameFavoritos)

            Log.e("Favoritos", "Lista favoritos nueva: " + json)
            Toast.makeText(context, stringToastFavoritos, Toast.LENGTH_LONG).show()
        }
    }
}
