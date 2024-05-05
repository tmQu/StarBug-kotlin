package intech.co.starbug.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import intech.co.starbug.R
import intech.co.starbug.model.ProductModel
import com.squareup.picasso.Picasso
import intech.co.starbug.utils.Utils

class ItemAdapter(private val itemList: List<ProductModel>) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {
    var onItemClick: ((View,Int) -> Unit)? = null
    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemImageView: ImageView = itemView.findViewById(R.id.itemPicture)
        val itemNameTextView: TextView = itemView.findViewById(R.id.itemName)
        val itemName2TextView: TextView = itemView.findViewById(R.id.itemName2)
        val itemPriceTextView: TextView = itemView.findViewById(R.id.itemPrice)

        init {
            itemView.setOnClickListener{
                onItemClick?.invoke(it.findViewById(R.id.itemPicture),adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.product_card, parent, false)
        return ItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val currentItem = itemList[position]
        holder.itemNameTextView.text = currentItem.name
        holder.itemName2TextView.text = currentItem.category
        holder.itemPriceTextView.text = Utils.formatPrice(currentItem.price)
        Picasso.get().load(currentItem.img[0]).into(holder.itemImageView)
    }

    override fun getItemCount() = itemList.size
}

