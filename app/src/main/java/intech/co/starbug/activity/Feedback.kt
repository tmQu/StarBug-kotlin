package intech.co.starbug.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import android.widget.Toolbar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import intech.co.starbug.R
import intech.co.starbug.model.FeedbackModel
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Feedback : AppCompatActivity() {
    private lateinit var sendBtn: Button
    private lateinit var image: ImageView

    private lateinit var database: FirebaseDatabase
    private lateinit var feedbackRef: DatabaseReference

    private lateinit var name: TextInputLayout
    private lateinit var phone: TextInputLayout
    private lateinit var description: TextInputLayout

    private lateinit var uploadImageUrl: MutableList<String>
    private var feedbackId: String? = null
    private val PICK_IMAGE_REQUEST_CODE = 101
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_feedback)

        val mToolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        mToolbar.setNavigationOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                finish()
            }
        })

        uploadImageUrl = mutableListOf()

        sendBtn = findViewById(R.id.buttonCreateFeedback)
        image = findViewById(R.id.itemPictureImage)
        name = findViewById(R.id.SenderName)
        phone = findViewById(R.id.phoneNumber)
        description = findViewById(R.id.PromotionMinimumBill)

        database = FirebaseDatabase.getInstance()
        feedbackRef = database.getReference("Feedbacks")

        sendBtn.setOnClickListener {
            addFirebase()
            Toast.makeText(this, "Sent successfully!", Toast.LENGTH_SHORT).show()
        }

        image.setOnClickListener {
            pickImage()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = Date()
        return dateFormat.format(date)
    }

    private fun addFirebase() {
        val senderNameText = name.editText?.text.toString()
        val senderPhoneNumberText = phone.editText?.text.toString()
        val descriptionText = description.editText?.text.toString()

        if (senderNameText.isBlank() || senderPhoneNumberText.isBlank() || descriptionText.isBlank()) {
            Toast.makeText(this, "Fill enough information.", Toast.LENGTH_SHORT).show()
            return
        }

        val feedback = FeedbackModel(
            time = getCurrentDateTime(),
            senderName = senderNameText,
            senderPhoneNumber = senderPhoneNumberText,
            description = descriptionText,
            img = uploadImageUrl
        )

        feedbackId = feedbackRef.push().key
        if (feedbackId != null) {
            feedback.id = feedbackId.toString()
            feedbackRef.child(feedbackId!!).setValue(feedback)
                .addOnSuccessListener {
                    Toast.makeText(this, "Successfully send feedback", Toast.LENGTH_SHORT)
                        .show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(
                        this,
                        "There is an error.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
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
            selectedImageUri = data.data
            uploadImageToFirebase(selectedImageUri)
        }
    }

    private fun uploadImageToFirebase(imageUri: Uri?) {
        if (imageUri != null) {
            var convertedImageUri: Uri? = null
            try {
                val inputStream = contentResolver.openInputStream(imageUri)
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeStream(inputStream, null, options)
                inputStream?.close()

                val imageMimeType = options.outMimeType ?: ""
                Log.i("Feedback", "Image MIME type: $imageMimeType")

                if (imageMimeType != "image/jpg" || imageMimeType != "image/jpeg" || imageMimeType != "image/png" || imageMimeType != "image/webp") {
                    convertedImageUri = convertImageToJpeg(imageUri)
                    Log.i("Feedback", "Converted image URI: $convertedImageUri")
                } else {
                    convertedImageUri = imageUri
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            if (convertedImageUri != null) {
                val storageRef = FirebaseStorage.getInstance().reference
                    .child("feedback_img/${System.currentTimeMillis()}")

                storageRef.putFile(convertedImageUri)
                    .addOnSuccessListener { taskSnapshot ->
                        storageRef.downloadUrl.addOnSuccessListener { uri ->
                            val updatedImageURL = uri.toString()
                            uploadImageUrl.add(updatedImageURL)
                            Picasso.get().load(updatedImageURL).fit().centerInside().into(image)
                        }
                    }
                    .addOnFailureListener{
                        Toast.makeText(this, "Can not upload images.", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }


    private fun convertImageToJpeg(inputUri: Uri): Uri? {
        try {
            var inputStream = contentResolver.openInputStream(inputUri)

            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            inputStream = contentResolver.openInputStream(inputUri)

            val bitmap = BitmapFactory.decodeStream(inputStream)

            val exif = inputStream?.let { ExifInterface(it) }
            val orientation = exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            val rotatedBitmap = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
                else -> bitmap
            }

            inputStream?.close()

            val outputStream = ByteArrayOutputStream()
            rotatedBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)

            val tempFile = createTempFile("tempImage", ".jpg")
            tempFile.deleteOnExit()

            tempFile.outputStream().use { output ->
                output.write(outputStream.toByteArray())
            }

            return Uri.fromFile(tempFile)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }


    private fun rotateBitmap(source: Bitmap?, angle: Float): Bitmap? {
        source ?: return null
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }
}

