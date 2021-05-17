package com.example.tulook.ui.turno

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.tulook.R
import com.example.tulook.databinding.FragmentNuevoTurnoHorariosBinding
import com.example.tulook.databinding.FragmentNuevoTurnoServiciosBinding

class NuevoTurnoServiciosFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private var _binding: FragmentNuevoTurnoServiciosBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentNuevoTurnoServiciosBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btn_nuevoTurno = binding.btnVerTurnoHorarios

        btn_nuevoTurno.setOnClickListener {
            findNavController().navigate(R.id.nuevoTurno_HorariosFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}