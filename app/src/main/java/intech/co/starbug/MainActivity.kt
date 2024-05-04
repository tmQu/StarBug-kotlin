package intech.co.starbug
import android.app.ActivityOptions
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.util.Pair
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import intech.co.starbug.activity.ContainerActivity
import intech.co.starbug.activity.GetAddressActivity
import intech.co.starbug.activity.CheckoutActivity
import intech.co.starbug.activity.Feedback
import intech.co.starbug.activity.admin.HomeManageActivity
import intech.co.starbug.activity.admin.feedback.FeedbackManager
import intech.co.starbug.activity.admin.product.ProductManagementActivity

import intech.co.starbug.activity.authentication.LoginActivity
import intech.co.starbug.helper.SharedPreferencesHelper
import intech.co.starbug.model.UserModel

class MainActivity : AppCompatActivity() {

    private lateinit var topAnim: Animation;
    private lateinit var bottomAnim: Animation;
    private lateinit var image: ImageView;
    private lateinit var logoTV: TextView;
    private lateinit var sloganTV: TextView;
    private lateinit var companyTV: TextView;
    private var wait = false;

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // hide the system bar
        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView)
        // Configure the behavior of the hidden system bars.
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);

        image = findViewById(R.id.imageView);
        logoTV = findViewById(R.id.textViewBrand);
        sloganTV = findViewById(R.id.textViewSlogan);
        companyTV = findViewById(R.id.textViewCompany);

        image.setAnimation(topAnim);
        logoTV.setAnimation(bottomAnim);
        sloganTV.setAnimation(bottomAnim);
        companyTV.setAnimation(bottomAnim);

        FirebaseApp.initializeApp(this)
        Handler().postDelayed({
            if(checkAuth())
            {
                val role = SharedPreferencesHelper(this).getRole()
                Log.i("MainActivity", "User authenticated $role")
                val intent = when(role)
                {
                    "Admin" -> Intent(this, HomeManageActivity::class.java)
                    "Customer" -> Intent(this, ContainerActivity::class.java)
                    else -> Intent(this, LoginActivity::class.java)
                }
                val pairs = arrayOf<Pair<View, String>>(
                    Pair(image, "logo_image"),
                    Pair(logoTV, "brand_text"),
                )
                if(wait) {
                    val options = ActivityOptions.makeSceneTransitionAnimation(this@MainActivity, *pairs)
                    startActivity(intent, options.toBundle())
                    finish()

                }
                else
                    wait = true


            }
            else {
                val pairs = arrayOf<Pair<View, String>>(
                    Pair(image, "logo_image"),
                    Pair(logoTV, "brand_text"),
                )
                Log.i("MainActivity", "User not authenticated")

                val intent = Intent(this, LoginActivity::class.java)
                val options = ActivityOptions.makeSceneTransitionAnimation(this@MainActivity, *pairs)
                startActivity(intent, options.toBundle())
                finish()

            }

        }, 3000)
    }

    private fun checkAuth(): Boolean
    {

        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid
        if(userId != null)
        {
            val dbRef = FirebaseDatabase.getInstance().getReference("User").child(userId)
            dbRef.addListenerForSingleValueEvent(/* listener = */ object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val userData = dataSnapshot.getValue(UserModel::class.java)
                    if (userData != null) {
                        val userRole = userData.role
                        val sharedPrefManager =
                            SharedPreferencesHelper(this@MainActivity)
                        sharedPrefManager.saveUser(userData)
                        if(wait)
                        {
                            val intent = when(userRole)
                            {
                                "Admin" -> Intent(this@MainActivity, HomeManageActivity::class.java)
                                "Customer" -> Intent(this@MainActivity, ContainerActivity::class.java)
                                else -> Intent(this@MainActivity, LoginActivity::class.java)
                            }

                            startActivity(intent)
                            finish()

                        }
                        else {
                            wait = true
                        }
                    }else {
                        val pairs = arrayOf<Pair<View, String>>(
                            Pair(image, "logo_image"),
                            Pair(logoTV, "brand_text"),
                        )
                        Log.i("MainActivity", "User not authenticated")

                        val intent = Intent(this@MainActivity, LoginActivity::class.java)
                        val options = ActivityOptions.makeSceneTransitionAnimation(this@MainActivity, *pairs)
                        startActivity(intent, options.toBundle())
                        finish()

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(
                        "MainActivity",
                        "Database error: ${error.message}"
                    )
                }
            })
        }
        else {
            return false
        }
        return true
    }
}
