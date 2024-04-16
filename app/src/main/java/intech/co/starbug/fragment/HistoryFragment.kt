package intech.co.starbug.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import intech.co.starbug.R
import intech.co.starbug.adapter.OrderHistoryAdapter
import intech.co.starbug.model.OrderModel
import intech.co.starbug.model.cart.CartItemModel
import intech.co.starbug.model.cart.DetailCartItem

class HistoryFragment : Fragment() {
    private lateinit var layout: View
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
        listStatus = resources.getStringArray(R.array.order_status)

        val listCart = listOf<DetailCartItem>(
            DetailCartItem(CartItemModel(), null)
        ).toMutableList()

        allHistory = mutableListOf()
        listHistory = mutableListOf()

        allHistory = mutableListOf(
        )
        filter = listOf(listStatus[0])
        setTablayoutListnener()

        getOrderHistory()

        return layout
    }


    fun setUpHistoryView()
    {
        filterHistory()
        val adapter = OrderHistoryAdapter(listHistory, activity?.baseContext!!)
        history_rv.adapter = adapter
        history_rv.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
    }

    fun filterHistory()
    {
        listHistory.clear()
        for (history in allHistory)
        {
            if(filter.contains(history.status))
            {
                listHistory.add(history)
            }
        }
    }


    private fun getOrderHistory()
    {
        // get order history from firebase
        val uesrUid = Firebase.auth.currentUser?.uid
        val dbRef = FirebaseDatabase.getInstance().getReference("Orders")
        val query = dbRef.orderByChild("uidUser").equalTo(uesrUid)
        listener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val history = snapshot.getValue(OrderModel::class.java)
                Log.i("HistoryFragment", "History: ${history?.id}")
                if(history != null){
                    allHistory.add(history)
                    setUpHistoryView()
                }

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val history = snapshot.getValue(OrderModel::class.java)
                if(history != null){
                    for (i in 0 until allHistory.size)
                    {
                        if(allHistory[i].id == history.id)
                        {
                            allHistory[i] = (history)
                            break
                        }
                    }
                    if (filter.contains(history.status))
                    {
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
    fun setTablayoutListnener()
    {
        val tabLayout = layout.findViewById<TabLayout>(R.id.tabLayout)
        tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null)
                {
                    val index = tab.position
                    when(index)
                    {
                        0 -> {
                            filter = listOf( listStatus[0])

                        }
                        1 -> {
                            filter = listOf( listStatus[1])

                        }
                        2 -> {
                            filter = listOf( listStatus[2], listStatus[3])
                        }
                        3 -> {
                            filter = listOf( listStatus[4])
                        }
                        4 -> {
                            filter = listOf( listStatus[5])
                        }
                    }
                    setUpHistoryView()

                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })
    }

}