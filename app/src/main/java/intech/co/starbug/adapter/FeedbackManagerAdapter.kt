package intech.co.starbug.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import intech.co.starbug.model.FeedbackModel
import intech.co.starbug.R
import intech.co.starbug.model.ProductModel

class FeedbackManagerAdapter(private val feedbackList: List<FeedbackModel>) : RecyclerView.Adapter<FeedbackManagerAdapter.FeedbackViewHolder>() {

    private var itemList: List<ProductModel> = emptyList()
    private var listener: ((FeedbackModel) -> Unit)? = null

    fun setOnItemClickListener(listener: (FeedbackModel) -> Unit) {
        this.listener = listener
    }

    inner class FeedbackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val textViewSender: TextView = itemView.findViewById(R.id.feedbackSenderNameTextView)
        val textViewDescription: TextView = itemView.findViewById(R.id.feedbackDescriptionTextView)
        val feedbackImageView: ImageView = itemView.findViewById(R.id.feedbackImageView)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener?.invoke(feedbackList[position])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedbackViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.feedback_manager_item, parent, false)
        return FeedbackViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FeedbackViewHolder, position: Int) {
        val currentItem = feedbackList[position]

        Picasso.get().load(currentItem.imageUrl).into(holder.feedbackImageView)
        holder.textViewSender.text= currentItem.senderName
        holder.textViewDescription.text = currentItem.description
    }

    override fun getItemCount() = feedbackList.size
}
