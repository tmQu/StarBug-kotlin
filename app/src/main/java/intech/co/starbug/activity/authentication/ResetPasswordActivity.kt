package intech.co.starbug.authentication

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Pair
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import intech.co.starbug.R
import java.util.regex.Pattern


class ResetPasswordActivity : AppCompatActivity() {
    private lateinit var image: ImageView
    private lateinit var logoTV: TextView
    private lateinit var resetTV: TextView
    private lateinit var descriptionTV: TextView
    private lateinit var password: TextInputLayout
    private lateinit var confirmPassword: TextInputLayout
    private lateinit var resetBtn: Button
    private lateinit var backBtn: Button
    private lateinit var mAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reset_password)
        image = findViewById(R.id.logoImage)
        logoTV = findViewById(R.id.branchTV)
        resetTV = findViewById(R.id.ResetPasswordTV)
        descriptionTV = findViewById(R.id.descriptionTV)
        password = findViewById(R.id.passwordInputLayout)
        confirmPassword = findViewById(R.id.confirmPasswordInputLayout)
        resetBtn = findViewById(R.id.resetBtn)
        backBtn = findViewById(R.id.haveUserBtn)

        mAuth = FirebaseAuth.getInstance()

        password.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                charSequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
            }

            override fun afterTextChanged(editable: Editable?) {
                validatePass(password)
            }
        })

        backBtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            val pairs = arrayOf<Pair<View, String>>(
                Pair(image, "logo_image"),
                Pair(logoTV, "brand_text"),
                Pair(resetTV, "reset_text"),
                Pair(descriptionTV, "description_text"),
                Pair(resetBtn, "reset_button"),
                Pair(backBtn, "ask_button")
            )

            val options = ActivityOptions.makeSceneTransitionAnimation(this, *pairs)
            startActivity(intent, options.toBundle())
        }

        resetBtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            val pairs = arrayOf<Pair<View, String>>(
                Pair(image, "logo_image"),
                Pair(logoTV, "brand_text"),
                Pair(resetTV, "reset_text"),
                Pair(descriptionTV, "description_text"),
                Pair(resetBtn, "reset_button"),
                Pair(backBtn, "ask_button")
            )




            val options = ActivityOptions.makeSceneTransitionAnimation(this, *pairs)
            startActivity(intent, options.toBundle())
        }
    }

    private fun validatePass(password: TextInputLayout) {
        // check for pattern
        val uppercase: Pattern = Pattern.compile("[A-Z]")
        val lowercase: Pattern = Pattern.compile("[a-z]")
        val digit: Pattern = Pattern.compile("[0-9]")

        val value = password.editText?.text.toString()

        // if lowercase character is not present
        if (!lowercase.matcher(value).find() || !uppercase.matcher(value)
                .find() || !digit.matcher(
                value
            ).find() || value.length < 8
        ) {
            password.error =
                "Password must be present uppercase, lowercase character, digit and at least 8 characters"
        } else {
            password.error = null
        }
    }


}