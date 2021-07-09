package com.example.tulook.ui

import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tulook.adapters.ReviewListAdapter
import com.example.tulook.databinding.FragmentComentariosDetailBinding
import com.example.tulook.model.Peluqueria
import com.example.tulook.model.Review
import com.example.tulook.services.APIService
import com.google.gson.GsonBuilder
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

        val btn_publicar_review = binding.btnPublicarReview

        btn_publicar_review.setOnClickListener {
            if(!binding.nuevoComentario.text.isNullOrEmpty() && binding.nuevaPuntuacion.rating.toFloat() != 0f) {
                publicarComentario()
                binding.nuevoComentario.setText("")
                binding.nuevaPuntuacion.rating = 0f

            }else{
                Toast.makeText(activity, "Debe escribir un comentario y puntuar con al menos media estrella.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.nuevoComentario.onFocusChangeListener  = View.OnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                hideKeyboard()

            }
        }

        rRecyclerView = binding.rvReview
        getPeluqueria(args.peluqueriaId)
        getComentarios(args.peluqueriaId)

    }

    override fun onRowClick(){}

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun hideKeyboard() {//view: View) {
        (context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).apply{
            hideSoftInputFromWindow(
                binding.nuevoComentario.windowToken,
                0
            )
        }
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

    private fun publicarComentario(){
        val gson = GsonBuilder().create()
        val comentario = Review(
            binding.nuevoComentario.text.toString(), binding.nuevaPuntuacion.rating.toFloat(), args.peluqueriaId, 1
        )//TODO:CAMBIAR EL USUARIOID

        var body = gson.toJsonTree(comentario).asJsonObject
        body.remove("id")
        APIService.create().postNewReview(body).enqueue(object : Callback<Review> {
            override fun onResponse(call: Call<Review>, response: Response<Review>) {
                if (response.isSuccessful) {
                    Log.e(ContentValues.TAG, response.body().toString())
                    Toast.makeText(activity, "Comentario subido con éxito", Toast.LENGTH_LONG).show()
                    rAdapter.addReview(response.body()!!)
                    rRecyclerView.adapter = rAdapter
                    val reviewLayoutManager = LinearLayoutManager(activity)
                    rRecyclerView.layoutManager = reviewLayoutManager
                    /*response.body()!!.comentario == comentario.comentario
                    response.body()!!.calificacion == comentario.calificacion
                    response.body()!!.peluqueriaId == comentario.peluqueriaId
                    response.body()!!.usuarioId == comentario.usuarioId*/
                } else {
                    showError()
                }
            }

            override fun onFailure(call: Call<Review>, t: Throwable) {
                Log.e(ContentValues.TAG, "onFailure: Ha fallado la llamada")
            }
        })
    }

}