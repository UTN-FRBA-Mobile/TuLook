package com.example.tulook.ui

import android.content.ContentValues
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tulook.adapters.ReviewListAdapter
import com.example.tulook.databinding.FragmentComentariosDetailBinding
import com.example.tulook.model.Peluqueria
import com.example.tulook.model.Review
import com.example.tulook.services.APIService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ComentariosDetailFragment : Fragment() , ReviewListAdapter.onReviewClickListener{

    private val args: ComentariosDetailFragmentArgs by navArgs()
    private lateinit var peluqueria: Peluqueria
    private lateinit var reviews: List<Review>
    private lateinit var rRecyclerView: RecyclerView
    private lateinit var rAdapter: ReviewListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private var _binding: FragmentComentariosDetailBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentComentariosDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    //En el ciclo de vida esto viene después de onCreateView => desde acá podemos acceder a todos los controles del fragment
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rRecyclerView = binding.rvReview
        getPeluqueria(args.peluqueriaId)
        getComentarios(args.peluqueriaId)

    }

    override fun onRowClick(){}

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

                    //Lleno la informacion principal de la peluqueria.
                    binding.peluName.text = peluqueria.nombre
                    binding.calificacion.text = peluqueria.rating.toString()
                    binding.estrellas.setRating(peluqueria.rating)
                    binding.icon.text = peluqueria.nombre.get(0).toString()

                } else {
                    showError()
                }
            }

            override fun onFailure(call: Call<Peluqueria>, t: Throwable) {
                Log.e(ContentValues.TAG, "onFailure: Ha fallado la llamada")
            }
        })
    }

    private fun getComentarios(id: Int) {
        APIService.create().getReviewsPorPeluqueria(id).enqueue(object : Callback<List<Review>> {
            override fun onResponse(call: Call<List<Review>>, response: Response<List<Review>>) {
                if (response.isSuccessful) {
                    Log.e(ContentValues.TAG, response.body().toString())

                    //Lleno la cantidad de comentarios
                    binding.cantidadComentarios.text = "(" + response.body()!!.size.toString() + ")"

                    //Lleno con los comentarios de la peluqueria.
                    rAdapter = ReviewListAdapter(response.body()?.toMutableList(), this@ComentariosDetailFragment)
                    val reviewLayoutManager = LinearLayoutManager(activity)
                    rRecyclerView.adapter = rAdapter
                    rRecyclerView.layoutManager = reviewLayoutManager

                } else {
                    showError()
                }
            }

            override fun onFailure(call: Call<List<Review>>, t: Throwable) {
                Log.e(ContentValues.TAG, "onFailure: Ha fallado la llamada")
            }
        })
    }
    private fun showError() {
        Toast.makeText(activity, "Ha ocurrido un error al obtener los comentarios", Toast.LENGTH_LONG).show()
    }

}