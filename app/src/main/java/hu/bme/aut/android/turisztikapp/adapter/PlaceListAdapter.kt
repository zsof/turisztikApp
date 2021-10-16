package hu.bme.aut.android.turisztikapp.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import hu.bme.aut.android.turisztikapp.R
import hu.bme.aut.android.turisztikapp.data.Category
import hu.bme.aut.android.turisztikapp.data.Place
import hu.bme.aut.android.turisztikapp.databinding.RowPlacesBinding
import hu.bme.aut.android.turisztikapp.fragment.PlaceListFragmentDirections


class PlaceListAdapter() :
    ListAdapter<Place, PlaceListAdapter.PlaceViewHolder>(itemCallback) {

    private val placeList: MutableList<Place> = mutableListOf()
    private var lastPosition = -1
    //  private val listener: OnItemCLickListener? = null

    inner class PlaceViewHolder(binding: RowPlacesBinding) : RecyclerView.ViewHolder(binding.root) {
        val textPlaceName: TextView = binding.rowPlaceName
        val textPlaceAddress: TextView = binding.rowPlaceAdress
        val imagePlace: ImageView = binding.rowPlaceImage
        val imagePlaceCategory: ImageView = binding.rowPlaceCategory
        var placeItem: Place? = null

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        return PlaceViewHolder(
            RowPlacesBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = getItem(position)
        holder.placeItem = place
        holder.textPlaceName.text = place.name
        holder.textPlaceAddress.text = place.address
        getImageResource(place.category)?.let { holder.imagePlaceCategory.setImageResource(it) }

        holder.textPlaceName.setOnClickListener {
            val action =
                PlaceListFragmentDirections.actionPlacelistToDetails(letter = place)
            println(place.id)
            holder.itemView.findNavController().navigate(action)
        }
        /* holder.mMapView.getMapAsync(new OnMapReadyCallback()
         {

             override fun onMapReady(GoogleMap googleMap) {
                 holder.mMapView = googleMap;

                 if (holder.mMapView != null) {
                     holder.mMapView.addMarker(...);
                 }
             }
         }*/

        if (place.image.isNullOrBlank()) {
            holder.imagePlace.visibility = View.GONE
        } else {
            Glide.with(holder.imagePlace)
                .load(place.image)
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
        //  setAnimation(holder.itemView, position)
    }

    @DrawableRes
    private fun getImageResource(category: Category?) = when (category) {
        Category.MÚZEUM -> R.drawable.ic_camera
        Category.Könyvtár -> R.drawable.ic_bank

        else -> null
    }

    fun addPlace(place: Place?) {
        place ?: return
        placeList += (place)
        placeList.sortBy { it.name.toUpperCase() }
        placeList.reverse()
        submitList(placeList)

    }

    fun removePlace(place: Place?) {
        place ?: return
        placeList -= (place)
        placeList.sortBy { it.name.toString() }
        placeList.reverse()
        submitList(placeList)
    }

    /*private fun setAnimation(viewToAnimate: View, position: Int) {
        if (position > lastPosition) {
            val animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left)
            viewToAnimate.startAnimation(animation)
            lastPosition = position
        }
    }*/


    companion object {
        object itemCallback : DiffUtil.ItemCallback<Place>() {
            override fun areItemsTheSame(oldItem: Place, newItem: Place): Boolean {
                return oldItem.id == newItem.id
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: Place, newItem: Place): Boolean {
                return oldItem == newItem
            }
        }
    }


}
