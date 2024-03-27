package intech.co.starbug.activity.authentication

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Pair
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import intech.co.starbug.HomeActivity
import intech.co.starbug.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class LoginActivity : AppCompatActivity() {
    private lateinit var email: TextInputLayout
    private lateinit var password: TextInputLayout
    private lateinit var signInBtn: Button
    private lateinit var signUpBtn: Button
    private lateinit var image: ImageView;
    private lateinit var logoTV: TextView;
    private lateinit var welcomeTV: TextView;
    private lateinit var signInTV: TextView
    private lateinit var forgetBtn: Button;

    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_login)

        image = findViewById(R.id.logoImage);
        logoTV = findViewById(R.id.branchTV);
        welcomeTV = findViewById(R.id.welcomeTV);
        signInTV = findViewById(R.id.signInTV);
        email = findViewById(R.id.emailInputLayout)
        password = findViewById(R.id.passwordInputLayout)
        signInBtn = findViewById(R.id.signInBtn)
        signUpBtn = findViewById(R.id.haveUserBtn)
        forgetBtn = findViewById(R.id.forgetPasswordBtn)

        auth = FirebaseAuth.getInstance()


        signInBtn.setOnClickListener {
            val emailValue = email.editText?.text.toString()
            val passwordValue = password.editText?.text.toString()

            if (emailValue.isNotEmpty() && passwordValue.isNotEmpty()) {
                signIn(email, password)
            } else {
                if (emailValue.isEmpty()) {
                    email.error = "Please enter email"
                } else if (passwordValue.isEmpty()) {
                    password.error = "Please enter password"
                }
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
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

    private fun signIn(email: TextInputLayout, password: TextInputLayout) {
        val emailValue = email.editText?.text.toString()
        val passwordValue = password.editText?.text.toString()

        val database = Firebase.database
        val sanitizedEmail = emailValue.replace(".", "_")
        val emailRef = database.getReference("User").child(sanitizedEmail)

        Log.d("LoginActivity", emailRef.toString())

        emailRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d("LoginActivity", "DataSnapshot: ${dataSnapshot.exists()}")
                if (dataSnapshot.exists()) {

                    val passwordFromDB = dataSnapshot.child("Password").getValue(String::class.java)

                    if (passwordFromDB == passwordValue) {

                        Log.d("LoginActivity", "Successfully signed in")
                        Toast.makeText(
                            this@LoginActivity,
                            "Successfully signed in",
                            Toast.LENGTH_SHORT
                        ).show()

                        val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                        startActivity(intent)

                    } else {
                        Log.d("LoginActivity", "Incorrect password")
                        password.error = "Incorrect password"
                        Toast.makeText(this@LoginActivity, "Incorrect password", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Log.d("LoginActivity", "Email does not exist")
                    email.error = "This email has not been registered. Please sign up."
                    Toast.makeText(
                        this@LoginActivity,
                        "This email has not been registered. Please sign up.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("LoginActivity", "Failed to sign in: ${databaseError.message}")
                Toast.makeText(
                    this@LoginActivity,
                    "An error occurred. Please try again later.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }


}