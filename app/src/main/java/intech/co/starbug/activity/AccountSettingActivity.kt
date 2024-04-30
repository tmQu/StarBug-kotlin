package intech.co.starbug.activity

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import intech.co.starbug.R
import intech.co.starbug.constants.CONSTANT.Companion.READ_STORAGE_PERMISSION_REQUEST_CODE
import intech.co.starbug.helper.SharedPreferencesHelper
import intech.co.starbug.model.UserModel
import java.util.regex.Pattern

class AccountSettingActivity : AppCompatActivity() {
    private lateinit var cancleBtn: Button
    private lateinit var saveBtn: Button
    private lateinit var avatar: ImageView
    private lateinit var fullname: TextInputLayout
    private lateinit var phoneNo: TextInputLayout
    private lateinit var password: TextInputLayout
    private lateinit var confirmPassword: TextInputLayout

    private val PICK_IMAGE_REQUEST_CODE = 101
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_setting)

        cancleBtn = findViewById(R.id.cancleBtn)
        saveBtn = findViewById(R.id.saveBtn)
        avatar = findViewById(R.id.avatar)
        fullname = findViewById(R.id.fullName)
        phoneNo = findViewById(R.id.phoneNumberInputLayout)
        password = findViewById(R.id.passwordInputLayout)
        confirmPassword = findViewById(R.id.confirmPasswordInputLayout)

        getCurrentUserData()

        // pick image from gallery
        avatar.setOnClickListener {
            requestPermission()
        }

        cancleBtn.setOnClickListener {
            finish()
        }

        saveBtn.setOnClickListener {
            if (fullname.editText?.text.toString().isEmpty() || phoneNo.editText?.text.toString().isEmpty() || password.editText?.text.toString().isEmpty() || confirmPassword.editText?.text.toString().isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(password.editText?.text.toString() != confirmPassword.editText?.text.toString()){
                Toast.makeText(this, "Password and Confirm Password must be the same", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            updateAccount()
        }

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

    private fun updateAccount() {
        val sharedPrefManager = SharedPreferencesHelper(this)
        val email = sharedPrefManager.getEmail()
        val pw = sharedPrefManager.getPassword()
        val uid = sharedPrefManager.getUid()
        val role = sharedPrefManager.getRole()

        val user = UserModel(
            uid!!,
            email!!,
            fullname.editText?.text.toString(),
            password.editText?.text.toString(),
            phoneNo.editText?.text.toString(),
            role!!
        )

        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference("User/${uid}")

        Log.d("UpdateAccount", userRef.toString())

        userRef.setValue(user).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d("UpdateAccount", "User account updated.")
                Toast.makeText(this, "Account updated successfully", Toast.LENGTH_SHORT).show()
                updatePassword(email, pw!!, password.editText?.text.toString())
                finish()
            } else {
                Log.d("UpdateAccount", "Failed to update account: ${it.exception?.message}")
                Toast.makeText(
                    this, "Failed to update account: ${it.exception?.message}", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun getCurrentUserData() {
        val sharedPrefManager = SharedPreferencesHelper(this)
        val userName = sharedPrefManager.getName()
        val userPhone = sharedPrefManager.getPhoneNumber()
        val pw = sharedPrefManager.getPassword()
        fullname.editText?.setText(userName)
        phoneNo.editText?.setText(userPhone)
        password.editText?.setText(pw)
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

    private fun requestPermission()
    {
        // if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
        if (this.checkSelfPermission(READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
            // Permission is already available
            // Start the image picker
            pickImage()
        }
        else
        {
            // Request for permission
            requestPermissions(arrayOf(READ_EXTERNAL_STORAGE), READ_STORAGE_PERMISSION_REQUEST_CODE)
        }
    }

    private fun pickImage() {
        val pickImageIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*" // Specify the MIME type to filter only images
        }

        if (pickImageIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(pickImageIntent, PICK_IMAGE_REQUEST_CODE) // improve the deprecated code
        } else {
            Toast.makeText(this, "No app to handle image selection", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            // The user has successfully picked an image
            val selectedImageUri: Uri? = data.data
            avatar.setImageURI(selectedImageUri)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_STORAGE_PERMISSION_REQUEST_CODE)
        {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                // Permission is granted
                pickImage()
            }
            else
            {
                // Permission is denied
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}