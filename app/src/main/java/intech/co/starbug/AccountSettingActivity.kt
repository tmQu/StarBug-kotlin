package intech.co.starbug

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputLayout
import intech.co.starbug.constants.CONSTANT.Companion.READ_STORAGE_PERMISSION_REQUEST_CODE
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

//      Dang nhap = cai gi thi khong duoc doi cai do
        cancleBtn = findViewById(R.id.cancleBtn)
        saveBtn = findViewById(R.id.saveBtn)
        avatar = findViewById(R.id.avatar)
        fullname = findViewById(R.id.fullName)
        phoneNo = findViewById(R.id.phoneNumberInputLayout)
        password = findViewById(R.id.passwordInputLayout)
        confirmPassword = findViewById(R.id.confirmPasswordInputLayout)


        // pick image from gallery
        avatar.setOnClickListener {
            requestPermission()
        }

        cancleBtn.setOnClickListener {
            // Cancel
        }

        saveBtn.setOnClickListener {
            // Save data
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