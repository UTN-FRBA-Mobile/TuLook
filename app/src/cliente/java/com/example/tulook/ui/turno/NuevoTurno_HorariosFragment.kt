package com.example.tulook.ui.turno

import android.app.DatePickerDialog
import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.tulook.MainActivity
import com.example.tulook.R
import com.example.tulook.databinding.FragmentNuevoTurnoHorariosBinding
import com.example.tulook.fileSystem.InternalStorage
import com.example.tulook.model.Turno
import com.example.tulook.services.APIService
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class NuevoTurno_HorariosFragment : Fragment(), DatePickerDialog.OnDateSetListener,
    AdapterView.OnItemSelectedListener {

    val args: NuevoTurno_HorariosFragmentArgs by navArgs()
    var servicios = emptyArray<String>()
    var peluqueriaId = 0
    var peluqueriaName = ""
    var duracionTurno = 0
    var peluqueriaDireccion = ""

    private lateinit var turnosLibres: List<Date>
    private lateinit var txtDate: TextView
    private lateinit var horarioElegido: Date
    private lateinit var turno: Turno

    var day = 0
    var month = 0
    var year = 0

    var savedDay = 0
    var savedMonth = 0
    var savedYear = 0

    private var arrayAdapter: ArrayAdapter<Turno>? = null

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

        val btn_solicitarTurno = binding.btnSolicitarTurno
        val btn_datepicker = binding.btnDatepicker
        txtDate = binding.tvDate

        servicios = args.serviciosSeleccionados
        peluqueriaId = args.peluqueriaId
        peluqueriaName = args.peluqueriaName
        peluqueriaDireccion = args.peluqueriaCalle + " " + args.peluqueriaNumero.toString()
        duracionTurno = 30 * servicios.size

        var datepicker = DatePickerDialog(requireActivity(), R.style.Theme_TuLook_Dialog, this, year, month, day)
        datepicker.datePicker.minDate = Date().time

        btn_solicitarTurno.setOnClickListener {
            val mainActivity = requireActivity() as MainActivity
            val user = mainActivity.auth?.currentUser

            if (user == null) {
                mainActivity.startSignin()
                Toast.makeText(activity, "Debe estar logeado para poder pedir un turno.", Toast.LENGTH_LONG).show()
            } else {
                turno = Turno(0, peluqueriaId, user.uid, 1, horarioElegido, duracionTurno)
                guardarTurno(turno)
                agregarPeluqueriaReciente(peluqueriaId.toString())
            }
        }

        btn_datepicker.setOnClickListener {
            getDateCalendar()
            datepicker.show()
        }



        val txtPeluqueriaName = binding.txtPeluqueria
        txtPeluqueriaName.text = peluqueriaName
        binding.direccion.text = peluqueriaDireccion

        val txtServicios = binding.txtServicios
        val serviciosIterator = servicios.iterator()
        while (serviciosIterator.hasNext()){
            txtServicios.append(serviciosIterator.next().toUpperCase())
            if (serviciosIterator.hasNext()){
                txtServicios.append(", ")
            }
        }
        
        initializeDate()
        txtDate.text = "$day-${month}-$year"
        getTurnosLibres(peluqueriaId, day, month, year)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        savedDay = dayOfMonth
        savedMonth = month + 1
        savedYear = year

        txtDate.text = "$savedDay-$savedMonth-$savedYear"

        getDateCalendar()
        getTurnosLibres(peluqueriaId, savedDay, savedMonth, savedYear)
    }

    private fun initializeDate() {
        val today = Date()
        val cal = Calendar.getInstance()
        cal.time = today
        year = cal[Calendar.YEAR]
        month = cal[Calendar.MONTH]+1
        day = cal[Calendar.DAY_OF_MONTH]
    }

    private fun getDateCalendar() {
        val calendar: Calendar = Calendar.getInstance()
        day = calendar.get(Calendar.DAY_OF_MONTH)
        month = calendar.get(Calendar.MONTH) + 1
        year = calendar.get(Calendar.YEAR)
    }


    private fun loadTurnosSpinner() {
        //        Spinner
        val spinner: Spinner = binding.spinnerHorarios
        val adapter = ArrayAdapter(
            requireActivity(),
            android.R.layout.simple_spinner_item,
            turnosToString(turnosLibres)
        )
        spinner.adapter = adapter
        spinner.onItemSelectedListener = this
    }

    private fun getTurnosLibres(peluqueriaId: Int, day: Int, month: Int, year: Int) {
//        val calendar = Calendar.getInstance()
//        calendar.set(year, month, day)
        val f: NumberFormat = DecimalFormat("00")
        val fecha = "$year-${f.format(month)}-${f.format(day)}"

        APIService.create().getTurnosLibres(peluqueriaId, fecha)
            .enqueue(object : Callback<List<Date>> {

                override fun onResponse(call: Call<List<Date>>, response: Response<List<Date>>) {
                    if (response.isSuccessful) {
                        Log.e(ContentValues.TAG, response.body().toString())
                        turnosLibres = response.body() ?: emptyList()
                        loadTurnosSpinner()

                        Log.e("Turnos libres", turnosLibres.toString())
                    } else {
                        showError()
                    }
                }

                override fun onFailure(call: Call<List<Date>>, t: Throwable) {
                    Log.e(ContentValues.TAG, "onFailure: Ha fallado la llamada")
                }
            })
    }

    private fun showError() {
        Toast.makeText(activity, "Ha ocurrido un error", Toast.LENGTH_LONG).show()
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        var items: String = parent?.getItemAtPosition(position) as String
        horarioElegido = turnosLibres.get(position)
        Log.e("Turno elegido", horarioElegido.toString())
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        Toast.makeText(activity, "Debe seleccionar un horario", Toast.LENGTH_LONG).show()
    }

    private fun turnosToString(turnos: List<Date>): List<String> {
        var ts = mutableListOf<String>()
        val formatter: SimpleDateFormat = SimpleDateFormat("HH:mm a")
        for (turno in turnos) {
            ts.add(formatter.format(turno))
        }
        return ts;
    }

    private fun guardarTurno(turno: Turno) {
        APIService.create().crearTurno(turno)
            .enqueue(object : Callback<Turno> {

                override fun onResponse(call: Call<Turno>, response: Response<Turno>) {
                    if (response.isSuccessful) {
                        Log.e(ContentValues.TAG, response.body().toString())
                        val nuevoTurno = response.body() ?: Turno()

                        if (nuevoTurno.id <= 0){
                            Log.e("Turnos/POST", "Ocurrió un error al guardar el turno")
                            showError()
                        }
                        else{
                            turno.id = nuevoTurno.id
                            Toast.makeText(activity, "Turno solicitado exitosamente", Toast.LENGTH_LONG).show()
                            findNavController().navigate(R.id.mainFragment)

                        }
                    } else {
                        showError()
                    }
                }

                override fun onFailure(call: Call<Turno>, t: Throwable) {
                    Log.e(ContentValues.TAG, "onFailure: Ha fallado la llamada")
                }
            })
    }

    private fun agregarPeluqueriaReciente(peluqueriaID: String) {
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
                arrayPeluquerias.removeAt(0)
            }
            arrayPeluquerias.add(peluqueriaID)

            val json: String = gson.toJson(arrayPeluquerias).replace("\\n", "\n")
            InternalStorage.saveFile(requireContext(), json, fileNameRecientes)
            Log.e("Recientes", "Lista recientes nueva: " + json)
        }
    }
}