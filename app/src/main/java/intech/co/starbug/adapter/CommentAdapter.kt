package intech.co.starbug.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import intech.co.starbug.R
import intech.co.starbug.model.CommentModel

class CommentAdapter(val listComment: List<CommentModel>): RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(val view: View): RecyclerView.ViewHolder(view)
    {
         val avatar = view.findViewById<ImageView>(R.id.avatar)
         val user_name = view.findViewById<TextView>(R.id.user_name)
         val rating = view.findViewById<RatingBar>(R.id.ratingBar)
         val comment_txt = view.findViewById<TextView>(R.id.txt_comment)

        fun bind(comment: CommentModel)
        {
            if(comment.avatar != "")
            {
//                avatar.load(comment.avatar)
            }
            else {
//                avatar.setImageResource()
            }
            user_name.text = comment.user_name
            rating.rating = comment.rating.toFloat()
            comment_txt.text = comment.comment
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.review_layout, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        return holder.bind(listComment[position])
    }

    override fun getItemCount(): Int {
        return listComment.size
    }
}