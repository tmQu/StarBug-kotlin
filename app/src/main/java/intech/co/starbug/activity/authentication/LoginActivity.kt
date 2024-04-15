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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import intech.co.starbug.FeedbackManager
import intech.co.starbug.HomeActivity
import intech.co.starbug.HomeManageActivity
import intech.co.starbug.R
import intech.co.starbug.activity.ContainerActivity
import intech.co.starbug.model.UserModel

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
        window.setFlags(
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
                        Log.d("LoginActivity", "EmailValue $emailValue")
                        Log.d("LoginActivity", "PasswordValue $passwordValue")
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            if (user != null) {
                                val userId = user.uid
                                val database = FirebaseDatabase.getInstance()
                                val myRef = database.getReference("User").child(userId)

                                myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                                        val userData = dataSnapshot.getValue(UserModel::class.java)
                                        if (userData != null) {
                                            val userRole = userData.role
                                            if (userRole == "Admin") {
                                                Toast.makeText(
                                                    this@LoginActivity,
                                                    "User is Admin",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                val intent = Intent(this@LoginActivity, HomeManageActivity::class.java)
                                                startActivity(intent)
                                            } else {
                                                Toast.makeText(
                                                    this@LoginActivity,
                                                    "User is not Admin",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                val intent = Intent(this@LoginActivity, ContainerActivity::class.java)
                                                startActivity(intent)
                                            }
                                        }
                                    }

                                    override fun onCancelled(databaseError: DatabaseError) {
                                        Log.e("LoginActivity", "Database error: ${databaseError.message}")
                                    }
                                })
                            }
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
                    if (user != null) {
                        addUserToDatabase(user)

                        val userId = user.uid
                        val database = FirebaseDatabase.getInstance()
                        val myRef = database.getReference("User").child(userId)

                        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                val userData = dataSnapshot.getValue(UserModel::class.java)
                                Log.i("LoginActivity", "User data: $userData.email")
                                if (userData != null) {
                                    val userRole = userData.role
                                    if (userRole == "Admin") {
                                        Toast.makeText(
                                            this@LoginActivity,
                                            "User is Admin",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        val intent = Intent(this@LoginActivity, HomeManageActivity::class.java)
                                        Log.i("LoginActivity", "intent: $intent")
                                        startActivity(intent)
                                    } else {
                                        Toast.makeText(
                                            this@LoginActivity,
                                            "User is not Admin",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        val intent = Intent(this@LoginActivity, ContainerActivity::class.java)
                                        startActivity(intent)
                                    }
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                Log.e("LoginActivity", "Database error: ${databaseError.message}")
                            }
                        })
                    }
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

    private fun addUserToDatabase(user: FirebaseUser?) {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("User")

        user?.uid?.let { userId ->
            myRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        val userData = UserModel(
                            uid = userId,
                            email = user.email ?: "",
                            name = user.displayName ?: "",
                            password = "", // Optional: add a password if needed
                            phoneNumber = "", // Optional: add a phone number if needed
                            role = "Customer"
                        )
                        myRef.child(userId).setValue(userData)
                            .addOnSuccessListener {
                                Log.d("LoginActivity", "User added to database successfully")
                            }
                            .addOnFailureListener { e ->
                                Log.e("LoginActivity", "Error adding user to database", e)
                            }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("LoginActivity", "Database error: ${databaseError.message}")
                }
            })
        }
    }
}
