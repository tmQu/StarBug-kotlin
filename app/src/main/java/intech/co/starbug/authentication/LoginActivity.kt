package intech.co.starbug.authentication

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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputLayout
import intech.co.starbug.HomeActivity
import intech.co.starbug.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
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
    private lateinit var googleBtn: SignInButton;

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
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
        googleBtn = findViewById(R.id.sign_in_button)

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleBtn.setOnClickListener {
            signInGoogle()
        }


        signInBtn.setOnClickListener {
            val emailValue = email.editText?.text.toString()
            val passwordValue = password.editText?.text.toString()

            if (emailValue.isNotEmpty() && passwordValue.isNotEmpty()) {
                auth.signInWithEmailAndPassword(emailValue, passwordValue)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            Log.d("LoginActivity", "Successfully signed in")
                            Toast.makeText(
                                this@LoginActivity,
                                "Successfully signed in",
                                Toast.LENGTH_SHORT
                            ).show()

                            val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                            startActivity(intent)
                        } else {
                            Log.d("LoginActivity", "Failed to sign in: ${task.exception}")
                            Toast.makeText(
                                this@LoginActivity,
                                "An error occurred. Please try again later.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
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

    private fun signInGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleResult(task)
        }
    }

    private fun handleResult(completedTask: Task<GoogleSignInAccount>) {
        val credential = GoogleAuthProvider.getCredential(completedTask.result.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Log.d("LoginActivity", "Successfully signed in")
                    Toast.makeText(
                        this@LoginActivity,
                        "Successfully signed in",
                        Toast.LENGTH_SHORT
                    ).show()

                    val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                    startActivity(intent)
                } else {
                    Log.d("LoginActivity", "Failed to sign in: ${task.exception}")
                    Toast.makeText(
                        this@LoginActivity,
                        "An error occurred. Please try again later.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

}