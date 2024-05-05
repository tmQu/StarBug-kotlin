package intech.co.starbug.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import intech.co.starbug.activity.HistoryDetailActivity
import intech.co.starbug.R
import intech.co.starbug.adapter.OrderHistoryAdapter
import intech.co.starbug.model.OrderModel
import intech.co.starbug.model.cart.CartItemModel
import intech.co.starbug.model.cart.DetailCartItem

class HistoryFragment : Fragment() {
    private lateinit var layout: View
    private lateinit var spinnerTab: Spinner
    private lateinit var allHistory: MutableList<OrderModel>
    private lateinit var listHistory: MutableList<OrderModel>

    private lateinit var history_rv: RecyclerView
    private lateinit var listStatus: Array<String>

    private lateinit var listener: ChildEventListener

    private var filter = listOf<String>()

    override fun onDestroy() {
        super.onDestroy()
        val dbRef = FirebaseDatabase.getInstance().getReference("Orders")
        dbRef.removeEventListener(listener)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        layout = inflater.inflate(R.layout.fragment_history, container, false)
        history_rv = layout.findViewById<RecyclerView>(R.id.rv_history)
        spinnerTab = layout.findViewById<Spinner>(R.id.spinnerTab)
        listStatus = resources.getStringArray(R.array.order_status)

        val listCart = listOf<DetailCartItem>(
            DetailCartItem(CartItemModel(), null)
        ).toMutableList()

        allHistory = mutableListOf()
        listHistory = mutableListOf()

        allHistory = mutableListOf(
        )
        filter = listOf(listStatus[0])
        setSpinnerListener()

        getOrderHistory()

        return layout
    }


    fun setUpHistoryView() {
        filterHistory()

        val adapter = OrderHistoryAdapter(listHistory, activity?.baseContext!!)
        history_rv.adapter = adapter
        history_rv.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        adapter.onItemClick = { view, position ->
            val intent = Intent(activity, HistoryDetailActivity::class.java)
            Log.d("HistoryFragment", "Order before putExtra: ${listHistory[position].id}")
            intent.putExtra("order", listHistory[position].id)
            startActivity(intent)
        }
    }

    fun filterHistory() {
        listHistory.clear()
        for (history in allHistory) {
            if (filter.contains(history.status)) {
                listHistory.add(history)
            }
        }
    }


    private fun getOrderHistory() {
        // get order history from firebase
        val uesrUid = Firebase.auth.currentUser?.uid
        val dbRef = FirebaseDatabase.getInstance().getReference("Orders")
        val query = dbRef.orderByChild("uidUser").equalTo(uesrUid)
        listener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val history = snapshot.getValue(OrderModel::class.java)
                if (history != null) {
                    allHistory.add(history)
                    setUpHistoryView()
                }

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val history = snapshot.getValue(OrderModel::class.java)
                if (history != null) {
                    for (i in 0 until allHistory.size) {
                        if (allHistory[i].id == history.id) {
                            allHistory[i] = (history)
                            break
                        }
                    }
                    if (filter.contains(history.status)) {
                        setUpHistoryView()
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }
        query.addChildEventListener(listener)
    }

    private fun setSpinnerListener() {
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listStatus)
        spinnerTab.adapter = adapter

        spinnerTab.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> {
                        filter = listOf(listStatus[0])
                    }

                    1 -> {
                        filter = listOf(listStatus[1])
                    }

                    2 -> {
                        filter = listOf(listStatus[2])
                    }
                    3 -> {
                        filter = listOf(listStatus[3])
                    }

                    4 -> {
                        filter = listOf(listStatus[4])
                    }

                    5 -> {
                        filter = listOf(listStatus[5])
                    }

                    6 -> {
                        filter = listStatus.toList()
                        Log.i("HistoryFragment", "Filter: ${filter.size})")

                    }
                }
                setUpHistoryView()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }

}