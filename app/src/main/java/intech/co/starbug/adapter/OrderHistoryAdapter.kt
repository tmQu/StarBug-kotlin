package intech.co.starbug.adapter

import android.app.Activity
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import intech.co.starbug.R
import intech.co.starbug.model.OrderModel
import intech.co.starbug.utils.Utils

class OrderHistoryAdapter(val listOrder: List<OrderModel>, val context: Context): RecyclerView.Adapter<OrderHistoryAdapter.ViewHolder>() {

    var onItemClick: ((View,Int) -> Unit)? = null

    inner class ViewHolder(val view: View): RecyclerView.ViewHolder(view) {
        fun bind(order: OrderModel) {
        }
        init {
            view.setOnClickListener{
                onItemClick?.invoke(it,adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.order_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val view = holder.view
        val order = listOrder[position]

        //            val orderNumber = view.findViewById<TextView>(R.id)

        val orderDate = view.findViewById<TextView>(R.id.order_date)
        val orderTotalPrice = view.findViewById<TextView>(R.id.order_total_price)
        val orderStatus = view.findViewById<TextView>(R.id.order_status)
        val orderImg = view.findViewById<ImageView>(R.id.order_img)
        val orderQuantity = view.findViewById<TextView>(R.id.order_quantity)
        val resources = context.resources
        val listStatus = resources.getStringArray(R.array.order_status)

        orderStatus.setTextColor(resources.getColor(R.color.white))

        val shape = GradientDrawable()
        shape.cornerRadius = 50f

        when(order.status)
        {
            listStatus[0] -> {
                val drawable = resources.getDrawable(R.drawable.ic_wait, context.theme)
                orderImg.setImageDrawable(drawable)
                orderStatus.text = listStatus[0]
                shape.setColor(resources.getColor(R.color.colorWaiting))
            }
            listStatus[1] -> {
                val drawable = resources.getDrawable(R.drawable.ic_prepare, context.theme)
                orderImg.setImageDrawable(drawable)
                orderStatus.text = "Preparing"
                shape.setColor(resources.getColor(R.color.colorApproved))
            }
            listStatus[2],listStatus[3]  -> {
                val drawable = resources.getDrawable(R.drawable.ic_delivering, context.theme)
                orderImg.setImageDrawable(drawable)
                orderStatus.text = "Delivering"
                shape.setColor(resources.getColor(R.color.colorDeliver))
            }
            listStatus[4] -> {
                val drawable = resources.getDrawable(R.drawable.ic_success, context.theme)
                orderImg.setImageDrawable(drawable)
                orderStatus.text = "Success"
                shape.setColor(resources.getColor(R.color.colorSuccess))
            }
            listStatus[5] -> {
                val drawable = resources.getDrawable(R.drawable.ic_unsuccess, context.theme)
                orderImg.setImageDrawable(drawable)
                orderStatus.text = "Unsuccess"
                shape.setColor(resources.getColor(R.color.colorUnsuccess))
            }
        }

        orderStatus.background = shape

        orderDate.text = Utils.convertDate(order.orderDate)
        orderTotalPrice.text = Utils.formatMoney(order.price)
        orderQuantity.text = order.listCartItem.size.toString()

    }

    override fun getItemCount(): Int {
        return listOrder.size
    }
}