package com.example.tulook.ui.turno

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.navigation.fragment.findNavController
import com.example.tulook.R
import com.example.tulook.databinding.FragmentNuevoTurnoHorariosBinding
import java.util.*

class NuevoTurno_HorariosFragment : Fragment(), DatePickerDialog.OnDateSetListener {

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
}