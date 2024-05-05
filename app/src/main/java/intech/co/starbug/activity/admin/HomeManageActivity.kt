package intech.co.starbug.activity.admin

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import intech.co.starbug.activity.admin.feedback.FeedbackManager
import intech.co.starbug.activity.admin.order.OrderManagementActivity
import intech.co.starbug.R
import intech.co.starbug.activity.admin.staff.StaffManagementActivity
import intech.co.starbug.activity.admin.product.ProductManagementActivity
import intech.co.starbug.activity.admin.promotion.PromotionManagementActivity
import intech.co.starbug.activity.admin.statistical.StatisticalActivity
import intech.co.starbug.activity.authentication.LoginActivity

class HomeManageActivity : AppCompatActivity() {

    private lateinit var productLayout: LinearLayout
    private lateinit var staffLayout: LinearLayout
    private lateinit var feedbackLayout: LinearLayout
    private lateinit var orderLayout: LinearLayout
    private lateinit var promotionLayout: LinearLayout
    private lateinit var statisticalLayout: LinearLayout

    private lateinit var logoutBtn: ImageButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_manage)

        // Initialize views
        productLayout = findViewById(R.id.productLayout)
        staffLayout = findViewById(R.id.staffLayout)
        feedbackLayout = findViewById(R.id.feedbackLayout)
        orderLayout = findViewById(R.id.orderLayout)
        promotionLayout = findViewById(R.id.promotionLayout)
        statisticalLayout = findViewById(R.id.staticLayout)

        logoutBtn = findViewById(R.id.logout_btn)

        logoutBtn.setOnClickListener {
            val googleSignInClient =
              GoogleSignIn.getClient(applicationContext, GoogleSignInOptions.DEFAULT_SIGN_IN)
            FirebaseAuth.getInstance().signOut()

            if (googleSignInClient != null) {
                googleSignInClient.revokeAccess()
            }
            // Handle click on logout button
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

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

        statisticalLayout.setOnClickListener {
            // Handle click on statistical layout
            val intent = Intent(this, StatisticalActivity::class.java)
            startActivity(intent)
        }
    }
}
