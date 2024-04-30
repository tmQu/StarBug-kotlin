package intech.co.starbug.activity.admin

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import intech.co.starbug.activity.admin.feedback.FeedbackManager
import intech.co.starbug.activity.OrderManagementActivity
import intech.co.starbug.R
import intech.co.starbug.activity.StaffManagementActivity
import intech.co.starbug.activity.admin.product.ProductManagementActivity
import intech.co.starbug.activity.admin.promotion.PromotionManagementActivity

class HomeManageActivity : AppCompatActivity() {

    private lateinit var productLayout: LinearLayout
    private lateinit var staffLayout: LinearLayout
    private lateinit var feedbackLayout: LinearLayout
    private lateinit var orderLayout: LinearLayout
    private lateinit var promotionLayout: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_manage)

        // Initialize views
        productLayout = findViewById(R.id.productLayout)
        staffLayout = findViewById(R.id.staffLayout)
        feedbackLayout = findViewById(R.id.feedbackLayout)
        orderLayout = findViewById(R.id.orderLayout)
        promotionLayout = findViewById(R.id.promotionLayout)

        // Set click listeners for each layout
        productLayout.setOnClickListener {
            // Handle click on product layout
            val intent = Intent(this, ProductManagementActivity::class.java)
            startActivity(intent)
        }

        staffLayout.setOnClickListener {
            // Handle click on staff layout
            val intent = Intent(this, StaffManagementActivity::class.java)
            startActivity(intent)
        }

        feedbackLayout.setOnClickListener {
            // Handle click on feedback layout
            val intent = Intent(this, FeedbackManager::class.java)
            startActivity(intent)
        }

        orderLayout.setOnClickListener {
            // Handle click on order layout
            val intent = Intent(this, OrderManagementActivity::class.java)
            startActivity(intent)
        }

        promotionLayout.setOnClickListener {
            val intent = Intent(this, PromotionManagementActivity::class.java)
            startActivity(intent)
        }
    }
}
