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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import intech.co.starbug.R
import intech.co.starbug.dialog.LoadingDialog
import intech.co.starbug.model.UserModel
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.util.regex.Pattern



class AddStaffManagementActivity : AppCompatActivity() {

    val roleOptions = arrayOf("Staff")

    private lateinit var fullName: TextInputLayout
    private lateinit var email: TextInputLayout
    private lateinit var phoneNo: TextInputLayout
    private lateinit var password: TextInputLayout
    private lateinit var role: TextInputEditText

    private lateinit var addBtn: Button

    private lateinit var account: UserModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_staff_management)
        fullName = findViewById(R.id.fullName)
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
                email.editText?.text.toString().isEmpty() ||
                password.editText?.text.toString().isEmpty() ||
                phoneNo.editText?.text.toString().isEmpty() ) {
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
//            showRoleDialog()
        }
    }


    private fun addData() {
        val fullNameValue = fullName.editText?.text.toString()
        val emailValue = email.editText?.text.toString()
        val phoneValue = phoneNo.editText?.text.toString()
        val passwordValue = password.editText?.text.toString()
        val roleValue = "Admin"

         account = UserModel(
            "",
            emailValue,
            fullNameValue,
            passwordValue,
            phoneValue,
            roleValue
        )



        authenSignUp(emailValue, passwordValue)
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
        val loadingDialog = LoadingDialog(this)
        loadingDialog.startLoadingDialog()
        val dbRef = FirebaseDatabase.getInstance().getReference("User")
        val query = dbRef.orderByChild("email").equalTo(emailValue)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    email.error = "This email has already been registered. Please use a different email."
                    loadingDialog.dismissDialog()

                } else {
                    queryToken()
                    loadingDialog.dismissDialog()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AddStaffManagement", "Database error: ${error.message}")
                loadingDialog.dismissDialog()
            }
        })


//        auth.createUserWithEmailAndPassword(emailValue, passwordValue)
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    val user = auth.currentUser
//                    Log.d("SignUpActivity", "Successfully signed up")
//                    Toast.makeText(
//                        this,
//                        "Successfully signed up",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    if (user != null) {
//                        account.uid = user.uid
//                        addAccountToFirebase(account)
//                    }
//                } else {
//                    if (task.exception?.message == "The email address is already in use by another account.") {
//                        email.error =
//                            "This email has already been registered. Please use a different email."
//                    }
//                    Log.d("SignUpActivity", "Failed to sign up: ${task.exception?.message}")
//                    Toast.makeText(
//                        this,
//                        "Failed to sign up: ${task.exception?.message}",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            }
    }

    private fun queryToken()
    {
        val loadingDialog = LoadingDialog(this)
        loadingDialog.startLoadingDialog()
        val mUser = FirebaseAuth.getInstance().currentUser
        mUser!!.getIdToken(true)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.i("AddStaffManagement", "Token: ${task.result?.token}")
                    val idToken = task.result.token
                    addAccountToFirebaseAuth(idToken!!, email.editText?.text.toString(), password.editText?.text.toString())

                } else {
                    // Handle error -> task.getException();
                    Toast.makeText(this, "Failed to add account, please login again", Toast.LENGTH_SHORT).show()
                }
                loadingDialog.dismissDialog()
            }
    }

    private fun addAccountToFirebaseAuth(token: String, emailValue: String, passwordValue: String)
    {
        val auth = FirebaseAuth.getInstance()
        val url = "https://lucent-halva-a93008.netlify.app/.netlify/functions/api/create-user"
        val client = OkHttpClient()
        val jsonBody = JSONObject()
        jsonBody.put("token", token)
        jsonBody.put("email", emailValue)
        jsonBody.put("password", passwordValue)
        val body = RequestBody.create("application/json".toMediaTypeOrNull(), jsonBody.toString())

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()
        val loadingDialog = LoadingDialog(this)
        loadingDialog.startLoadingDialog()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: java.io.IOException) {
                // Handle failure
                Log.i("AddStaffManagement", e.message.toString())
                this@AddStaffManagementActivity.runOnUiThread(Runnable {
                    Toast.makeText(this@AddStaffManagementActivity, "Failed to add account", Toast.LENGTH_SHORT).show()
                })
                loadingDialog.dismissDialog()
            }

            override fun onResponse(call: Call, response: Response) {
                // Handle success
                val objectString = response.body?.string()
                val objectJson = JSONObject(objectString)

                Log.i("AddStaffManagement", "Account added successfully ${objectString}")


                val status = response.code
                if(status == 200)
                {
                    account.uid = objectJson.getString("uid")
                    addAccountToFirebaseDB()
                }
                else
                {
                    this@AddStaffManagementActivity.runOnUiThread(Runnable {
                        Toast.makeText(this@AddStaffManagementActivity, "Failed to add account", Toast.LENGTH_SHORT).show()
                    })
                }

                loadingDialog.dismissDialog()
            }
        })
    }

    private fun addAccountToFirebaseDB() {
        val usersReference = FirebaseDatabase.getInstance().getReference("User")
        val id = account.uid

        id?.let {
            usersReference.child(it).setValue(account).addOnCompleteListener {
                if (it.isSuccessful) {

                    finish()
                } else {
                    Log.d(
                        "AddStaffManagement",
                        "Failed to add account to database: ${it.exception?.message}"
                    )
                }
            }
        }

        Log.i("AddStaffManagement", "Account added to Firebase ${FirebaseAuth.getInstance().currentUser?.uid}")
    }
}