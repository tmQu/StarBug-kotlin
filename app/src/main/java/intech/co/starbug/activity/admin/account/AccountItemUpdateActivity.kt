package intech.co.starbug.activity.admin.account

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import intech.co.starbug.R

class AccountItemUpdateActivity : AppCompatActivity() {
    private var idData: String? = null
    private var fullNameData: String? = null
    private var emailData: String? = null
    private var roleData: String? = null
    private var phoneData: String? = null
    private var passwordData: String? = null
    private var usernameData: String? = null

    val roleOptions = arrayOf("Admin", "Customer", "Staff")

    private lateinit var fullName: TextInputLayout
    private lateinit var userName: TextInputLayout
    private lateinit var email: TextInputLayout
    private lateinit var phoneNo: TextInputLayout
    private lateinit var password: TextInputLayout
    private lateinit var role: TextInputLayout

    private lateinit var saveBtn: Button
    private lateinit var deleteBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_item)

        fullName = findViewById(R.id.fullName)
        userName = findViewById(R.id.usernameInputLayout)
        email = findViewById(R.id.emailInputLayout)
        phoneNo = findViewById(R.id.phoneNumberInputLayout)
        password = findViewById(R.id.passwordInputLayout)
        role = findViewById(R.id.roleInputLayout)

        getData()
        setData()

        saveBtn = findViewById(R.id.saveBtn)
        deleteBtn = findViewById(R.id.deleteBtn)

        saveBtn.setOnClickListener {
            if (fullName.editText?.text.toString().isEmpty() || userName.editText?.text.toString()
                    .isEmpty() || email.editText?.text.toString()
                    .isEmpty() || password.editText?.text.toString()
                    .isEmpty() || phoneNo.editText?.text.toString().isEmpty() || role.editText?.text.toString().isEmpty()
            ) {
                // Show error message indicating fields are empty
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Exit the function early
            }
            showConfirmationDialog(
                title = "Save Changes",
                message = "Are you sure you want to save changes?",
                onConfirm = { saveData() }
            )
        }

        deleteBtn.setOnClickListener {
            showConfirmationDialog(
                title = "Delete Account",
                message = "Are you sure you want to delete this account?",
                onConfirm = { deleteData() }
            )
        }

        role.setOnClickListener() {
            showRoleDialog()
        }
    }

    private fun getData() {
        idData = intent?.getStringExtra("Id")
        fullNameData = intent?.getStringExtra("FullName")
        emailData = intent?.getStringExtra("Email")
        roleData = intent?.getStringExtra("Role")
        phoneData = intent?.getStringExtra("Phone")
        passwordData = intent?.getStringExtra("Password")
        usernameData = intent?.getStringExtra("Username")
    }

    private fun setData() {
        fullName.editText?.setText(fullNameData)
        userName.editText?.setText(usernameData)
        email.editText?.setText(emailData)
        phoneNo.editText?.setText(phoneData)
        password.editText?.setText(passwordData)
        role.editText?.setText(roleData)
    }

    private fun saveData() {
        val resultIntent = intent.apply {
            putExtra("Id", idData)
            putExtra("FullName", fullName.editText?.text.toString())
            putExtra("Username", userName.editText?.text.toString())
            putExtra("Email", email.editText?.text.toString())
            putExtra("Phone", phoneNo.editText?.text.toString())
            putExtra("Password", password.editText?.text.toString())
            putExtra("Role", role.editText?.text.toString())
        }
        setResult(RESULT_OK, resultIntent)
        Toast.makeText(this, "Changes saved", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun deleteData() {
        Log.d("AccountItemActivity", "deleteData: ")
        val resultIntent = intent.apply {
            putExtra("Id", idData)
            putExtra("FullName", fullName.editText?.text.toString())
            putExtra("Username", userName.editText?.text.toString())
            putExtra("Email", email.editText?.text.toString())
            putExtra("Phone", phoneNo.editText?.text.toString())
            putExtra("Password", password.editText?.text.toString())
            putExtra("Role", role.editText?.text.toString())
            putExtra("Delete", "true")
        }
        setResult(RESULT_OK, resultIntent)
        updatePassword(emailData!!, passwordData!!, password.editText?.text.toString())
        Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show()
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
        role.editText?.setText(selectedRole)
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose Role")
            .setSingleChoiceItems(roleOptions, 0) { _, which ->
                selectedRole = roleOptions[which]
            }
            .setPositiveButton("OK") { dialog, _ ->
                role.editText?.setText(selectedRole)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun updatePassword(email: String, oldPassword: String, newPassword: String) {
        val auth = FirebaseAuth.getInstance()
        val credential = EmailAuthProvider.getCredential(email, oldPassword)

        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                user?.updatePassword(newPassword)?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("UpdatePassword", "User password updated.")
                        Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Log.d(
                            "UpdatePassword",
                            "Failed to update password: ${task.exception?.message}"
                        )
                        Toast.makeText(
                            this,
                            "Failed to update password: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Log.d("SignIn", "Failed to sign in: ${task.exception?.message}")
                Toast.makeText(
                    this, "Failed to sign in: ${task.exception?.message}", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}