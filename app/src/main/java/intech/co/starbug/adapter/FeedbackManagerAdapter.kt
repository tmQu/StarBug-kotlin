package intech.co.starbug.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import intech.co.starbug.model.FeedbackModel
import intech.co.starbug.R

class FeedbackManagerAdapter(private val feedbackList: List<FeedbackModel>) : RecyclerView.Adapter<FeedbackManagerAdapter.FeedbackViewHolder>() {

    private var listener: ((FeedbackModel) -> Unit)? = null

    fun setOnItemClickListener(listener: (FeedbackModel) -> Unit) {
        this.listener = listener
    }

    inner class FeedbackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val textViewTitle: TextView = itemView.findViewById(R.id.feedbackTitleTextView)
        val textViewDescription: TextView = itemView.findViewById(R.id.feedbackDescriptionTextView)

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
        holder.textViewTitle.text = currentItem.title
        holder.textViewDescription.text = currentItem.description
    }

    override fun getItemCount() = feedbackList.size
}
