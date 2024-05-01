package intech.co.starbug.adapter

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.firebase.database.FirebaseDatabase
import intech.co.starbug.R
import intech.co.starbug.dialog.ConfirmDialog
import intech.co.starbug.dialog.ReasonDialog
import intech.co.starbug.model.CommentModel
import android.content.Context


class CommentAdminAdapter(val listComment: List<CommentModel>, val onReplyClick: (Int) -> Unit, val onDeleteReplyClick: (Int) -> Unit, val onDeleteCommentClick: (Int) -> Unit): RecyclerView.Adapter<CommentAdminAdapter.CommentViewHolder>(){



    inner class CommentViewHolder(val view: View): RecyclerView.ViewHolder(view)
    {
         val avatar = view.findViewById<ImageView>(R.id.avatar)
         val user_name = view.findViewById<TextView>(R.id.user_name)
         val rating = view.findViewById<RatingBar>(R.id.ratingBar)
         val comment_txt = view.findViewById<TextView>(R.id.txt_comment)
        val replyBtn = view.findViewById<Button>(R.id.reply_btn)
        val deleteComment =  view.findViewById<ImageButton>(R.id.delete_comment)

        fun bind(comment: CommentModel)
        {
            if(comment.avatar != "")
            {
                avatar.load(comment.avatar){
                    crossfade(true)
                    placeholder(R.drawable.default_avatar)
                }
            }

            user_name.text = comment.user_name
            rating.rating = comment.rating.toFloat()
            comment_txt.text = comment.comment
            deleteComment.setOnClickListener {
                onDeleteCommentClick(adapterPosition)
            }
            if(comment.reply != "")
            {
                view.findViewById<CardView>(R.id.reply_container).visibility = VISIBLE
                view.findViewById<TextView>(R.id.reply).text = comment.reply

                view.findViewById<ImageButton>(R.id.edit_btn).setOnClickListener {
                    onReplyClick(adapterPosition)
                }

                view.findViewById<ImageButton>(R.id.delete_reply).setOnClickListener{
                    onDeleteReplyClick(adapterPosition)
                }



                replyBtn.visibility = View.GONE

            }else
            {
                view.findViewById<CardView>(R.id.reply_container).visibility = GONE
                replyBtn.visibility = View.VISIBLE
            }

            replyBtn.setOnClickListener {
                onReplyClick(adapterPosition)
            }
        }
    }

    private fun deleteTheReply(commentId: String) {
        val db = FirebaseDatabase.getInstance().getReference("Comment")
        db.child(commentId).removeValue()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.comment_admin_row, parent, false)
        return CommentViewHolder(view)

    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        return holder.bind(listComment[position])
    }

    override fun getItemCount(): Int {
        Log.i("test",listComment.size.toString() )
        return listComment.size
    }


}