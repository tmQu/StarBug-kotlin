package intech.co.starbug

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import intech.co.starbug.activity.product.DetailProductActivity

class AccoutActivity : AppCompatActivity() {

    private lateinit var logout: Button
    private lateinit var order: Button
    private lateinit var accountSetting: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accout)

        logout = findViewById(R.id.logout)
        order = findViewById(R.id.order)
        accountSetting = findViewById(R.id.account_setting)

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
    }
}