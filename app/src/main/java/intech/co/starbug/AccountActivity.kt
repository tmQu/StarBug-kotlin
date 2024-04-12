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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        logout = findViewById(R.id.logout)
        order = findViewById(R.id.order)
        accountSetting = findViewById(R.id.account_setting)

        accountSetting.setOnClickListener {
            startActivity(Intent(this, AccountSettingActivity::class.java))
        }

        logout.setOnClickListener {
            // Logout
        }

        order.setOnClickListener {
            // Orders
        }

    }
}