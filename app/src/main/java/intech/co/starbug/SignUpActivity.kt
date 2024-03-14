package intech.co.starbug

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Pair
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputLayout

class SignUpActivity : AppCompatActivity() {
    private lateinit var signInBtn: Button
    private lateinit var signUpBtn: Button
    private lateinit var image: ImageView;
    private lateinit var logoTV: TextView;
    private lateinit var welcomeTV: TextView;
    private lateinit var signInTV : TextView;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        image = findViewById(R.id.logoImage);
        logoTV = findViewById(R.id.branchTV);
        welcomeTV = findViewById(R.id.welcomeTV);
        signInTV = findViewById(R.id.signUpTV);
        signInBtn = findViewById(R.id.signUpBtn)
        signUpBtn = findViewById(R.id.haveAccountBtn)

        signUpBtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            val pairs = arrayOf<Pair<View, String>>(
                Pair(image, "logo_image"),
                Pair(logoTV, "brand_text"),
                Pair(welcomeTV, "welcome_text"),
                Pair(signInTV, "sign_in_up_text"),
                Pair(signInBtn, "go_button"),
                Pair(signUpBtn, "ask_button")
            )

            val options = ActivityOptions.makeSceneTransitionAnimation(this, *pairs)
            startActivity(intent, options.toBundle())
        }
    }
}