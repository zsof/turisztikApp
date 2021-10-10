package hu.bme.aut.android.turisztikapp.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import hu.bme.aut.android.turisztikapp.data.Place
import hu.bme.aut.android.turisztikapp.databinding.RowPlacesBinding


class PlaceListAdapter(private val context: Context) :
        ListAdapter<Place, PlaceListAdapter.PlaceViewHolder>(itemCallback) {

    private val placeList: MutableList<Place> = mutableListOf()
    private var lastPosition = -1

    class PlaceViewHolder(binding: RowPlacesBinding) : RecyclerView.ViewHolder(binding.root) {
        val textPlaceName: TextView = binding.rowPlaceName
        val textPlaceAddress: TextView = binding.rowPlaceAdress
        val imagePlace: ImageView = binding.rowPlaceImage
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            PlaceViewHolder(
                    RowPlacesBinding.inflate(
                            LayoutInflater.from(parent.context),
                            parent,
                            false
                    )
            )

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val places = placeList[position]
        holder.textPlaceName.text = places.name
        holder.textPlaceAddress.text = places.address

        if (places.image.isNullOrBlank()) {
            holder.imagePlace.visibility = View.GONE
        } else {
            Glide.with(context)
                    .load(places.image)
                    .into(holder.imagePlace)
            holder.imagePlace.visibility = View.VISIBLE
        }
        if (position % 2 == 1) {
            holder.itemView.setBackgroundColor(Color.parseColor("#b3f5d4"));
            //  holder.imageView.setBackgroundColor(Color.parseColor("#FFFFFF"));
        } else {
            holder.itemView.setBackgroundColor(Color.parseColor("#cdfaf4"));
            //  holder.imageView.setBackgroundColor(Color.parseColor("#FFFAF8FD"));
        }
        setAnimation(holder.itemView, position)
    }

    fun addPlace(place: Place?) {
        place ?: return

        placeList += (place)
        submitList((placeList))
    }

    private fun setAnimation(viewToAnimate: View, position: Int) {
        if (position > lastPosition) {
            val animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left)
            viewToAnimate.startAnimation(animation)
            lastPosition = position
        }
    }

    companion object {
        object itemCallback : DiffUtil.ItemCallback<Place>() {
            override fun areItemsTheSame(oldItem: Place, newItem: Place): Boolean {
                return oldItem == newItem
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: Place, newItem: Place): Boolean {
                return oldItem == newItem
            }
        }
    }
}
