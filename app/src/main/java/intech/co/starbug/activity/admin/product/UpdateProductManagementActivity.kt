package intech.co.starbug.activity.admin.product

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import intech.co.starbug.R
import intech.co.starbug.adapter.EditImageAdapter
import intech.co.starbug.model.ProductModel
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import kotlin.properties.Delegates

class UpdateProductManagementActivity : AppCompatActivity() {

    private lateinit var productId: String
    private lateinit var database: FirebaseDatabase
    private lateinit var productsRef: DatabaseReference

    private lateinit var editTextProductName: EditText
    private lateinit var editTextProductPrice: EditText
    private lateinit var editTextProductCategory: EditText
    private lateinit var radioYes: RadioButton
    private lateinit var radioNo: RadioButton
    private lateinit var radioSugarYes: RadioButton
    private lateinit var radioSugarNo: RadioButton
    private lateinit var buttonSave: Button
    private lateinit var editTextDescription: EditText
    private lateinit var editTextMedium: EditText
    private lateinit var editTextLarge: EditText
    private lateinit var itemPictureImage: ViewPager2
    private lateinit var cancelButton: Button

    private var currentImage: MutableList<String> = mutableListOf()
    private lateinit var tempImage: MutableList<String>
    private lateinit var deleteImage: MutableList<String>
    private var tempAvgRate by Delegates.notNull<Double>()

    private lateinit var imageAdapter: EditImageAdapter
    private val IMAGE_PICK_CODE = 1000 // Mã để xác định kết quả trả về từ hộp thoại chọn ảnh
    private var countNewImage = 0
    private var countDeleteImage = 0
    private var saveButtonClick = false

