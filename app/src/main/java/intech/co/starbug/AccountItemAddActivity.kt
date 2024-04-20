package intech.co.starbug

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class AccountItemAddActivity : AppCompatActivity() {

    val roleOptions = arrayOf("Admin", "Customer", "Staff")

    private lateinit var fullName: TextInputLayout
    private lateinit var userName: TextInputLayout
    private lateinit var email: TextInputLayout
    private lateinit var phoneNo: TextInputLayout
    private lateinit var password: TextInputLayout
    private lateinit var role: TextInputEditText

    private lateinit var addBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_item_add)

        fullName = findViewById(R.id.fullName)
        userName = findViewById(R.id.usernameInputLayout)
        email = findViewById(R.id.emailInputLayout)
        phoneNo = findViewById(R.id.phoneNumberInputLayout)
        password = findViewById(R.id.passwordInputLayout)
        role = findViewById(R.id.roleInputLayout)

        addBtn = findViewById(R.id.addToDB)

        addBtn.setOnClickListener {
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

}