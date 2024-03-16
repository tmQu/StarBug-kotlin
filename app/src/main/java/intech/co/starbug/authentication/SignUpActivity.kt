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
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import intech.co.starbug.R
import java.util.regex.Pattern

class SignUpActivity : AppCompatActivity() {
    private lateinit var signInBtn: Button
    private lateinit var signUpBtn: Button
    private lateinit var image: ImageView
    private lateinit var logoTV: TextView
    private lateinit var welcomeTV: TextView
    private lateinit var signInTV: TextView

    private lateinit var fullname: TextInputLayout
    private lateinit var username: TextInputLayout
    private lateinit var email: TextInputLayout
    private lateinit var phoneNo: TextInputLayout
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

        fullname = findViewById(R.id.fullName)
        username = findViewById(R.id.usernameInputLayout)
        email = findViewById(R.id.emailInputLayout)
        phoneNo = findViewById(R.id.phoneNumberInputLayout)
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

            val fullNameValue = fullname.editText?.text.toString()
            val usernameValue = username.editText?.text.toString()
            val emailValue = email.editText?.text.toString()
            val passwordValue = password.editText?.text.toString()
            val phoneNoValue = phoneNo.editText?.text.toString()
            val role = "Customer"

            // Check if password and confirm password match
            val confirmValue = confirmPassword.editText?.text.toString()
            if (passwordValue != confirmValue) {
                confirmPassword.error = "Password does not match"
            }

            // Check if all fields are filled
            if (fullNameValue.isEmpty() || usernameValue.isEmpty() || emailValue.isEmpty() || passwordValue.isEmpty() || phoneNoValue.isEmpty()) {
                // Show error message indicating fields are empty
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Exit the function early
            }

            // Create a new user in the database
            val database = Firebase.database
            val myRef = database.getReference("User")

            val sanitizedEmail = emailValue.replace(".", "_")

            val emailRef = database.getReference("User").child(sanitizedEmail)
            emailRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Log.d("SignUpActivity", "Email already exists")
                        email.error = "This email has already been registered. Please use a different email."
                        Toast.makeText(
                            this@SignUpActivity,
                            "This email has already been registered. Please use a different email.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        // Email does not exist, proceed with creating the account
                        val userData = hashMapOf(
                            "Role" to role,
                            "Name" to fullNameValue,
                            "Username" to usernameValue,
                            "Email" to emailValue,
                            "Password" to passwordValue,
                            "Phone" to phoneNoValue
                        )

                        myRef.child(sanitizedEmail).setValue(userData).addOnSuccessListener {
                            Log.d("SignUpActivity", "Data saved successfully")
                            Toast.makeText(
                                this@SignUpActivity,
                                "Create account successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        }.addOnFailureListener {
                            Log.d("SignUpActivity", "Data not saved")
                            Toast.makeText(
                                this@SignUpActivity,
                                "Create account failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d("SignUpActivity", "Failed to check email existence: ${databaseError.message}")
                    Toast.makeText(
                        this@SignUpActivity,
                        "An error occurred. Please try again later.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })

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
