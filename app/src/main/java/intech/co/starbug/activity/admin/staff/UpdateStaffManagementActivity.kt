package intech.co.starbug.activity.admin.staff

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import intech.co.starbug.R
import intech.co.starbug.model.UserModel

class UpdateStaffManagementActivity : AppCompatActivity() {

    private var idData: String? = null
    private var fullNameData: String? = null
    private var emailData: String? = null
    private var roleData: String? = null
    private var phoneData: String? = null
    private var passwordData: String? = null

    val roleOptions = arrayOf("Admin")

    private lateinit var fullName: TextInputLayout
    private lateinit var email: TextInputLayout
    private lateinit var phoneNo: TextInputLayout
    private lateinit var password: TextInputLayout
    private lateinit var role: TextInputLayout

    private lateinit var saveBtn: Button
    private lateinit var deleteBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_staff_management)

        fullName = findViewById(R.id.fullName)
        email = findViewById(R.id.emailInputLayout)
        phoneNo = findViewById(R.id.phoneNumberInputLayout)
        password = findViewById(R.id.passwordInputLayout)
        role = findViewById(R.id.roleInputLayout)

        getData()
        setData()

        saveBtn = findViewById(R.id.saveBtn)
        deleteBtn = findViewById(R.id.deleteBtn)

        saveBtn.setOnClickListener {
            if (fullName.editText?.text.toString().isEmpty() || email.editText?.text.toString()
                    .isEmpty()  || phoneNo.editText?.text.toString()
                    .isEmpty()
            ) {
                // Show error message indicating fields are empty
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Exit the function early
            }
            showConfirmationDialog(title = "Save Changes",
                message = "Are you sure you want to save changes?",
                onConfirm = { saveData() })
        }

        deleteBtn.setOnClickListener {
            showConfirmationDialog(title = "Delete Account",
                message = "Are you sure you want to delete this account?",
                onConfirm = { deleteAccountFromDatabase(idData!!) })
        }

        role.setOnClickListener() {
//            showRoleDialog()
        }
    }

    private fun getData() {
        idData = intent?.getStringExtra("Id")
        fullNameData = intent?.getStringExtra("FullName")
        emailData = intent?.getStringExtra("Email")
        roleData = intent?.getStringExtra("Role")
        phoneData = intent?.getStringExtra("Phone")
        passwordData = intent?.getStringExtra("Password")
    }

    private fun setData() {
        fullName.editText?.setText(fullNameData)
        email.editText?.setText(emailData)
        phoneNo.editText?.setText(phoneData)
        password.editText?.setText(passwordData)
        role.editText?.setText(roleData)
    }

    private fun saveData() {
        val resultIntent = intent.apply {
            putExtra("Id", idData)
            putExtra("FullName", fullName.editText?.text.toString())
            putExtra("Email", emailData) // Keep the original email
            putExtra("Phone", phoneNo.editText?.text.toString())
            putExtra("Password", password.editText?.text.toString())
            putExtra("Role", "Admin")
        }
        Log.i("UpdateStaffManagement", "Changes saved")
        setResult(RESULT_OK, resultIntent)
        Toast.makeText(this, "Changes saved", Toast.LENGTH_SHORT).show()
        updateAccountInFirebase(idData!!)
        finish()
    }

    private fun showConfirmationDialog(title: String, message: String, onConfirm: () -> Unit) {
        AlertDialog.Builder(this).setTitle(title).setMessage(message)
            .setPositiveButton("Yes") { dialog, _ ->
                onConfirm()
                dialog.dismiss()
            }.setNegativeButton("No") { dialog, _ ->
                dialog.cancel()
            }.show()
    }


    private fun updateAccountInFirebase(id: String) {
        val usersReference = FirebaseDatabase.getInstance().getReference("User")
        val updatedUser = UserModel(
            uid = id,
            email = emailData!!,
            name = fullName.editText?.text.toString(),
            password = password.editText?.text.toString(),
            phoneNumber = phoneNo.editText?.text.toString(),
            role = "Admin"
        )
        usersReference.child(id).setValue(updatedUser).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Account updated in database successfully", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Log.d(
                    "UpdateAccountDatabase",
                    "Failed to update account in database: ${task.exception?.message}"
                )
                Toast.makeText(
                    this,
                    "Failed to update account in database: ${task.exception?.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun deleteAccountFromDatabase(id: String) {
        Log.i("ID", "$id")
        val usersReference = FirebaseDatabase.getInstance().getReference("User")
        Log.i("DeleteAccountDatabase", "$usersReference")
        usersReference.child(id).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Account deleted from database successfully", Toast.LENGTH_SHORT)
                    .show()
                finish()
            } else {
                Log.d(
                    "DeleteAccountDatabase",
                    "Failed to delete account from database: ${task.exception?.message}"
                )
                Toast.makeText(
                    this,
                    "Failed to delete account from database: ${task.exception?.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}