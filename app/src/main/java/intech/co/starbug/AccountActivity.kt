package intech.co.starbug

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout

class AccountActivity : AppCompatActivity() {

    private lateinit var logout: Button
    private lateinit var order: Button
    private lateinit var accountSetting: Button

    private lateinit var homeBtn: LinearLayout
    private lateinit var cartBtn: LinearLayout
    private lateinit var loveBtn: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        logout = findViewById(R.id.logout)
        order = findViewById(R.id.order)
        accountSetting = findViewById(R.id.account_setting)

        homeBtn = findViewById(R.id.homeBtnLayout)
        cartBtn = findViewById(R.id.cartBtnLayout)
        loveBtn = findViewById(R.id.loveBtnLayout)

        logout.setOnClickListener {
            // Logout
        }

        order.setOnClickListener {
            // Order
        }

        accountSetting.setOnClickListener {
            val intent = Intent(this, AccountSettingActivity::class.java)
            startActivity(intent)
        }

        homeBtn.setOnClickListener {
            // Go to HomeActivity
            startActivity(Intent(this, HomeActivity::class.java))
        }

        cartBtn.setOnClickListener {
            // Go to CartActivity
            startActivity(Intent(this, CartActivity::class.java))
        }

        loveBtn.setOnClickListener {
            // Go to LoveActivity
        }

    }
}