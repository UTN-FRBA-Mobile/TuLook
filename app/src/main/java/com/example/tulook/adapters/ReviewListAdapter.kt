package com.example.tulook.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tulook.R
import com.example.tulook.base.BaseViewHolder
import com.example.tulook.databinding.ReviewRowItemBinding
import com.example.tulook.model.Review
import kotlin.random.Random


class ReviewListAdapter(
    private val reviewList: MutableList<Review>?,
    val itemClickListener: ReviewListAdapter.onReviewClickListener
) :
    RecyclerView.Adapter<BaseViewHolder<*>>() {

    interface onReviewClickListener {
        fun onRowClick()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {

        return ReviewViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.review_row_item, parent, false))

    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        when (holder) {
            is ReviewViewHolder -> holder.bind(reviewList!![position], position, false)

        }
    }

    override fun getItemCount(): Int {
        return reviewList!!.size
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    inner class ReviewViewHolder(itemView: View) : BaseViewHolder<Review>(itemView) {
        val binding = ReviewRowItemBinding.bind(itemView)

        override fun bind(item: Review, position: Int, isActivated: Boolean) {

            binding.rating.setRating(item.calificacion)
            binding.comentario.text = item.comentario

            //val randomValues = List(10) { Random.nextInt(0, 100) }

            //val perfil = "ic_perfil_" + randomValues.toString()
            binding.imagen.setImageResource(R.mipmap.ic_perfil_1)
        }
    }

}