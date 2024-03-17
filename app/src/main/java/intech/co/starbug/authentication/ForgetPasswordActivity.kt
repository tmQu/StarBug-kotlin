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
import intech.co.starbug.R

class ForgetPasswordActivity : AppCompatActivity() {
    private lateinit var email: TextInputLayout;
    private lateinit var forgetBtn: Button;
    private lateinit var haveUserBtn: Button;
    private lateinit var image: ImageView;
    private lateinit var logoTV: TextView;
    private lateinit var verifyTV: TextView;
    private lateinit var forgetTV : TextView;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_forget_password)

        image = findViewById(R.id.logoImage);
        logoTV = findViewById(R.id.branchTV);
        verifyTV = findViewById(R.id.verifyTV);
        email = findViewById(R.id.emailInputLayout);
        forgetBtn = findViewById(R.id.verifyBtn);
        forgetTV = findViewById(R.id.forgetPasswordTV);
        haveUserBtn = findViewById(R.id.haveUserBtn);

        haveUserBtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            val pairs = arrayOf<Pair<View, String>>(
                Pair(image, "logo_image"),
                Pair(logoTV, "brand_text"),
                Pair(forgetTV, "welcome_text"),
                Pair(verifyTV, "sign_in_up_text"),
                Pair(forgetBtn, "go_button"),
                Pair(haveUserBtn, "ask_button")
            )

            val options = ActivityOptions.makeSceneTransitionAnimation(this, *pairs)
            startActivity(intent, options.toBundle())
        }

        forgetBtn.setOnClickListener {
            val intent = Intent(this, ResetPasswordActivity::class.java)
            val pairs = arrayOf<Pair<View, String>>(
                Pair(image, "logo_image"),
                Pair(logoTV, "brand_text"),
                Pair(verifyTV, "reset_text"),
                Pair(email, "email_input"),
                Pair(forgetBtn, "reset_button"),
                Pair(haveUserBtn, "have_user_button")
            )

            val options = ActivityOptions.makeSceneTransitionAnimation(this, *pairs)
            startActivity(intent, options.toBundle())
        }
    }
}