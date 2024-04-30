package intech.co.starbug.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import intech.co.starbug.R
import intech.co.starbug.model.cart.DetailCartItem

class DetailCartItemAdapter(private val items: List<DetailCartItem>) : RecyclerView.Adapter<DetailCartItemAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val orderImg: ImageView = view.findViewById(R.id.order_img)
        val orderDate: TextView = view.findViewById(R.id.order_date)
        val orderTotalPrice: TextView = view.findViewById(R.id.order_total_price)
        val orderQuantity: TextView = view.findViewById(R.id.order_quantity)
        val orderSize: TextView = view.findViewById(R.id.order_size)
        val orderIce: TextView = view.findViewById(R.id.order_ice)
        val orderSugar: TextView = view.findViewById(R.id.order_sugar)
        val orderTemp: TextView = view.findViewById(R.id.order_temp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.history_detail_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        Picasso.get().load(item.product?.img?.get(0)).into(holder.orderImg)
        holder.orderDate.text = item.product?.name
        holder.orderTotalPrice.text = "$${item.getProductPrice()}"
        holder.orderQuantity.text = item.cartItemModel.quantity.toString()
        holder.orderSize.text = item.cartItemModel.size
        holder.orderIce.text = item.cartItemModel.amountIce.toString()
        holder.orderSugar.text = item.cartItemModel.amountSugar.toString()
        holder.orderTemp.text = item.cartItemModel.temperature.toString()

    }
    override fun getItemCount() = items.size
}