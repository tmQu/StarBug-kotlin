package intech.co.starbug

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Pair
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import java.util.regex.Pattern

class SignUpActivity : AppCompatActivity() {
    private lateinit var signInBtn: Button
    private lateinit var signUpBtn: Button
    private lateinit var image: ImageView
    private lateinit var logoTV: TextView
    private lateinit var welcomeTV: TextView
    private lateinit var signInTV: TextView

    private lateinit var password: TextInputLayout
    private lateinit var confirmPassword: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        image = findViewById(R.id.logoImage)
        logoTV = findViewById(R.id.branchTV)
        welcomeTV = findViewById(R.id.welcomeTV)
        signInTV = findViewById(R.id.signUpTV)
        signUpBtn = findViewById(R.id.signUpBtn)
        signInBtn = findViewById(R.id.haveAccountBtn)

        password = findViewById(R.id.passwordInputLayout)
        confirmPassword = findViewById(R.id.confirmPasswordInputLayout)

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

        signUpBtn.setOnClickListener {
            val valuePassword = password.editText?.text.toString()
            val valueConfirm = confirmPassword.editText?.text.toString()
            if (valuePassword != valueConfirm) {
                confirmPassword.error = "Password does not match"
            }
        }

        signInBtn.setOnClickListener {
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

    private fun validatePass(password: TextInputLayout) {
        // check for pattern
        val uppercase: Pattern = Pattern.compile("[A-Z]")
        val lowercase: Pattern = Pattern.compile("[a-z]")
        val digit: Pattern = Pattern.compile("[0-9]")

        val value = password.editText?.text.toString()

        // if lowercase character is not present
        if (!lowercase.matcher(value).find() || !uppercase.matcher(value).find() || !digit.matcher(
                value
            ).find() || value.length < 8
        ) {
            password.error = "Password must be present uppercase, lowercase character, digit and at least 8 characters"
        } else {
            password.error = null
        }
    }
}
