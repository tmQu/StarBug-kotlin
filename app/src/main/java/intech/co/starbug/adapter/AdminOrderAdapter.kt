package intech.co.starbug.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import intech.co.starbug.R
import intech.co.starbug.activity.DetailOrderActivity
import intech.co.starbug.activity.admin.order.DetailAdminOrder
import intech.co.starbug.model.BranchModel
import intech.co.starbug.model.OrderModel
import intech.co.starbug.utils.Utils

class AdminOrderAdapter(var allOrder: List<OrderModel>, val context: Context, var branchName: String): RecyclerView.Adapter<AdminOrderAdapter.ViewHolder>() {

    var onItemClick: ((View,Int) -> Unit)? = null
    var listOrder: List<OrderModel> = allOrder.filter { it.paymentInforModel.branchName == branchName || it.paymentInforModel.branchName == ""}.sortedByDescending { it.orderDate }
    inner class ViewHolder(val view: View): RecyclerView.ViewHolder(view) {
        fun bind(order: OrderModel) {
        }
        init {
            view.setOnClickListener{
                val intent = Intent(context, DetailAdminOrder::class.java)
                intent.putExtra("order_id", listOrder[adapterPosition].id)
                startActivity(context, intent, null)
            }
        }
    }

    private fun filterOrderList()
    {
        listOrder = allOrder.filter { it.paymentInforModel.branchName == branchName || it.paymentInforModel.branchName == "All"}
        listOrder.sortedByDescending { it.orderDate }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_admin_order, parent, false)
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
        val customerName = view.findViewById<TextView>(R.id.customer_name)
        val customerPhone = view.findViewById<TextView>(R.id.phone)
        val customerAddress = view.findViewById<TextView>(R.id.address)
        val payMethod = view.findViewById<TextView>(R.id.pay_method)
        val reminderCard = view.findViewById<View>(R.id.reminder_card)
        val approveBtn = view.findViewById<TextView>(R.id.btn_approve)
        val branchName = view.findViewById<TextView>(R.id.branch_name)


        val resources = context.resources
        val listStatus = resources.getStringArray(R.array.order_status)
        branchName.text = order.paymentInforModel.branchName
        orderStatus.setTextColor(resources.getColor(R.color.white))

        val shape = GradientDrawable()

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
            listStatus[2]  -> {
                val drawable = resources.getDrawable(R.drawable.ic_delivering, context.theme)
                orderImg.setImageDrawable(drawable)
                orderStatus.text = "Ready to pickup"
                shape.setColor(resources.getColor(R.color.colorDeliver))
            }
            listStatus[3] -> {
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

        customerName.text = order.paymentInforModel.name
        customerPhone.text = order.paymentInforModel.phone
        customerAddress.text = order.paymentInforModel.address
        payMethod.text = order.paymentInforModel.paymentMethod

        if (order.status == listStatus[3])
        {
            if(order.orderToken != "")
            {
                reminderCard.visibility = View.VISIBLE
            }
        }
        else {
            reminderCard.visibility = View.GONE
        }

        if(order.status == listStatus[0])
        {
            approveBtn.visibility = View.VISIBLE
            approveBtn.setOnClickListener {
                val intent = Intent(context, DetailAdminOrder::class.java)
                intent.putExtra("order_id", order.id)
                startActivity(context, intent, null)
            }
        }
        else {
            approveBtn.visibility = View.GONE
        }


    }
    fun updateBranch(branchName: String)
    {
        this.branchName = branchName
        filterOrderList()
        notifyDataSetChanged()
    }
    fun updateListOrder(listOrder: List<OrderModel>)
    {
        this.allOrder = listOrder
        filterOrderList()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        Log.i("OrderManagementActivity", "getItemCount: ${listOrder.size}")
        return listOrder.size
    }
}