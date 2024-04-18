package intech.co.starbug

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import intech.co.starbug.model.PromotionModel

class PromotionAdapter(
    private var itemList: List<PromotionModel> = emptyList(),
    private var onDetailClick: ((PromotionModel) -> Unit)? = null,
    private var onDeleteClick: ((PromotionModel) -> Unit)? = null
) : RecyclerView.Adapter<PromotionAdapter.PromotionViewHolder>() {
    var onItemClick: ((View, Int) -> Unit)? = null
    private lateinit var context: Context // Thêm một biến context

    inner class PromotionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemImageView: ImageView = itemView.findViewById(R.id.imageViewPromotion)
        val itemNameTextView: TextView = itemView.findViewById(R.id.textViewPromotionName)
        val itemStartDay: TextView = itemView.findViewById(R.id.textViewStartDay)
        val itemEndDay: TextView = itemView.findViewById(R.id.textViewEndDay)
        val detailButton: Button = itemView.findViewById(R.id.detailButton)
        val deleteImageButton: ImageButton = itemView.findViewById(R.id.deleteImageButton)
        init {
            itemView.setOnClickListener {
                onItemClick?.invoke(it, adapterPosition)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PromotionViewHolder {
        context = parent.context // Lấy context từ ViewGroup
        val itemView = LayoutInflater.from(context).inflate(R.layout.promotion_management_item, parent, false)
        val viewHolder = PromotionViewHolder(itemView)

        // Xử lý sự kiện click cho nút "Detail"
        viewHolder.detailButton.setOnClickListener {
            val position = viewHolder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val Promotion = itemList[position]
                onDetailClick?.invoke(Promotion)
            }
        }

        // Xử lý sự kiện click cho nút "Delete"
        viewHolder.deleteImageButton.setOnClickListener {
            val position = viewHolder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val Promotion = itemList[position]
                onDeleteClick?.invoke(Promotion)
            }
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: PromotionViewHolder, position: Int) {
        val currentItem = itemList[position]
        holder.itemNameTextView.text = currentItem.name
        holder.itemStartDay.text = currentItem.startDay
        holder.itemEndDay.text = currentItem.endDay
        Picasso.get().load(currentItem.img).into(holder.itemImageView)

    }

    override fun getItemCount() = itemList.size

    fun submitList(list: List<PromotionModel>) {
        itemList = list
        notifyDataSetChanged()
    }

    // Phương thức public để thiết lập listener cho sự kiện nhấp vào item
    fun setOnItemClickListener(listener: (PromotionModel) -> Unit) {
        onItemClick = { view, position ->
            val item = itemList[position]
            listener.invoke(item)
        }
    }

    fun setOnDetailClickListener(listener: (PromotionModel) -> Unit) {
        onDetailClick = listener
    }

    fun setOnDeleteClickListener(listener: (PromotionModel) -> Unit) {
        onDeleteClick = listener
    }


}
