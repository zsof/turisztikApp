package hu.bme.aut.android.turisztikapp.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import hu.bme.aut.android.turisztikapp.data.Comment
import hu.bme.aut.android.turisztikapp.data.Image
import hu.bme.aut.android.turisztikapp.databinding.RowImageBinding


class ImageAdapter(private val id: String) :
    ListAdapter<Image, ImageAdapter.ViewHolder>(itemCallback) {

    private val imageList: MutableList<Image> = mutableListOf()

    inner class ViewHolder(binding: RowImageBinding) : RecyclerView.ViewHolder(binding.root) {
        val rowImageView: ImageView = binding.rowImageView
        var imageItem: Image? = null

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RowImageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val image = getItem(position)
        holder.imageItem = image

        if (image.image.isNullOrBlank()) {
            holder.rowImageView.visibility = View.GONE
        } else {
            Glide.with(holder.rowImageView)
                .load(image.image)
                .into(holder.rowImageView)
            holder.rowImageView.visibility = View.VISIBLE
        }

    }

    fun addImage(image: Image?) {
        image ?: return
        if (id == image.placeId) {
            imageList += (image)
            submitList(imageList)
        }
    }

    fun removeImage(image: Image?) {
        image ?: return
        imageList -= (image)
        submitList(imageList)
    }


    companion object {
        object itemCallback : DiffUtil.ItemCallback<Image>() {
            override fun areItemsTheSame(oldItem: Image, newItem: Image): Boolean {
                return oldItem.id == newItem.id
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: Image, newItem: Image): Boolean {
                return oldItem == newItem
            }
        }
    }

}