package intech.co.starbug.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import intech.co.starbug.HistoryDetailActivity
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
            OrderModel(listCart, listStatus[0]),
            OrderModel(listCart, listStatus[1]),
            OrderModel(listCart, listStatus[2]),
            OrderModel(listCart, listStatus[3]),
            OrderModel(listCart, listStatus[4]),
            OrderModel(listCart, listStatus[5])
        )
        listHistory += filterHistory(listStatus[0])
        setUpHistoryView()

        setTablayoutListnener()

        return layout
    }


    fun setUpHistoryView()
    {
        val adapter = OrderHistoryAdapter(listHistory, activity?.baseContext!!)
        history_rv.adapter = adapter
        history_rv.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        adapter.onItemClick = { view, position ->
            val intent = Intent(activity, HistoryDetailActivity::class.java)
            startActivity(intent)
        }
    }

    fun filterHistory(status: String): List<OrderModel>
    {
        // filter according status
        return allHistory.filter{it.status == status}
    }

    fun setTablayoutListnener()
    {
        val tabLayout = layout.findViewById<TabLayout>(R.id.tabLayout)
        tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null)
                {
                    val index = tab.position
                    listHistory.clear()
                    when(index)
                    {
                        0 -> {
                            listHistory += filterHistory(listStatus[0])
                        }
                        1 -> {
                            listHistory += filterHistory(listStatus[1])

                        }
                        2 -> {
                            listHistory += filterHistory(listStatus[2])
                            listHistory += filterHistory(listStatus[3])
                        }
                        3 -> {
                            listHistory += filterHistory(listStatus[4])
                        }
                        4 -> {
                            listHistory += filterHistory(listStatus[5])
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