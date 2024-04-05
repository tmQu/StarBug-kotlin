package intech.co.starbug.fragment

import CartAdapter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import intech.co.starbug.R
import intech.co.starbug.StarbugApp
import intech.co.starbug.dialog.MenuEditDialog
import intech.co.starbug.model.ProductModel
import intech.co.starbug.model.cart.CartItemDAO
import intech.co.starbug.model.cart.CartItemModel
import intech.co.starbug.model.cart.DetailCartItem
import intech.co.starbug.utils.Utils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CartFragment : Fragment(), CartAdapter.ButtonClickListener {

    private lateinit var mLayout: View
    private var listDetailCart = mutableListOf<DetailCartItem>()
    private lateinit var cartItemDao: CartItemDAO
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mLayout =  inflater.inflate(R.layout.fragment_cart, container, false)
        cartItemDao = (activity?.application as StarbugApp).dbSQLite.cartItemDAO()
        getCartItem(cartItemDao)
        return mLayout
    }

    private fun getCartItem(cartItemDAO: CartItemDAO)
    {
        lifecycleScope.launch {
            cartItemDAO.findAllCartItem().collect{
                val listCart = it
                listDetailCart.clear()
                if(listCart.size == 0)
                {
                    setUpRv(listDetailCart)
                }
                else {
                    queryProduct(listCart)
                }
            }

        }
    }

    private fun setUpRv(listDetailCart: MutableList<DetailCartItem>) {
        val recyclerView = mLayout.findViewById<RecyclerView>(R.id.recyclerProductsView)
        val cartAdapter = CartAdapter(listDetailCart, this)
        recyclerView.adapter = cartAdapter
        recyclerView.layoutManager = LinearLayoutManager(activity)
        val totalPriceView = mLayout.findViewById<TextView>(R.id.total_price)
        var totalPrice = 0
        for (item in listDetailCart)
        {
            totalPrice += item.quantity * item.getProductPrice()
        }
        totalPriceView.text = Utils.formatMoney(totalPrice)
    }


    private fun queryProduct(listCartItem: List<CartItemModel>) {
        val productRef = FirebaseDatabase.getInstance().getReference("Products")

        productRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val product = snapshot.getValue(ProductModel::class.java)
                for (item in listCartItem)
                {
                    if (product != null) {
                        if(item.productId == product.id) {
                            listDetailCart.add(DetailCartItem(item, product))
                            setUpRv(listDetailCart)
                            Log.i("CartFragment", listDetailCart[0].product?.img?.get(0) ?:"no img" )
                            break
                        }
                    }
                }

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val product = snapshot.getValue(ProductModel::class.java)
                for (item in listDetailCart)
                {
                    if (product != null) {
                        if(item.productId == product.id) {
                            item.product = product
                            setUpRv(listDetailCart)
                        }
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    override fun onItemClick(position: Int) {
        val menuDialog = MenuEditDialog()
        val args = Bundle()
        args.putSerializable("option_menu", listDetailCart[position])
        menuDialog.arguments = args
        activity?.let { menuDialog.show(it.supportFragmentManager, "menu selection") }
    }

    override fun onDeleteClick(position: Int) {
        lifecycleScope.launch {
            Log.i("test", listDetailCart.size.toString())

            cartItemDao.deleteCartItem(listDetailCart[position].cartItemModel)
        }
    }

    override fun onAddQuantityClick(position: Int) {
        lifecycleScope.launch {
            val cartItem = listDetailCart[position].cartItemModel
            cartItem.quantity += 1
            cartItemDao.updateCartItem(cartItem)
        }
    }

    override fun onDecreaseQuantityClick(position: Int) {
        lifecycleScope.launch {
            val cartItem = listDetailCart[position].cartItemModel
            cartItem.quantity -= 1

            if(cartItem.quantity == 0)
            {
                cartItemDao.deleteCartItem(cartItem)
            }
            else {
                cartItemDao.updateCartItem(cartItem)

            }
        }
    }
}