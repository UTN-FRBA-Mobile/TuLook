package com.example.tulook.ui

import android.content.ContentValues
import android.content.res.ColorStateList
import com.example.tulook.R
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.selection.*
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
import kotlin.collections.ArrayList


class PeluqueriaDetailFragment : Fragment() , ServicioListAdapter.onServiceClickListener {


    private val args: PeluqueriaDetailFragmentArgs by navArgs()
    private lateinit var peluqueria: Peluqueria
    private lateinit var bannerRecyclerView: RecyclerView
    private lateinit var bannerAdapter: BannerListAdapter
    private lateinit var sRecyclerView: RecyclerView
    private lateinit var sAdapter: ServicioListAdapter
    private lateinit var tracker: SelectionTracker<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private var _binding: FragmentPeluqueriaDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

            if (!tracker.selection.isEmpty()){
                val action =
                    PeluqueriaDetailFragmentDirections.actionPeluqueriaDetailFragmentToNuevoTurnoHorariosFragment(
                        peluqueria.id,
                        tracker.selection.toList().toTypedArray(),
                        peluqueria.nombre,
                        peluqueria.direccion!!.calle,
                        peluqueria.direccion!!.numero
                    )
                findNavController().navigate(action)
            }
            else{
                Toast.makeText(activity, "Debe seleccionar al menos un servicio", Toast.LENGTH_LONG).show()
            }
        }

        val btn_agregar_favoritos = binding.btnAgregarFavoritos

        btn_agregar_favoritos.setOnClickListener {
            checkOrEditFavoritos(peluqueriaId.toString(), true)
        }

        val btn_ver_comentarios = binding.btnVerComentarios
        btn_ver_comentarios.setOnClickListener {
            val action = PeluqueriaDetailFragmentDirections.actionPeluqueriaDetailFragmentToComentarios(peluqueriaId)
            findNavController().navigate(action)
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
                    sAdapter = ServicioListAdapter(
                        peluqueria.servicios.toList(),
                        this@PeluqueriaDetailFragment
                    )

                    val pLayoutManager = LinearLayoutManager(activity)
                    sRecyclerView.adapter = sAdapter
                    sRecyclerView.layoutManager = pLayoutManager

                    tracker = SelectionTracker.Builder<String>(
                        "serviceSelection",
                        sRecyclerView,
                        ServicioKeyProvider(sAdapter),
                        MyItemDetailsLookup(sRecyclerView),
                        StorageStrategy.createStringStorage()
                    ).withSelectionPredicate(
                        SelectionPredicates.createSelectAnything()
                    ).build()
                    sAdapter.tracker = tracker

                    tracker.addObserver(object : SelectionTracker.SelectionObserver<String>() {
                        override fun onSelectionChanged() {
                            super.onSelectionChanged()
                            Log.d("DEBUG", "selection=${tracker.selection}")
                        }
                    })

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

    private fun renderPeluqueria(peluqueria: Peluqueria) {
        val calApertura = Calendar.getInstance()
        calApertura.time = peluqueria.horarioApertura
        val hourApertura = calApertura.get(Calendar.HOUR_OF_DAY)

        val calCierre = Calendar.getInstance()
        calCierre.time = peluqueria.horarioCierre
        val hourCierre = calCierre.get(Calendar.HOUR_OF_DAY)

        binding.peluName.text = peluqueria.nombre
        binding.peluDireccion.text =
            "${peluqueria.direccion?.calle} ${peluqueria.direccion?.numero}"
        binding.peluHorario.text = "${hourApertura?.toString()}hs a ${hourCierre?.toString()}hs"

    }

    override fun onRowClick(){}

    private fun checkOrEditFavoritos(id: String, editEnabled: Boolean) {
        val btn_agregar_favoritos = binding.btnAgregarFavoritos
        val fileNameFavoritos = "Favoritos"

        var readedText = "[]"
        if (InternalStorage.getFileUri(requireContext(), fileNameFavoritos) != null) {
            readedText = InternalStorage.readFile(requireContext(), fileNameFavoritos)
        }

        Log.e("Favoritos", "Lista favoritos anterior: " + readedText)

        val gson = Gson()
        val array = gson.fromJson(readedText, Array<String>::class.java)
        val arrayPeluquerias = ArrayList(array.toMutableList())
        var stringToastFavoritos = ""
        if (arrayPeluquerias.contains(id)) {
            if (editEnabled) {
                arrayPeluquerias.remove(id)
                stringToastFavoritos = "Eliminado de favoritos"
                ImageViewCompat.setImageTintList(btn_agregar_favoritos, ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.grey_ligth)));

            }else{
                stringToastFavoritos = "Añadido a favoritos"
                ImageViewCompat.setImageTintList(btn_agregar_favoritos, ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext() , R.color.red)));
            }
        } else {
            if (editEnabled) {
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

        if (editEnabled) {
            val json: String = gson.toJson(arrayPeluquerias).replace("\\n", "\n")

            InternalStorage.saveFile(requireContext(), json, fileNameFavoritos)

            Log.e("Favoritos", "Lista favoritos nueva: " + json)
            Toast.makeText(context, stringToastFavoritos, Toast.LENGTH_LONG).show()
        }
    }


    inner class ServicioKeyProvider(private val adapter: ServicioListAdapter) :
        ItemKeyProvider<String>(
            SCOPE_CACHED
        ) {
        override fun getKey(position: Int): String? =
            adapter.servicesList!![position]

        override fun getPosition(key: String): Int =
            adapter.servicesList!!.indexOfFirst { it == key }
    }

    private class MyItemDetailsLookup(private val recyclerView: RecyclerView) :
        ItemDetailsLookup<String>() {
        override fun getItemDetails(event: MotionEvent): ItemDetails<String>? {
            val view = recyclerView.findChildViewUnder(event.x, event.y)
            if (view != null) {
                return (recyclerView.getChildViewHolder(view) as ServicioListAdapter.ServiceViewHolder).getItemDetails()
            }
            return null
        }
    }
}
