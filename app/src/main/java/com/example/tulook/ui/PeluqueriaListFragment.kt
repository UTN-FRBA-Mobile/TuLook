package com.example.tulook.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.tulook.R
import com.example.tulook.databinding.FragmentMainBinding
import com.example.tulook.databinding.FragmentPeluqueriaListBinding

class PeluqueriaListFragment : Fragment() {

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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}