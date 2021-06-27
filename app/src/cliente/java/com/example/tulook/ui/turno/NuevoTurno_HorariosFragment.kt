package com.example.tulook.ui.turno

import android.app.DatePickerDialog
import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.tulook.R
import com.example.tulook.databinding.FragmentNuevoTurnoHorariosBinding
import com.example.tulook.model.Peluqueria
import com.example.tulook.model.Turno
import com.example.tulook.services.APIService
import com.example.tulook.ui.PeluqueriaDetailFragmentArgs
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class NuevoTurno_HorariosFragment : Fragment(), DatePickerDialog.OnDateSetListener {

    val args: NuevoTurno_HorariosFragmentArgs by navArgs()
    var servicios = emptyArray<String>()
    var peluqueriaId = 0
    var duracionTurno = 0

    private lateinit var turnos: List<Turno>

    var day = 0
    var month = 0
    var year = 0

    var savedDay = 0
    var savedMonth = 0
    var savedYear = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private var _binding: FragmentNuevoTurnoHorariosBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentNuevoTurnoHorariosBinding.inflate(inflater, container, false)
        val view = binding.root

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btn_nuevoTurno = binding.btnVerTurnoDetalle
        val btn_datepicker = binding.btnDatepicker

        var datepicker = DatePickerDialog(requireActivity(), this, year, month, day)
        datepicker.datePicker.minDate = Date().time

        btn_nuevoTurno.setOnClickListener {
            findNavController().navigate(R.id.nuevoTurno_DetailFragment)
        }

        btn_datepicker.setOnClickListener{
            getDateCalendar()
            datepicker.show()
        }

        servicios = args.serviciosSeleccionados
        peluqueriaId = args.peluqueriaId
        duracionTurno = 30 * servicios.size
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        savedDay = dayOfMonth
        savedMonth = month
        savedYear = year

        var tv_date = binding.tvDate
        tv_date.text = "$savedDay-$savedMonth-$savedYear"

        getDateCalendar()
    }

    private fun getDateCalendar(){
        val calendar: Calendar = Calendar.getInstance()
        day = calendar.get(Calendar.DAY_OF_MONTH)
        month = calendar.get(Calendar.MONTH)
        year = calendar.get(Calendar.YEAR)
    }




    private fun getTurnos(peluqueriaId: Int, day: Int, month: Int, year: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)
        val fecha: Date = calendar.time

        APIService.create().getTurnosPorPeluqueriaPorDia(peluqueriaId, fecha).enqueue(object : Callback<List<Turno>> {

            override fun onResponse(call: Call<List<Turno>>, response: Response<List<Turno>>) {
                if (response.isSuccessful) {
                    Log.e(ContentValues.TAG, response.body().toString())
                    turnos = response.body() ?: emptyList()

                    Log.e("Turnos", turnos.toString())
                } else {
                    showError()
                }
            }

            override fun onFailure(call: Call<List<Turno>>, t: Throwable) {
                Log.e(ContentValues.TAG, "onFailure: Ha fallado la llamada")
            }
        })
    }

    private fun showError() {
        Toast.makeText(activity, "Ha ocurrido un error", Toast.LENGTH_LONG).show()
    }
}