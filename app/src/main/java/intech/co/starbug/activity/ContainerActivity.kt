package intech.co.starbug.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.bottomnavigation.BottomNavigationView
import intech.co.starbug.R
import intech.co.starbug.StarbugApp
import intech.co.starbug.dialog.MenuEditDialog
import intech.co.starbug.fragment.CartFragment
import intech.co.starbug.fragment.HistoryFragment
import intech.co.starbug.fragment.HomeFragment
import intech.co.starbug.model.cart.CartItemModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ContainerActivity : AppCompatActivity(), MenuEditDialog.DialogListener{

    private lateinit var bottomNav: BottomNavigationView
    val homeFragment = HomeFragment()
    private lateinit var prevFragment: Fragment

    private lateinit var badgeCart: BadgeDrawable
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)
        bottomNav = findViewById(R.id.bottom_nav)
        bottomNav.itemIconTintList = null
        supportFragmentManager.beginTransaction().add(R.id.fragment_layout, homeFragment).commit()
        prevFragment = homeFragment


        badgeCart = bottomNav.getOrCreateBadge(R.id.action_cart)
        badgeCart.isVisible = true

        val cartItemDao = (this.application as StarbugApp).dbSQLite.cartItemDAO()
        lifecycleScope.launch {
            try {
                cartItemDao.findAllCartItem().collect {
                    val cartItems = it
                    var total = 0
                    for (cartItem in cartItems){
                        total += cartItem.quantity
                    }
                    badgeCart.number = total
                }
            }
            catch (e: Exception){
                Log.e("ContainerActivity", e.message.toString())
            }


        }

        bottomNav.setOnItemSelectedListener { item ->
            val id = item.itemId
            when (id) {
                R.id.action_home -> {
//                    homeFragment = HomeFragment()
                    val transition = supportFragmentManager.beginTransaction()
                    transition.apply {
                        if(prevFragment != homeFragment)
                        {
                            Log.i("HomeFragment", "Remove")
                            remove(prevFragment)
                        }

                        prevFragment = homeFragment
                        show(homeFragment)
                        commit()
                    }
                }

                R.id.action_cart -> {
                    changeFragment(CartFragment())
                }

                R.id.action_history -> {
                    changeFragment(HistoryFragment())
                }
            }
            true
        };
    }

    private fun changeFragment(f: Fragment){
        val transition = supportFragmentManager.beginTransaction()
        transition.apply {
            add(R.id.fragment_layout, f)
            if(prevFragment != homeFragment)
            {
                Log.i("HomeFragment", "Remove")
                remove(prevFragment)
            }
            prevFragment = f
            hide(homeFragment)
            commit()
        }

    }

    override fun onDialogPositiveClick(dialog: DialogFragment, cartItem: CartItemModel) {
        val cartItemDAO = (application as StarbugApp).dbSQLite.cartItemDAO()
        lifecycleScope.launch(Dispatchers.IO) {
            val iseExist = cartItemDAO.isExistCartItem(cartItem.id,cartItem.productId, cartItem.size, cartItem.temperature, cartItem.amountSugar, cartItem.amountIce)
            if(iseExist == null)
            {
                cartItemDAO.updateCartItem(cartItem)
            }
            else
            {
                iseExist.quantity += cartItem.quantity
                cartItemDAO.updateCartItem(iseExist)
                cartItemDAO.deleteCartItem(cartItem)
            }
        }
    }

    override fun onBackPressed() {
        val fm = supportFragmentManager
        if(bottomNav.selectedItemId == R.id.action_home){
            super.onBackPressed()
        }
        else{
            bottomNav.selectedItemId = R.id.action_home
        }

    }
}