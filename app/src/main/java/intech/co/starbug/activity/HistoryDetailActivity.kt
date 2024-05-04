package intech.co.starbug.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import intech.co.starbug.R
import intech.co.starbug.adapter.DetailCartItemAdapter
import intech.co.starbug.model.OrderModel
import intech.co.starbug.utils.Utils
import java.text.SimpleDateFormat
import java.util.Locale

class HistoryDetailActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var orderStatus: TextView
    private lateinit var orderDate: TextView
    private lateinit var orderQuantity: TextView
    private lateinit var orderPrice: TextView
    private lateinit var cancelButton: ImageButton
    private lateinit var order: OrderModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_detail)

        cancelButton = findViewById(R.id.imageBackButton)
        cancelButton.setOnClickListener {
            finish()
        }
        orderStatus = findViewById(R.id.order_status)
        orderDate = findViewById(R.id.order_date)
        orderQuantity = findViewById(R.id.order_quantity)
        orderPrice = findViewById(R.id.order_price)

        val orderId = intent.getStringExtra("order")
        if (orderId != null) {
            val dbRef = FirebaseDatabase.getInstance().getReference("Orders")
            dbRef.child(orderId).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val data = snapshot.getValue(OrderModel::class.java)
                    if (data != null) {
                        order = data
                        setUpView()

                    } else {
                        // Handle the case where order is null
                        Log.e("HistoryDetailActivity", "Order is null")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                    Log.e("HistoryDetailActivity", "Error getting Order: ", error.toException())
                }
            })
        } else {
            // Handle the case where orderId is null
            Log.e("HistoryDetailActivity", "Order ID is null")
        }
    }

    private fun setUpView()
    {
        val msgView = findViewById<TextView>(R.id.message)
        if (order.status == resources.getStringArray(R.array.order_status)[5])
        {
            var sorryMsg = "Sorry your order has been cancelled."
            if (order.reason != "")
            {
                sorryMsg += "\nDue to ${order.reason}"
            }
            msgView.text = sorryMsg
            msgView.visibility = View.VISIBLE
        }
        else {
            msgView.visibility = View.GONE
        }
        orderStatus.text = order.status
        orderDate.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(order.orderDate)
        orderQuantity.text = order.listCartItem.sumOf { it.quantity }.toString()
        orderPrice.text = Utils.formatMoney(order.price)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this@HistoryDetailActivity)
        recyclerView.adapter = DetailCartItemAdapter(order.listCartItem)
    }
}
