package com.example.tulook.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tulook.R
import com.example.tulook.base.BaseViewHolder
import com.example.tulook.databinding.BannerRowItemBinding
import com.squareup.picasso.Picasso


class BannerListAdapter(
    private val imagenesList: List<String>?
) :
    RecyclerView.Adapter<BaseViewHolder<*>>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return BannerViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.banner_row_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        when (holder) {
            is BannerViewHolder -> holder.bind(imagenesList!![position], position, false)
        }
    }

    override fun getItemCount(): Int {
        return imagenesList!!.size
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    inner class BannerViewHolder(itemView: View) : BaseViewHolder<String>(itemView) {
        val binding = BannerRowItemBinding.bind(itemView)

        override fun bind(item: String, position: Int, isActivated: Boolean) {
            Picasso.get().load(Uri.parse(item)).fit().centerCrop().into(binding.serviceImage)

        }
    }
}