    private val handler = Handler(Looper.getMainLooper())
    private fun waitForCallback() {

        Log.i("UpdateProductManagementActivity", "countNewImage: $countNewImage countDeleteImage: $countDeleteImage")

        if(countNewImage == 0 && countDeleteImage == 0 && saveButtonClick)
        {
            Log.i("UpdateProductManagementActivity", "in countNewImage: $countNewImage countDeleteImage: $countDeleteImage")
            updateProductInFirebase()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_product_management)

        editTextProductName = findViewById(R.id.editTextProductName)
        editTextProductPrice = findViewById(R.id.editTextPrice)
        editTextProductCategory = findViewById(R.id.editTextCategory)
        radioYes = findViewById(R.id.radio)
        radioNo = findViewById(R.id.radio2)
        radioSugarYes = findViewById(R.id.radioSugar)
        radioSugarNo = findViewById(R.id.radio2Sugar)
        buttonSave = findViewById(R.id.buttonSaveProduct)
        editTextDescription = findViewById(R.id.editTextDescription)
        editTextMedium = findViewById(R.id.editTextMedium)
        editTextLarge = findViewById(R.id.editTextLarge)
        itemPictureImage = findViewById(R.id.itemPictureImage)
        cancelButton = findViewById(R.id.cancelButton)

        // Nhận ID sản phẩm từ Intent
        productId = intent.getStringExtra("PRODUCT_ID") ?: ""

        // Khởi tạo Firebase
        database = FirebaseDatabase.getInstance()
        productsRef = database.getReference("Products")

        imageAdapter = EditImageAdapter(mutableListOf())
        {

        }
        deleteImage = mutableListOf()
        itemPictureImage.adapter = imageAdapter
        // Lấy thông tin sản phẩm từ Firebase và hiển thị trên giao diện
        productsRef.child(productId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val product = dataSnapshot.getValue(ProductModel::class.java)
                product?.let { it ->
                    // Hiển thị thông tin sản phẩm lên giao diện
                    editTextProductName.setText(it.name)
                    editTextProductPrice.setText(it.price.toString())
                    editTextProductCategory.setText(it.category)
                    if (it.sugarOption) {
                        radioSugarYes.isChecked = true
                    } else {
                        radioSugarNo.isChecked = true
                    }
                    if (it.iceOption) {
                        radioYes.isChecked = true
                    } else {
                        radioNo.isChecked = true
                    }
                    editTextDescription.setText(it.description)
                    editTextMedium.setText(it.medium_price.toString())
                    editTextLarge.setText(it.large_price.toString())

                    currentImage = it.img.toMutableList()
                    tempImage = it.img.toMutableList()
                    tempAvgRate = it.avgRate

                    imageAdapter = EditImageAdapter(it.img.toMutableList()) {position: Int ->
                        val stringImage: String = tempImage[position]
                        deleteImage.add(stringImage)
                        tempImage.removeAt(position)
                    }
                    itemPictureImage.adapter = imageAdapter


                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@UpdateProductManagementActivity, "Failed to load product data", Toast.LENGTH_SHORT).show()
            }
        })

        // Xử lý sự kiện khi người dùng nhấn nút "Lưu lại"
        buttonSave.setOnClickListener {
            for (i in 0 until tempImage.size)
            {
                if(tempImage[i] !in currentImage)
                {
                    countNewImage++
                }
            }

            for (i in 0 until currentImage.size)
            {
                if(currentImage[i] !in tempImage)
                {
                    countDeleteImage++
                }
            }
            Log.i("UpdateProductManagementActivity", "start countNewImage: $countNewImage countDeleteImage: $countDeleteImage")
            for (i in 0 until tempImage.size)
            {
                if(tempImage[i] !in currentImage)
                {
                    uploadImageToFirebase(Uri.parse(tempImage[i]))
                }
            }
            for (i in 0 until currentImage.size)
            {
                if(currentImage[i] !in tempImage)
                {
                    deleteImageToFirebase(currentImage[i])
                }
            }

            // start Progrss

            saveButtonClick = true

        }

        cancelButton.setOnClickListener {
            finish() // Kết thúc UpdateProductManagementActivity
        }




        // Xử lý sự kiện khi người dùng chọn ảnh từ thư viện

        findViewById<ImageView>(R.id.camera).setOnClickListener {
//            val intent = Intent(Intent.ACTION_PICK)
//            intent.type = "image/*"
//            startActivityForResult(intent, IMAGE_PICK_CODE)
            // show dialog edit image

            pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

    }

    val pickMultipleMedia =
        this.registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(5)) { uris ->
            // Callback is invoked after the user selects media items or closes the
            // photo picker.
            if (uris.isNotEmpty()) {
                for (uri in uris) {

                    tempImage.add(uri.toString())
                    imageAdapter.addImage(uri.toString())
                }
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }
    private fun getPathStorageFromUrl(url:String): String?{
        // https://firebasestorage.googleapis.com/v0/b/starbug-9fad0.appspot.com/o/product_img%2F1712984227297?alt=media&token=7fd642be-bd4d-41f4-866b-403d9395140e
        val baseUrl = "https://firebasestorage.googleapis.com/v0/b/starbug-9fad0.appspot.com/o/"
        if(url.contains(baseUrl))
        {
            val parts = url.split("/")
            val lastPart = parts.last()
            val fileNameWithoutParams = lastPart.substringBefore("?").replace("%2F", "/")
            Log.i("UpdateProductManagementActivity", "getPathStorageFromUrl: $fileNameWithoutParams")
            return fileNameWithoutParams
        }
        else {
            return null
        }
    }
    private fun deleteImageToFirebase(url: String)
    {
        Log.i("UpdateProductManagementActivity", "deleteImageToFirebase pahtname: $url")


        val storageRef = FirebaseStorage.getInstance().reference
        val getPathName = getPathStorageFromUrl(url)
        if(getPathName == null)
        {
            countDeleteImage--
            waitForCallback()
            return
        }
        Log.i("UpdateProductManagementActivity", "deleteImageToFirebase pahtname: $getPathName")

        val desertRef = storageRef.child(getPathName)
        desertRef.delete().addOnSuccessListener {
            // File deleted successfully
            Log.i("UpdateProductManagementActivity", "deleteImageToFirebase: File deleted successfully $getPathName")
        }.addOnFailureListener {
            // Uh-oh, an error occurred!
            Log.i("UpdateProductManagementActivity", "deleteImageToFirebase: File deleted unsuccessfully $getPathName")

        }.addOnCompleteListener {
            Log.i("UpdateProductManagementActivity", "deleteImageToFirebase: File deleted complete $getPathName")
            countDeleteImage--
            waitForCallback()

        }


    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null) {
//            val imageUri = data.data
//            uploadImageToFirebase(imageUri)
//        }
//    }

    private fun uploadImageToFirebase(imageUri: Uri?) {
        if (imageUri != null) {
            var convertedImageUri: Uri? = null
            try {
                // Kiểm tra định dạng của hình ảnh
                val inputStream = contentResolver.openInputStream(imageUri)
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeStream(inputStream, null, options)
                inputStream?.close()

                val imageMimeType = options.outMimeType ?: ""

                if (imageMimeType != "image/jpg" && imageMimeType != "image/jpeg" && imageMimeType != "image/png" && imageMimeType != "image/webp") {
                    // Chuyển đổi hình ảnh sang định dạng JPG
                    convertedImageUri = convertImageToJpeg(imageUri)
                } else {
                    convertedImageUri = imageUri
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            if (convertedImageUri != null) {
                val storageRef = FirebaseStorage.getInstance().reference
                    .child("product_img/${System.currentTimeMillis()}")

                storageRef.putFile(convertedImageUri)
                    .addOnSuccessListener { taskSnapshot ->
                        storageRef.downloadUrl.addOnSuccessListener { uri ->
                            val updatedImageURL = uri.toString()
                            Log.i("UpdateProductManagementActivity", "uploadImageToFirebase: $updatedImageURL $imageUri ${tempImage.indexOf(imageUri.toString())}")
                            tempImage[tempImage.indexOf(imageUri.toString())] = updatedImageURL

                        }
                            .addOnCompleteListener {
                                countNewImage--
                                waitForCallback()

                            }
                    }
                    .addOnFailureListener{
                        Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                    }
                    .addOnCompleteListener {

                    }
            }
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun convertImageToJpeg(inputUri: Uri): Uri? {
        try {
            var inputStream = contentResolver.openInputStream(inputUri)

            // Đọc thông tin kích thước của ảnh gốc
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            // Đặt lại đọc ảnh từ đầu
            inputStream = contentResolver.openInputStream(inputUri)

            // Decode bitmap với thông tin kích thước đã lấy
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

            val tempFile = File.createTempFile("tempImage", ".jpg", cacheDir)
            val uri = FileProvider.getUriForFile(this, "your.package.name.fileprovider", tempFile)

            tempFile.outputStream().use { output ->
                output.write(outputStream.toByteArray())
            }

            return uri
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

    private fun updateProductInFirebase() {

        // Lấy dữ liệu mới từ giao diện
        val newName = editTextProductName.text.toString()
        val newPrice = editTextProductPrice.text.toString().toDoubleOrNull()
        val newCategory = editTextProductCategory.text.toString()
        val newRadioYes = radioYes.isChecked
        val newRadioSugarYes = radioSugarYes.isChecked
        val newDescription = editTextDescription.text.toString()
        val newMedium = editTextMedium.text.toString().toIntOrNull()
        val newLarge = editTextLarge.text.toString().toIntOrNull()

        // Kiểm tra dữ liệu nhập liệu có hợp lệ không
        if (newName.isNotEmpty() && newPrice != null && newCategory.isNotEmpty()) {
            // Tạo đối tượng ProductModel mới
            val updatedProduct = ProductModel(productId,
                name = newName, category = newCategory,
                img = tempImage,
                price = newPrice.toInt(),
                medium_price = newMedium!!,
                large_price = newLarge!!,
                description = newDescription,
                iceOption = newRadioYes,
                sugarOption = newRadioSugarYes,
                avgRate = tempAvgRate)

            // Cập nhật thông tin sản phẩm vào Firebase
            productsRef.child(productId).setValue(updatedProduct)
                .addOnSuccessListener {
                    Toast.makeText(this@UpdateProductManagementActivity, "Product updated successfully", Toast.LENGTH_SHORT).show()
                    finish() // Kết thúc activity sau khi cập nhật thành công
                }
                .addOnFailureListener {
                    Toast.makeText(this@UpdateProductManagementActivity, "Failed to update product", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this@UpdateProductManagementActivity, "Please fill in all fields", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
