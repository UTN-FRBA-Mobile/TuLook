package com.example.tulook.ui.turno

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.tulook.R
import com.example.tulook.databinding.FragmentNuevoTurnoServiciosBinding
import com.example.tulook.ui.PeluqueriaDetailFragmentArgs
import com.example.tulook.ui.PeluqueriaDetailFragmentDirections
import java.util.*

class NuevoTurnoServiciosFragment : Fragment() {
    //    val args: NuevoTurnoServiciosFragmentArgs by navArgs()
    var serviciosElegidos = emptyArray<String>()

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

//        val servicios = args.servicios
//        val peluqueriaId = args.peluqueriaId
        val btn_nuevoTurno = binding.btnVerTurnoHorarios

        //TODO: eliminar - Sentencia de prueba
//        serviciosElegidos = servicios
//
//        binding.tvServicios.text = "PeluId: $peluqueriaId - Servicios: ${Arrays.toString(servicios)}"
//
//        btn_nuevoTurno.setOnClickListener {
//            //TODO-Guti: Descomentar esto => implementar servicios elegidos
////            if (serviciosElegidos.count() > 0){
////                val action = NuevoTurnoServiciosFragmentDirections.actionNuevoTurnoServiciosFragmentToNuevoTurnoHorariosFragment(args.peluqueriaId, args.servicios)
////                findNavController().navigate(action)
////            }
////            else{
////                Toast.makeText(activity, "Debe seleccionar al menos un servicio para continuar", Toast.LENGTH_LONG).show()
////            }
//
//            val action = NuevoTurnoServiciosFragmentDirections.actionNuevoTurnoServiciosFragmentToNuevoTurnoHorariosFragment(peluqueriaId, servicios)
//            findNavController().navigate(action)

//    }
    }

//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//
//    private fun getSelectedServices() : Array<String>{
//        return serviciosElegidos
//    }
}