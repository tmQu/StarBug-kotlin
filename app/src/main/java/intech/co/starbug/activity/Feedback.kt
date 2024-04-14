package intech.co.starbug.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import intech.co.starbug.R
import intech.co.starbug.constants.CONSTANT
import intech.co.starbug.model.FeedbackModel

class Feedback : AppCompatActivity() {
    private lateinit var sendBtn: Button
    private lateinit var pickImageBtn: Button
    private lateinit var image: ImageView

    private lateinit var uploadImageUrl: String

    private lateinit var database: FirebaseDatabase
    private lateinit var feedbackRef: DatabaseReference

    private lateinit var name : TextInputEditText
    private lateinit var phone : TextInputEditText
    private lateinit var description : TextInputEditText

    private val PICK_IMAGE_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_feedback)

        sendBtn = findViewById(R.id.buttonSendFeedback)
        pickImageBtn = findViewById(R.id.buttonPickImage)
        image = findViewById(R.id.imageViewFeedback)

        name = findViewById(R.id.editTextName)
        phone = findViewById(R.id.editTextPhoneNumber)
        description = findViewById(R.id.editTextFeedback)

        database = FirebaseDatabase.getInstance()
        feedbackRef = database.getReference("Feedbacks")

        sendBtn.setOnClickListener {
            addFirebase()
            showToast("Your feedback has been sent successfully!")
        }

        pickImageBtn.setOnClickListener {
            pickImage()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun addFirebase() {
        val feedback = FeedbackModel(
            description.text.toString(),
            uploadImageUrl,
            name.text.toString(),
            phone.text.toString(),
            )
        val id = feedbackRef.push().key
        id?.let {
            feedbackRef.child(id).setValue(feedback)
        }
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri: Uri? = data.data
            uploadImageToFirebase(selectedImageUri)
        }
    }

    private fun uploadImageToFirebase(imageUri: Uri?) {
        if (imageUri != null) {
            val storageRef = FirebaseStorage.getInstance().reference
                .child("feedback_img/${System.currentTimeMillis()}")

            storageRef.putFile(imageUri)
                .addOnSuccessListener { taskSnapshot ->
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        uploadImageUrl = imageUrl
                        showToast("Your feedback has been sent successfully!")
                    }
                }
                .addOnFailureListener { e ->
                    showToast("Failed to send feedback. Please try again.")
                }
        }
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
