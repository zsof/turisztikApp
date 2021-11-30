package hu.bme.aut.android.turisztikapp.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
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

class PlaceListAdapter :
    ListAdapter<Place, PlaceListAdapter.PlaceViewHolder>(ItemCallback), Filterable {

    private val placeList: MutableList<Place> = mutableListOf()
    private var filterList: MutableList<Place> = placeList

    inner class PlaceViewHolder(binding: RowPlacesBinding) : RecyclerView.ViewHolder(binding.root) {
        val textPlaceName: TextView = binding.rowPlaceName
        val textPlaceAddress: TextView = binding.rowPlaceAdress
        val imagePlace: ImageView = binding.rowPlaceImage
        val imagePlaceCategory: ImageView = binding.rowPlaceCategory
        val ratePlace: TextView = binding.rowPlaceRate
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
        holder.ratePlace.text = place.rate.toString()
        holder.textPlaceAddress.text = place.address
        getImageResource(place.category)?.let { holder.imagePlaceCategory.setImageResource(it) }

        holder.itemView.setOnClickListener {
            val action =
                PlaceListFragmentDirections.actionPlacelistToDetails(place = place)
            holder.itemView.findNavController().navigate(action)
        }

        if (place.image.isNullOrBlank()) {
            holder.imagePlace.visibility = View.GONE
        } else {
            Glide.with(holder.imagePlace)
                .load(place.image)
                .into(holder.imagePlace)
            holder.imagePlace.visibility = View.VISIBLE
        }
    }

    private fun getImageResource(category: Category?) = when (category) {
        Category.Museum -> R.drawable.ic_museum
        Category.Library -> R.drawable.ic_library
        Category.ArtGallery -> R.drawable.ic_art_gallery
        Category.Church -> R.drawable.ic_church
        Category.Zoo -> R.drawable.ic_zoo
        Category.Castle -> R.drawable.ic_castle
        else -> null
    }

    fun addPlace(place: Place?) {
        place ?: return
        placeList += (place)
        placeList.sortBy { it.name }
        submitList(placeList)
    }

    fun removePlace(place: Place?) {
        place ?: return
        placeList -= (place)
        placeList.sortBy { it.name }
        submitList(placeList)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val searchString = charSequence.toString()

                if (searchString.isEmpty()) {
                    filterList = placeList

                } else {
                    val tempList: MutableList<Place> = mutableListOf()

                    for (place: Place in placeList) {
                        if (place.name.lowercase().contains(searchString.lowercase()))
                            tempList.add(place)
                    }
                    filterList = tempList
                }
                val filterResults = FilterResults()
                filterResults.values = filterList

                return filterResults
            }

            override fun publishResults(
                charSequence: CharSequence?,
                filterResults: FilterResults?
            ) {
                val newList = filterResults?.values as MutableList<Place>
                submitList(newList)
            }
        }
    }

    companion object {
        object ItemCallback : DiffUtil.ItemCallback<Place>() {
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