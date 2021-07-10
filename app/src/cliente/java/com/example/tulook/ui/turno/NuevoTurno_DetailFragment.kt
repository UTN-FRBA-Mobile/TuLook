package com.example.tulook.ui.turno

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import com.example.tulook.R
import com.example.tulook.databinding.FragmentNuevoTurnoDetailBinding
import com.example.tulook.fileSystem.InternalStorage
import com.google.gson.Gson

class NuevoTurno_DetailFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private var _binding: FragmentNuevoTurnoDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNuevoTurnoDetailBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btn_confirmarTurno = binding.btnConfirmarTurno
        //TODO: Implementar confirmacion Turno
        btn_confirmarTurno.setOnClickListener {
            val peluqueriaID = 1 //TODO: obtener peluqueriaID cuando se arme el nuevo turno
            agregarPeluqueriaReciente(peluqueriaID.toString())
        }

//        btn_verTurnoHorarios.setOnClickListener {
//            findNavController().navigate(R.id.nuevoTurno_HorariosFragment)
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun agregarPeluqueriaReciente(peluqueriaID: String) {
        val fileNameRecientes = "Recientes"
        val tamanioMaxRecientes = 5

        var readedText = "[]"
        if (InternalStorage.getFileUri(requireContext(), fileNameRecientes) != null) {
            readedText = InternalStorage.readFile(requireContext(), fileNameRecientes)
        }

        Log.e("Recientes", "Lista recientes anterior: " + readedText)

        val gson = Gson()
        val array = gson.fromJson(readedText, Array<String>::class.java)
        val arrayPeluquerias = ArrayList(array.toMutableList())

        if (!arrayPeluquerias.contains(peluqueriaID)) {
            if(arrayPeluquerias.size >= tamanioMaxRecientes){
                arrayPeluquerias.removeAt(arrayPeluquerias.size-1)
            }
            arrayPeluquerias.add(peluqueriaID)

            val json: String = gson.toJson(arrayPeluquerias).replace("\\n", "\n")
            InternalStorage.saveFile(requireContext(), json, fileNameRecientes)
            Log.e("Recientes", "Lista recientes nueva: " + json)
        }
    }
}