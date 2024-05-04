package intech.co.starbug.fragment

import CartAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import intech.co.starbug.R
import intech.co.starbug.StarbugApp
import intech.co.starbug.activity.CheckoutActivity
import intech.co.starbug.dialog.MenuEditDialog
import intech.co.starbug.model.ProductModel
import intech.co.starbug.model.cart.CartItemDAO
import intech.co.starbug.model.cart.CartItemModel
import intech.co.starbug.model.cart.DetailCartItem
import intech.co.starbug.utils.Utils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CartFragment : Fragment(), CartAdapter.ButtonClickListener {

    private lateinit var mLayout: View
    private var listDetailCart = mutableListOf<DetailCartItem>()
    private lateinit var cartItemDao: CartItemDAO
    private lateinit var noteView: TextInputLayout
    private lateinit var productRef: DatabaseReference
    private var job: Job? = null

    private lateinit var buyBtn: Button

    private lateinit var listener: ValueEventListener

    override fun onDestroy() {
        super.onDestroy()
        val dbRef = FirebaseDatabase.getInstance().getReference("Products")
        dbRef.removeEventListener(listener)

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mLayout =  inflater.inflate(R.layout.fragment_cart, container, false)
        cartItemDao = (activity?.application as StarbugApp).dbSQLite.cartItemDAO()
        productRef = FirebaseDatabase.getInstance().getReference("Products")
        queryProduct()

        noteView = mLayout.findViewById(R.id.note)
        buyBtn = mLayout.findViewById<Button>(R.id.btn_buy)
        buyBtn.setOnClickListener {
            if(listDetailCart.isEmpty())
            {
                Toast.makeText(activity, "Your cart is empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(activity, CheckoutActivity::class.java)
            intent.putExtra("listDetailOrder", ArrayList(listDetailCart))
            intent.putExtra("note", noteView.editText?.text.toString())
            startActivity(intent)
        }
        return mLayout
    }

    private fun getCartItem(cartItemDAO: CartItemDAO, listPrduct: MutableList<ProductModel>)
    {
        job?.cancel()
        job = lifecycleScope.launch {
            try {
                cartItemDAO.findAllCartItem().cancellable().collect{
                    val listCart = it
                    listDetailCart.clear()
                    Log.i("CartFragment", "${listCart.size}")
                    if(listCart.isEmpty())
                    {
                        mLayout.findViewById<LinearLayout>(R.id.empty_cart).visibility = View.VISIBLE
                        mLayout.findViewById<RecyclerView>(R.id.recyclerProductsView).visibility = View.GONE


                        setUpRv(listDetailCart)


                    }
                    else {
                        mLayout.findViewById<LinearLayout>(R.id.empty_cart).visibility = View.GONE
                        mLayout.findViewById<RecyclerView>(R.id.recyclerProductsView).visibility = View.VISIBLE
                        for (cartItem in listCart) {
                            val product = listPrduct.find { it.id == cartItem.productId }
                            if (product != null) {
                                listDetailCart.add(DetailCartItem(cartItem, product))
                            }
                            else {
//                                Toast.makeText(activity, "Product not found", Toast.LENGTH_SHORT).show()
                                cartItemDao.deleteCartItem(cartItem)
                            }
                        }
                        setUpRv(listDetailCart)
                    }
                }
            }
            finally {
                Log.i("CartFragment", "cancel job")

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


    private fun queryProduct() {
        val listProduct = mutableListOf<ProductModel>()
        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listProduct.clear()

                for (productSnapshot in snapshot.children) {
                    val product = productSnapshot.getValue(ProductModel::class.java)


                    if (product != null) {
                        listProduct.add(product)
                    }
                }
                getCartItem(cartItemDao, listProduct)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("CartFragment", "loadPost:onCancelled", error.toException())
            }

        }
        productRef.addValueEventListener(listener)
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