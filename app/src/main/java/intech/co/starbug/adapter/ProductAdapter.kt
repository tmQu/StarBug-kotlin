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
import intech.co.starbug.activity.admin.comment.CommentManage
import intech.co.starbug.model.ProductModel

class ProductAdapter(
    private var itemList: List<ProductModel> = emptyList(),
    private var onDetailClick: ((ProductModel) -> Unit)? = null,
    private var onDeleteClick: ((ProductModel) -> Unit)? = null
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {
    var onItemClick: ((View, Int) -> Unit)? = null
    private lateinit var context: Context // Thêm một biến context

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemImageView: ImageView = itemView.findViewById(R.id.imageViewProduct)
        val itemNameTextView: TextView = itemView.findViewById(R.id.textViewProductName)
        val itemPriceTextView: TextView = itemView.findViewById(R.id.textViewNumPrice)
        val itemCategoryTextView: TextView = itemView.findViewById(R.id.textViewTxtCategory)
        val detailButton: Button = itemView.findViewById(R.id.detailButton)
        val deleteImageButton: ImageButton = itemView.findViewById(R.id.deleteImageButton)
        val commentBtn: Button = itemView.findViewById(R.id.comment_button)
        init {
            itemView.setOnClickListener {
                onItemClick?.invoke(it, adapterPosition)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        context = parent.context // Lấy context từ ViewGroup
        val itemView = LayoutInflater.from(context).inflate(R.layout.product_management_item, parent, false)
        val viewHolder = ProductViewHolder(itemView)

        // Xử lý sự kiện click cho nút "Detail"
//        viewHolder.detailButton.setOnClickListener {
//            val position = viewHolder.adapterPosition
//            if (position != RecyclerView.NO_POSITION) {
//                val product = itemList[position]
//                onDetailClick?.invoke(product)
//            }
//        }

        itemView.setOnClickListener {
            val position = viewHolder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val product = itemList[position]
                onDetailClick?.invoke(product)
            }
        }

        viewHolder.commentBtn.setOnClickListener {
            val position = viewHolder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val product = itemList[position]
                val intent = Intent(context, CommentManage::class.java)
                intent.putExtra("product_id", product.id)
                intent.putExtra("product", product)
                context.startActivity(intent)
            }
        }

        // Xử lý sự kiện click cho nút "Delete"
        viewHolder.deleteImageButton.setOnClickListener {
            val position = viewHolder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val product = itemList[position]
                onDeleteClick?.invoke(product)
            }
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val currentItem = itemList[position]
        holder.itemNameTextView.text = currentItem.name
        holder.itemPriceTextView.text = currentItem.price.toString()
        holder.itemCategoryTextView.text = currentItem.category
        Picasso.get().load(currentItem.img[currentItem.img.size - 1]).into(holder.itemImageView)

    }

    override fun getItemCount() = itemList.size

    fun submitList(list: List<ProductModel>) {
        itemList = list
        notifyDataSetChanged()
    }

    // Phương thức public để thiết lập listener cho sự kiện nhấp vào item
    fun setOnItemClickListener(listener: (ProductModel) -> Unit) {
        onItemClick = { view, position ->
            val item = itemList[position]
            listener.invoke(item)
        }
    }

    fun setOnDetailClickListener(listener: (ProductModel) -> Unit) {
        onDetailClick = listener
    }

    fun setOnDeleteClickListener(listener: (ProductModel) -> Unit) {
        onDeleteClick = listener
    }


}
