package intech.co.starbug.activity.admin.staff

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import intech.co.starbug.R
import intech.co.starbug.activity.authentication.LoginActivity
import java.util.regex.Pattern

class AddStaffManagementActivity : AppCompatActivity() {

    val roleOptions = arrayOf("Staff")

    private lateinit var fullName: TextInputLayout
    private lateinit var userName: TextInputLayout
    private lateinit var email: TextInputLayout
    private lateinit var phoneNo: TextInputLayout
    private lateinit var password: TextInputLayout
    private lateinit var role: TextInputEditText

    private lateinit var addBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_staff_management)
        fullName = findViewById(R.id.fullName)
        userName = findViewById(R.id.usernameInputLayout)
        email = findViewById(R.id.emailInputLayout)
        phoneNo = findViewById(R.id.phoneNumberInputLayout)
        password = findViewById(R.id.passwordInputLayout)
        role = findViewById(R.id.roleInputLayout)

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

        addBtn = findViewById(R.id.addToDB)

        addBtn.setOnClickListener {
            if (fullName.editText?.text.toString().isEmpty() ||
                userName.editText?.text.toString().isEmpty() ||
                email.editText?.text.toString().isEmpty() ||
                password.editText?.text.toString().isEmpty() ||
                phoneNo.editText?.text.toString().isEmpty() ||
                role.text.toString().isEmpty()) {
                // Show error message indicating fields are empty
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Exit the function early
            }

            showConfirmationDialog(
                title = "Add Account",
                message = "Are you sure you want to add this account?",
                onConfirm = { addData() }
            )
        }

        role.setOnClickListener() {
            showRoleDialog()
        }
    }


    private fun addData() {
        val resultIntent = intent.apply {
            putExtra("FullName", fullName.editText?.text.toString())
            putExtra("Username", userName.editText?.text.toString())
            putExtra("Email", email.editText?.text.toString())
            putExtra("Phone", phoneNo.editText?.text.toString())
            putExtra("Password", password.editText?.text.toString())
            putExtra("Role", role.text.toString())
        }
        authenSignUp(email.editText?.text.toString(), password.editText?.text.toString())
        setResult(RESULT_OK, resultIntent)
        Toast.makeText(this, "Account added successfully", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun showConfirmationDialog(title: String, message: String, onConfirm: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Yes") { dialog, _ ->
                onConfirm()
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    private fun showRoleDialog() {
        var selectedRole = roleOptions[0] // Default selected role

        Log.d("AccountItemActivity", "showRoleDialog: $roleOptions")
        role.setText(selectedRole)
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose Role")
            .setSingleChoiceItems(roleOptions, 0) { _, which ->
                selectedRole = roleOptions[which]
            }
            .setPositiveButton("OK") { dialog, _ ->
                role.setText(selectedRole)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }

        val alertDialog = builder.create()
        alertDialog.show()
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

    private fun authenSignUp(emailValue: String, passwordValue: String) {
        val auth = FirebaseAuth.getInstance()
        auth.createUserWithEmailAndPassword(emailValue, passwordValue)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Log.d("SignUpActivity", "Successfully signed up")
                    Toast.makeText(
                        this,
                        "Successfully signed up",
                        Toast.LENGTH_SHORT
                    ).show()
                    if (user != null) {
                        user.sendEmailVerification()
                        Toast.makeText(this, "Please verify your email", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    if (task.exception?.message == "The email address is already in use by another account.") {
                        email.error =
                            "This email has already been registered. Please use a different email."
                    }
                    Log.d("SignUpActivity", "Failed to sign up: ${task.exception?.message}")
                    Toast.makeText(
                        this,
                        "Failed to sign up: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}