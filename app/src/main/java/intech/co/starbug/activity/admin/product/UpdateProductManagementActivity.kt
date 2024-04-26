package intech.co.starbug.activity.admin.product

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import intech.co.starbug.R
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
    private lateinit var radioYes: RadioButton
    private lateinit var radioNo: RadioButton
    private lateinit var radioSugarYes: RadioButton
    private lateinit var radioSugarNo: RadioButton
    private lateinit var buttonSave: Button
    private lateinit var editTextDescription: EditText
    private lateinit var editTextMedium: EditText
    private lateinit var editTextLarge: EditText
    private lateinit var itemPictureImage: ImageView
    private lateinit var cancelButton: Button
    private lateinit var spinnerCategory: Spinner

    private lateinit var tempImage: MutableList<String>
    private var tempAvgRate by Delegates.notNull<Double>()
    private val IMAGE_PICK_CODE = 1000 // Mã để xác định kết quả trả về từ hộp thoại chọn ảnh

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_product_management)

        editTextProductName = findViewById(R.id.editTextProductName)
        editTextProductPrice = findViewById(R.id.editTextPrice)
        spinnerCategory = findViewById(R.id.spinnerCategory)
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

        ArrayAdapter.createFromResource(
            this,
            R.array.category_array, // This is an array of strings defined in your strings.xml resource file
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinnerCategory.adapter = adapter
        }

        // Nhận ID sản phẩm từ Intent
        productId = intent.getStringExtra("PRODUCT_ID") ?: ""

        // Khởi tạo Firebase
        database = FirebaseDatabase.getInstance()
        productsRef = database.getReference("Products")

        // Lấy thông tin sản phẩm từ Firebase và hiển thị trên giao diện
        productsRef.child(productId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val product = dataSnapshot.getValue(ProductModel::class.java)
                product?.let {
                    // Hiển thị thông tin sản phẩm lên giao diện
                    editTextProductName.setText(it.name)
                    editTextProductPrice.setText(it.price.toString())
                    spinnerCategory.setSelection((spinnerCategory.adapter as ArrayAdapter<String>).getPosition(it.category))
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
                    Picasso.get().load(it.img[it.img.size - 1]).into(itemPictureImage)

                    tempImage = it.img.toMutableList()
                    tempAvgRate = it.avgRate
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@UpdateProductManagementActivity, "Failed to load product data", Toast.LENGTH_SHORT).show()
            }
        })

        // Xử lý sự kiện khi người dùng nhấn nút "Lưu lại"
        buttonSave.setOnClickListener {
            updateProductInFirebase()
        }

        cancelButton.setOnClickListener {
            finish() // Kết thúc UpdateProductManagementActivity
            startActivity(Intent(this@UpdateProductManagementActivity, ProductManagementActivity::class.java)) // Khởi động ProductManagementActivity
        }

        // Xử lý sự kiện khi người dùng chọn ảnh từ thư viện
        itemPictureImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            val imageUri = data.data
            uploadImageToFirebase(imageUri)
        }
    }

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
                            tempImage.add(updatedImageURL)
                            Picasso.get().load(updatedImageURL).fit().centerInside().into(itemPictureImage)
                        }
                    }
                    .addOnFailureListener{
                        Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
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
        val newCategory = spinnerCategory.selectedItem.toString()
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
}
