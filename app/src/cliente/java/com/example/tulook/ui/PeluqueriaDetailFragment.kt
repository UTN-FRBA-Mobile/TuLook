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
import androidx.navigation.fragment.navArgs
import com.example.tulook.R
import com.example.tulook.databinding.FragmentPeluqueriaDetailBinding
import com.example.tulook.model.Peluqueria
import com.example.tulook.services.APIService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


//////////////////////
import com.example.tulook.fileSystem.InternalStorage
import com.google.gson.Gson

class PeluqueriaDetailFragment : Fragment() {

    val args: PeluqueriaDetailFragmentArgs by navArgs()
    private lateinit var peluqueria: Peluqueria


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
//        binding.peluIdTxt.text = "Peluquería ID: ${peluqueriaId}"

        getPeluqueria(peluqueriaId)

        val btn_nuevoTurno = binding.btnNuevoTurno

        btn_nuevoTurno.setOnClickListener {
            findNavController().navigate(R.id.nuevoTurnoServiciosFragment)
        }
        ///////////////////////////////////////////////////
        val btn_agregar_favoritos = binding.btnAgregarFavoritos
        //val file = File(requireContext().filesDir, "FAVORITOOS")

        btn_agregar_favoritos.setOnClickListener {
            val fileNameFavoritos = "Favoritos"
            //Log.e("FAVORITOS", R.string.file_favoritos.toString())
            InternalStorage.saveFile(requireContext(), "[\"99\"]", fileNameFavoritos)

            val readedText = InternalStorage.readFile(requireContext(), fileNameFavoritos)
            val gson = Gson()
            val array = gson.fromJson(readedText, Array<String>::class.java)
            val arrayPeluquerias = ArrayList(array.toMutableList())
            arrayPeluquerias.add(peluqueria.id.toString())
            val json: String = gson.toJson(arrayPeluquerias).replace("\\n", "\n")
            Log.e("FAVORITOS","Nueva lista: " + json)

            InternalStorage.saveFile(requireContext(), json, fileNameFavoritos)

            Toast.makeText(context, "Añadido a favoritos", Toast.LENGTH_LONG).show()
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
        binding.peluIdTxt.text = peluqueria.id.toString()
        binding.peluNameTxt.text = peluqueria.nombre
        binding.peluDireccionTxt.text = "${peluqueria.direccion?.calle} ${peluqueria.direccion?.numero}"
        binding.peluHorarioInicioTxt.text = peluqueria.horarioApertura.toString()
        binding.peluHorarioFinTxt.text = peluqueria.horarioCierre.toString()
        binding.peluRatingTxt.text = peluqueria.rating.toString()
        binding.peluServiciosTxt.text = peluqueria.servicios.toString()


    }
}
