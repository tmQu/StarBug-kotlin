package intech.co.starbug.activity.admin.order

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import intech.co.starbug.adapter.AdminOrderAdapter
import intech.co.starbug.model.OrderModel

import intech.co.starbug.R
import intech.co.starbug.model.BranchModel

class OrderManagementActivity : AppCompatActivity() {
    private lateinit var tabLayout: TabLayout
    private lateinit var listOrderRv: RecyclerView
    private lateinit var orderAdapter: AdminOrderAdapter
    private lateinit var listOrder: MutableList<OrderModel>
    private lateinit var database: FirebaseDatabase

    private var listStatus = listOf<String>()
    private var listBranch = BranchModel.BRANCHES
    private var branchName = listBranch[0].name
    private lateinit var childEventListener: ChildEventListener

    private lateinit var query: Query

    private lateinit var spinnerBranch: Spinner
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_order_management)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        database = FirebaseDatabase.getInstance()

        listStatus = resources.getStringArray(R.array.order_status).toList()
        tabLayout = findViewById(R.id.tabAppLayout)
        listOrderRv = findViewById(R.id.list_order)
        spinnerBranch = findViewById(R.id.spinner_branch)


        tabSelected()

        listOrder = mutableListOf()

        orderAdapter = AdminOrderAdapter(listOrder, this, branchName)

        listOrderRv.adapter = orderAdapter
        listOrderRv.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)

        firstQuery()
        setUpSpinnerBranch()

    }

    private fun setUpSpinnerBranch() {
        val listSpinner = mutableListOf<String>()
        listBranch.forEach {
            listSpinner.add(it.name)
        }
        listSpinner.add("All")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listSpinner)
        spinnerBranch.adapter = adapter

        spinnerBranch.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                branchName = listBranch[position].name
                orderAdapter.updateBranch(branchName)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }


    private fun firstQuery() {
        query = database.reference.child("Orders").orderByChild("status").equalTo(listStatus[0])
        childEventListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val order = snapshot.getValue(OrderModel::class.java)
                if (order != null) {
                    listOrder.add(order)
                    orderAdapter.updateListOrder(listOrder)
                    orderAdapter.notifyDataSetChanged()
                }
                Log.i("OrderManagementActivitys", "Order add o:${order?.id}, ${order?.status}")

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val order = snapshot.getValue(OrderModel::class.java)
                if (order != null) {
                    Log.i("OrderManagementActivitys", "Order changed o: ${order.id}")
                    val index = listOrder.indexOfFirst { it.id == order.id }
                    if (index != -1) {
                        if (listOrder[index].status != order.status) {
                            Log.i("OrderManagementActivitys", "Order changed: ${order.id}")
                            listOrder.removeAt(index)
                            orderAdapter.updateListOrder(listOrder)
                            orderAdapter.notifyItemRemoved(index)
                        } else {
                            listOrder.set(index, order)
                            orderAdapter.updateListOrder(listOrder)
                            orderAdapter.notifyItemChanged(index)
                        }
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val order = snapshot.getValue(OrderModel::class.java)
                if (order != null) {
                    val index = listOrder.indexOfFirst { it.id == order.id }
                    if (index != -1) {
                        listOrder.removeAt(index)
                        orderAdapter.updateListOrder(listOrder)
                        orderAdapter.notifyItemRemoved(index)
                    }
                    Log.i("OrderManagementActivitys", "Order remove o: ${order.id}")
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Do nothing
            }
            override fun onCancelled(error: DatabaseError) {
                // Do nothing
            }

        }

        query.addChildEventListener(childEventListener)
    }

    private fun tabSelected()
    {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {

                        queryOrders(listStatus[0])
                    }
                    1 -> {
                        queryOrders(listStatus[1])
                    }
                    2 -> {
                        queryOrders(listStatus[2])

                    }
                    3 -> {

                        queryOrders(listStatus[3])
                    }
                    4 -> {
                        queryOrders(listStatus[4])
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Do nothing
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Do nothing
            }
        })
    }

    private fun queryOrders(status: String) {

        listOrder = mutableListOf()
        orderAdapter.updateListOrder(listOrder)
        orderAdapter.notifyDataSetChanged()
        query?.removeEventListener(childEventListener)
        query = database.reference.child("Orders").orderByChild("status").equalTo(status)
        query.addChildEventListener(childEventListener)
    }
}