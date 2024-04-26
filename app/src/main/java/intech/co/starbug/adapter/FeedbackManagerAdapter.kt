package intech.co.starbug.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import intech.co.starbug.model.FeedbackModel
import intech.co.starbug.R

class FeedbackManagerAdapter(
    private var itemList: List<FeedbackModel> = emptyList(),
    private var onDetailClick: ((FeedbackModel) -> Unit)? = null,
    private var onDeleteClick: ((FeedbackModel) -> Unit)? = null
) : RecyclerView.Adapter<FeedbackManagerAdapter.FeedbackViewHolder>() {
    var onItemClick: ((View, Int) -> Unit)? = null
    private lateinit var context: Context

    inner class FeedbackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewSender: TextView = itemView.findViewById(R.id.feedbackSenderTextView)
        val feedbackImageView: ImageView = itemView.findViewById(R.id.feedbackImageView)
        val feedbackTimeTextView: TextView = itemView.findViewById(R.id.feedbackTimeTextView)
        val detailButton: Button = itemView.findViewById(R.id.detailButton)
        val deleteImageButton: ImageButton = itemView.findViewById(R.id.deleteImageButton)

        init {
            itemView.setOnClickListener {
                onItemClick?.invoke(it, adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedbackViewHolder {
        context = parent.context
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.feedback_manager_item, parent, false)

        Log.i("FeedbackManagerAdapter", "onCreateViewHolder: $itemView")
        val viewHolder = FeedbackViewHolder(itemView)

        // Xử lý sự kiện click cho nút "Detail"
        viewHolder.detailButton.setOnClickListener {
            val position = viewHolder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val feedback = itemList[position]
                onDetailClick?.invoke(feedback)
            }
        }

        // Xử lý sự kiện click cho nút "Delete"
        viewHolder.deleteImageButton.setOnClickListener {
            val position = viewHolder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val feedback = itemList[position]
                onDeleteClick?.invoke(feedback)
            }
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: FeedbackViewHolder, position: Int) {
        val currentItem = itemList[position]

        Picasso.get().load(currentItem.img[currentItem.img.size - 1]).into(holder.feedbackImageView)
        holder.textViewSender.text = currentItem.senderName
        holder.feedbackTimeTextView.text = currentItem.time

        Log.i("FeedbackManagerAdapter", "onBindViewHolder: $currentItem")
    }

    override fun getItemCount() = itemList.size

    fun submitList(list: List<FeedbackModel>) {
        itemList = list
        notifyDataSetChanged()
    }

    // Phương thức public để thiết lập listener cho sự kiện nhấp vào item
    fun setOnItemClickListener(listener: (FeedbackModel) -> Unit) {
        onItemClick = { view, position ->
            val item = itemList[position]
            listener.invoke(item)
        }
    }

    fun setOnDetailClickListener(listener: (FeedbackModel) -> Unit) {
        onDetailClick = listener
    }

    fun setOnDeleteClickListener(listener: (FeedbackModel) -> Unit) {
        onDeleteClick = listener
    }
}