package intech.co.starbug.authentication

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Pair
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import intech.co.starbug.HomeActivity
import intech.co.starbug.R

class LoginActivity : AppCompatActivity() {
    private lateinit var username: TextInputLayout
    private lateinit var password: TextInputLayout
    private lateinit var signInBtn: Button
    private lateinit var signUpBtn: Button
    private lateinit var image: ImageView;
    private lateinit var logoTV: TextView;
    private lateinit var welcomeTV: TextView;
    private lateinit var signInTV : TextView
    private lateinit var forgetBtn: Button;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_login)

        image = findViewById(R.id.logoImage);
        logoTV = findViewById(R.id.branchTV);
        welcomeTV = findViewById(R.id.welcomeTV);
        signInTV = findViewById(R.id.signInTV);
        username = findViewById(R.id.usernameInputLayout)
        password = findViewById(R.id.passwordInputLayout)
        signInBtn = findViewById(R.id.signInBtn)
        signUpBtn = findViewById(R.id.haveUserBtn)
        forgetBtn = findViewById(R.id.forgetPasswordBtn)

        signInBtn.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        signUpBtn.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
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

        forgetBtn.setOnClickListener {
            val intent = Intent(this, ForgetPasswordActivity::class.java)
            val pairs = arrayOf< Pair<View, String>>(
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