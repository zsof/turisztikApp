package hu.bme.aut.android.turisztikapp.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import hu.bme.aut.android.turisztikapp.R
import hu.bme.aut.android.turisztikapp.data.Comment
import hu.bme.aut.android.turisztikapp.data.Place
import hu.bme.aut.android.turisztikapp.databinding.RowCommentBinding


class CommentAdapter(private val id: String) :
    ListAdapter<Comment, CommentAdapter.ViewHolder>(itemCallback) {

    private val commentList: MutableList<Comment> = mutableListOf()

    inner class ViewHolder(binding: RowCommentBinding) : RecyclerView.ViewHolder(binding.root) {
        val commentText: TextView = binding.rowCommentText
        val userNameText: TextView = binding.userName
        var commentItem: Comment? = null
        var deleteComment: ImageButton = binding.rowCommentDelete

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RowCommentBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = getItem(position)
        holder.commentItem = comment
        holder.commentText.text = comment.comment.replaceFirstChar { it.uppercase() }
        holder.userNameText.text = comment.userName

        holder.deleteComment.setOnClickListener {
            removeComment(holder.commentItem)
        }


        /*if (position % 2 == 1) {
            holder.itemView.setBackgroundColor(Color.parseColor("#d0f7eb"));

        } else {
            holder.itemView.setBackgroundColor(Color.parseColor(R.color));

        }*/
        /*  holder.commentRate.text=comment.rate.toString()

          holder.commentRate.setOnClickListener {
            //  showDialog()
          }*/

    }

    fun addComment(comment: Comment?) {
        comment ?: return
        if (id == comment.placeId) {
            commentList += (comment)
            commentList.sortBy { it.comment }
            submitList(commentList)
        }


    }

    fun removeComment(comment: Comment?) {
        comment ?: return
        commentList -= (comment)
        submitList(commentList)
    }

    /*private fun showDialog() {
        val popDialog = AlertDialog.Builder()
        val linearLayout = LinearLayout(parent.context)
        val rating = RatingBar(context)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        rating.layoutParams = lp
        rating.numStars = 5
        rating.stepSize = 1f

        //add ratingBar to linearLayout
        linearLayout.addView(rating)
        popDialog.setIcon(android.R.drawable.btn_star_big_on)
        popDialog.setTitle("Értékelés: ")

        //add linearLayout to dailog
        popDialog.setView(linearLayout)
        rating.onRatingBarChangeListener =
            RatingBar.OnRatingBarChangeListener { ratingBar, v, b -> println("Rated val:$v") }


        // Button OK
        popDialog.setPositiveButton(android.R.string.ok) { dialoginterface, i ->
            .setText(rating.progress.toString())

        }
            .setNegativeButton("Cancel", null)
        popDialog.create()
        popDialog.show()
    }*/


    companion object {
        object itemCallback : DiffUtil.ItemCallback<Comment>() {
            override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
                return oldItem.id == newItem.id
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
                return oldItem == newItem
            }
        }
    }


}

