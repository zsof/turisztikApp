package hu.bme.aut.android.turisztikapp.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import hu.bme.aut.android.turisztikapp.data.Comment
import hu.bme.aut.android.turisztikapp.databinding.RowCommentBinding

class CommentAdapter(private val id: String) :
    ListAdapter<Comment, CommentAdapter.ViewHolder>(ItemCallback) {

    private val commentList: MutableList<Comment> = mutableListOf()

    inner class ViewHolder(binding: RowCommentBinding) : RecyclerView.ViewHolder(binding.root) {
        val commentText: TextView = binding.rowCommentText
        val userNameText: TextView = binding.userName
        var commentItem: Comment? = null
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
    }

    fun addComment(comment: Comment?) {
        comment ?: return
        if (id == comment.placeId) {
            commentList += (comment)
            commentList.sortBy { it.date }
            submitList(commentList)
        }
    }

    fun removeComment(comment: Comment?) {
        comment ?: return
        commentList -= (comment)
        submitList(commentList)
    }

    companion object {
        object ItemCallback : DiffUtil.ItemCallback<Comment>() {
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