package intech.co.starbug

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputLayout

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
            saveData()
        }

        deleteBtn.setOnClickListener {
            deleteData()
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
        finish()
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
                role.editText?.setText(selectedRole)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }

        val alertDialog = builder.create()
        alertDialog.show()
    }
}