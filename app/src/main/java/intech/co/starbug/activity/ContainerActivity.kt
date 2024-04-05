package intech.co.starbug.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import intech.co.starbug.R
import intech.co.starbug.fragment.CartFragment
import intech.co.starbug.fragment.HomeFragment

class ContainerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.itemIconTintList = null
        changeFragment(HomeFragment())
        bottomNav.setOnItemSelectedListener { item ->
            val id = item.itemId
            when (id) {
                R.id.action_home -> {
                    changeFragment(HomeFragment())
                }

                R.id.action_cart -> {
                    changeFragment(CartFragment())
                }
            }
            true
        };
    }

    private fun changeFragment(f: Fragment){
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_layout, f)
            commit()
        }
    }
}