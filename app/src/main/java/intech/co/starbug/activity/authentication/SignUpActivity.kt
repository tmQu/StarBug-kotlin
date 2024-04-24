package intech.co.starbug.activity.authentication

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Pair
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import intech.co.starbug.R
import intech.co.starbug.model.UserModel
import java.util.regex.Pattern

class SignUpActivity : AppCompatActivity() {
    private lateinit var signInBtn: Button
    private lateinit var signUpBtn: Button
    private lateinit var image: ImageView
    private lateinit var logoTV: TextView
    private lateinit var welcomeTV: TextView
    private lateinit var signInTV: TextView

    private lateinit var username: TextInputLayout
    private lateinit var email: TextInputLayout
    private lateinit var phoneNo: TextInputLayout
    private lateinit var password: TextInputLayout
    private lateinit var confirmPassword: TextInputLayout

    private lateinit var auth: FirebaseAuth

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

        username = findViewById(R.id.usernameInputLayout)
        email = findViewById(R.id.emailInputLayout)
        phoneNo = findViewById(R.id.phoneNumberInputLayout)
        password = findViewById(R.id.passwordInputLayout)
        confirmPassword = findViewById(R.id.confirmPasswordInputLayout)

        password.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(editable: Editable?) {
                validatePass(password)
            }
        })

        signInBtn.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            val options = ActivityOptions.makeSceneTransitionAnimation(
                this,
                Pair.create(image, "logoImage"),
                Pair.create(logoTV, "branchTV"),
                Pair.create(welcomeTV, "welcomeTV"),
                Pair.create(signInTV, "signUpTV"),
                Pair.create(signInBtn, "haveAccountBtn")
            )
            startActivity(intent, options.toBundle())
        }

        signUpBtn.setOnClickListener {
            val usernameValue = username.editText?.text.toString()
            val emailValue = email.editText?.text.toString()
            val passwordValue = password.editText?.text.toString()
            val phoneNoValue = phoneNo.editText?.text.toString()
            val role = "Customer"

            // Check if password and confirm password match
            val confirmValue = confirmPassword.editText?.text.toString()
            if (passwordValue != confirmValue) {
                confirmPassword.error = "Password does not match"
                return@setOnClickListener
            }

            // Check if all fields are filled
            if (usernameValue.isEmpty() || emailValue.isEmpty() || passwordValue.isEmpty() || phoneNoValue.isEmpty()) {
                // Show error message indicating fields are empty
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Exit the function early
            }

            val database = FirebaseDatabase.getInstance()
            val myRef = database.getReference("User")

            authenSignUp(emailValue, passwordValue, myRef, role, usernameValue, phoneNoValue)
        }
    }

    private fun validatePass(password: TextInputLayout) {
        // check for pattern
        val uppercase: Pattern = Pattern.compile("[A-Z]")
        val lowercase: Pattern = Pattern.compile("[a-z]")
        val digit: Pattern = Pattern.compile("[0-9]")

        val value = password.editText?.text.toString()

        // if lowercase character is not present
        if (!lowercase.matcher(value).find() || !uppercase.matcher(value).find() || !digit.matcher(value).find() || value.length < 8) {
            password.error = "Password must contain uppercase, lowercase character, digit and be at least 8 characters long"
        } else {
            password.error = null
        }
    }

    private fun authenSignUp(emailValue: String, passwordValue: String, myRef: DatabaseReference, role: String, usernameValue: String, phoneNoValue: String) {
        auth = FirebaseAuth.getInstance()
        auth.createUserWithEmailAndPassword(emailValue, passwordValue)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Log.d("SignUpActivity", "Successfully signed up")
                    Toast.makeText(this@SignUpActivity, "Successfully signed up. Please check your email for verification.", Toast.LENGTH_SHORT).show()

                    // Send email verification
                    user?.sendEmailVerification()?.addOnCompleteListener { emailTask ->
                        if (emailTask.isSuccessful) {
                            Log.d("SignUpActivity", "Email verification sent")
                            // After verification, navigate to login screen
                            val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
                            startActivity(intent)
                        } else {
                            Log.e("SignUpActivity", "Failed to send email verification", emailTask.exception)
                        }
                    }

                    // Save user data to database
                    saveUserDataToDatabase(user, myRef, role, usernameValue, emailValue, phoneNoValue)
                } else {
                    if (task.exception?.message == "The email address is already in use by another account.") {
                        email.error = "This email has already been registered. Please use a different email."
                    }
                    Log.d("SignUpActivity", "Failed to sign up: ${task.exception?.message}")
                    Toast.makeText(this@SignUpActivity, "Failed to sign up: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserDataToDatabase(user: FirebaseUser?, myRef: DatabaseReference, role: String, username: String, email: String, phoneNo: String) {
        user?.uid?.let { userId ->
            val userData = UserModel(
                uid = userId,
                email = email,
                name = username,
                password = "", // Optional: add a password if needed
                phoneNumber = phoneNo,
                role = role
            )

            myRef.child(userId).setValue(userData)
                .addOnSuccessListener {
                    Log.d("SignUpActivity", "User added to database successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("SignUpActivity", "Error adding user to database", e)
                }
        }
    }
}
