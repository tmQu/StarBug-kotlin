package intech.co.starbug

import android.app.Activity
import android.content.Intent
import android.net.Uri
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
            val pickImageIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPEN11ABLE)
                type = "image/*" // Specify the MIME type to filter only images
            }

            if (pickImageIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(pickImageIntent, PICK_IMAGE_REQUEST_CODE)
            } else {
                Toast.makeText(this, "No app to handle image selection", Toast.LENGTH_SHORT).show()
            }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            // The user has successfully picked an image
            val selectedImageUri: Uri? = data.data
            avatar.setImageURI(selectedImageUri)
        }
    }
}