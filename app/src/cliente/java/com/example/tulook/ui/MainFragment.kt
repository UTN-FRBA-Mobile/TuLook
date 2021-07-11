package com.example.tulook.ui

import android.content.ContentValues
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tulook.MainActivity
import com.example.tulook.R
import com.example.tulook.adapters.PeluqueriaListAdapter
import com.example.tulook.databinding.FragmentMainBinding
import com.example.tulook.fileSystem.InternalStorage
import com.example.tulook.fileSystem.MyPreferenceManager
import com.example.tulook.model.Peluqueria
import com.example.tulook.model.Turno
import com.example.tulook.services.APIService
import com.google.firebase.auth.FirebaseUser
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class MainFragment : Fragment(), PeluqueriaListAdapter.onPeluqueriaClickListener {
    private lateinit var pRecyclerViewFav: RecyclerView
    private lateinit var pRecyclerViewRec: RecyclerView
    private lateinit var pAdapterFav: PeluqueriaListAdapter
    private lateinit var pAdapterRec: PeluqueriaListAdapter
    private var peluqueriasConsultadas: List<Peluqueria>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private var _binding: FragmentMainBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    //En el ciclo de vida esto viene después de onCreateView => desde acá podemos acceder a todos los controles del fragment
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btn_cambiar_direccion = binding.layDireccion.btnCambiarDireccion

        btn_cambiar_direccion.setOnClickListener {
            findNavController().navigate(R.id.myLocationFragment)
        }

        val btn_lista_peluquerias = binding.btnListaPeluquerias

        btn_lista_peluquerias.setOnClickListener {
            findNavController().navigate(R.id.peluqueriaListFragment)
        }

        pRecyclerViewFav = binding.rvFavoritos
        pRecyclerViewRec = binding.rvRecientes

        (requireActivity() as MainActivity).auth?.addAuthStateListener {
            if (_binding != null) {
                getProximoTurno()
                updateLayoutVisibilityOnAuthChange(it.currentUser)
            }
        }

    }

    // onresume para cosas que pueden cambiar cuando vuelve a la pantalla
    // (por ejemplo, cambia la dirección guardada en preferences)
    override fun onResume() {
        super.onResume()

        val loc = MyPreferenceManager.getLocation(requireActivity().applicationContext)
        val locationText = binding.layDireccion.textDireccion

        if (loc != null) {
            locationText.text = loc.addr
        } else {
            locationText.text = "No tenés ninguna dirección configurada. Podés guardar una ubicación y ver las peluquerías que tenés cerca!"
//            val locationLayout = binding.layDireccion
//            locationLayout.root.visibility = GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getProximoTurno() {

        val mainActivity = activity as MainActivity
        val user = mainActivity.auth?.currentUser

        val navigationView = mainActivity.findViewById<LinearLayout>(R.id.container_proximo_turno)
        if (user == null) {
            navigationView.visibility = GONE
            return
        }

        navigationView.visibility = VISIBLE

        APIService.create().getTurnosPorUsuario(user.uid).enqueue(object : Callback<List<Turno>> {
            override fun onResponse(call: Call<List<Turno>>, response: Response<List<Turno>>) {
                /*  puede suceder que empiece a cargar este request y el usuario cambie de
                 *  fragment antes de que se termine. si pasa esto, cuando termina el request,
                 *  trata de usar este binding pero es null porque se eliminó el fragment,
                 *  entonces debemos checkear que siga existiendo.
                 *  en realidad está mal asumir siempre que el binding no es null,
                 *  y el atajo que tenemos en todos los fragments es una trampa,
                 *  pero refactorizar todos los fragments sería mucho laburo.                */
                Log.d("auth", "$binding, ${response.isSuccessful}")
                if (response.isSuccessful && binding != null) {
                    //ordeno las fechas ascendente
                    val turnosOrdenadosPorFecha =
                        response.body()!!.sortedBy { getTime(it.fecha).time }
                    //filtro las fechas mayores que hoy
                    val proximosTurnos =
                        turnosOrdenadosPorFecha.filter { Calendar.getInstance().time <= getTime(it.fecha).time }

                    if (proximosTurnos.isNotEmpty()) {
                        val fechaProximoTurno =
                            SimpleDateFormat("dd-MM-yyyy, hh:mm").format(proximosTurnos[0].fecha)

                        binding.textProxTurno.text = fechaProximoTurno.toString()
                    } else {
                        binding.textProxTurno.text = "Sin Prox. Turno"
                    }
                } else {
                    showErrorTurnos()
                }
            }

            override fun onFailure(call: Call<List<Turno>>, t: Throwable) {
                Log.e(ContentValues.TAG, "onFailure: Ha fallado la llamada")
            }
        })
    }

    private fun getTime(fecha: Date): Calendar {
        val calendario = Calendar.getInstance()
        calendario.time = fecha
        return calendario
    }

    private fun getPeluquerias(fileName: String) {
        if (InternalStorage.getFileUri(requireContext(), fileName) != null) {
            val readedText = InternalStorage.readFile(requireContext(), fileName)
            if (readedText != "[]") {
                val gson = Gson()
                val array = gson.fromJson(readedText, Array<String>::class.java)
                val arrayPeluquerias = ArrayList(array.toMutableList())

                if (peluqueriasConsultadas.isNullOrEmpty()) {
                    APIService.create().getPeluquerias()
                        .enqueue(object : Callback<List<Peluqueria>> {
                            override fun onResponse(
                                call: Call<List<Peluqueria>>,
                                response: Response<List<Peluqueria>>
                            ) {
                                if (response.isSuccessful) {
                                    peluqueriasConsultadas = response.body()
                                    filtrarPeluqueriasObtenidas(fileName, arrayPeluquerias)
                                } else {
                                    showErrorPeluquerias()
                                }
                            }

                            override fun onFailure(call: Call<List<Peluqueria>>, t: Throwable) {
                                Log.e(ContentValues.TAG, "onFailure: Ha fallado la llamada")
                            }
                        })
                } else {
                    filtrarPeluqueriasObtenidas(fileName, arrayPeluquerias)
                }
            } else {
                if (fileName == "Favoritos") {
                    binding.textFavoritos0.visibility = VISIBLE
                } else {
                    binding.textRecientes0.visibility = VISIBLE
                }
            }
        } else {
            if (fileName == "Favoritos") {
                binding.textFavoritos0.visibility = VISIBLE
            } else {
                binding.textRecientes0.visibility = VISIBLE
            }
        }
    }

    private fun filtrarPeluqueriasObtenidas(fileName: String, arrayPeluquerias: ArrayList<String>) {
        val peluqueriasFiltradas =
            peluqueriasConsultadas?.filter { peluqueria -> arrayPeluquerias.contains(peluqueria.id.toString()) }

        var typeOfAdapter = ""
        if (fileName == "Favoritos") {
            typeOfAdapter = "favoritoList"
        } else {
            typeOfAdapter = "recienteList"
        } //falta codear el de recienteList

        Log.e("typeOfAdapter", typeOfAdapter)
        if (fileName == "Favoritos") {

            pAdapterFav = PeluqueriaListAdapter(
                peluqueriasFiltradas?.toMutableList(),
                this@MainFragment,
                typeOfAdapter
            )
            val pLayoutManager =
                LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
            pRecyclerViewFav.adapter = pAdapterFav
            pRecyclerViewFav.layoutManager = pLayoutManager
        } else {
            pAdapterRec = PeluqueriaListAdapter(
                peluqueriasFiltradas?.toMutableList(),
                this@MainFragment,
                typeOfAdapter
            )
            val pLayoutManager =
                LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
            pRecyclerViewRec.adapter = pAdapterRec
            pRecyclerViewRec.layoutManager = pLayoutManager
        }
    }

    private fun showErrorPeluquerias() {
        Toast.makeText(
            activity,
            "Ha ocurrido un error obteniendo los datos de las peluquerías",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showErrorTurnos() {
        Toast.makeText(
            activity,
            "Ha ocurrido un error obteniendo los turnos del usuario",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onRowClick(id: Int) {
        Log.e("RowClick", "Id de pelu: ${id}")
        val action =
            MainFragmentDirections.actionMainFragmentToPeluqueriaDetailFragment(peluqueriaId = id)
        findNavController().navigate(action)
    }

    override fun onFavClick(id: Int) {
        Log.e("FavClick", "Id de pelu: ${id}")
    }

    private fun updateLayoutVisibilityOnAuthChange(currentUser: FirebaseUser?) {
        with(requireActivity()) {

            // solo se ven si estoy logeado
            val visiblesLogeado: MutableList<View> = mutableListOf(
                findViewById<LinearLayout>(R.id.container_proximo_turno),
                findViewById<RecyclerView>(R.id.rv_favoritos),
                findViewById<RecyclerView>(R.id.rv_recientes)
            )

            // siempre tienen que estar invisibles, salvo que la función
            // getPeluquerias determine que se tienen que ver
            val maybeVisiblesLogeado: MutableList<View> = mutableListOf(
                findViewById<TextView>(R.id.text_favoritos_0),
                findViewById<TextView>(R.id.text_recientes_0),
            )

            // solo se ven si no estoy logeado
            val visiblesNoLogeado: MutableList<View> = mutableListOf(
                findViewById<TextView>(R.id.text_favoritos_logeate),
                findViewById<TextView>(R.id.text_recientes_logeate)
            )

            if (currentUser != null) {
                visiblesLogeado.forEach { it.visibility = VISIBLE }
                maybeVisiblesLogeado.forEach { it.visibility = GONE }
                visiblesNoLogeado.forEach { it.visibility = GONE }

                getPeluquerias("Favoritos")
                getPeluquerias("Recientes")
            } else {
                visiblesLogeado.forEach { it.visibility = GONE }
                maybeVisiblesLogeado.forEach { it.visibility = GONE }
                visiblesNoLogeado.forEach { it.visibility = VISIBLE }
            }
        }
    }
}