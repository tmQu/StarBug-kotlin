package intech.co.starbug.activity.admin.product

import android.app.Activity
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
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import intech.co.starbug.R
import intech.co.starbug.model.ProductModel
import java.io.ByteArrayOutputStream
import java.io.IOException

class AddProductManagementActivity : AppCompatActivity() {

    private lateinit var itemPictureImage: ImageView
    private lateinit var productNameEditText: TextInputLayout
    private lateinit var productCategoryEditText: TextInputLayout
    private lateinit var productPriceEditText: TextInputLayout
    private lateinit var productDescriptionEditText: TextInputLayout
    private lateinit var productMediumPriceEditText: TextInputLayout
    private lateinit var productLargePriceEditText: TextInputLayout
    private lateinit var radioIceOption: RadioButton
    private lateinit var radioSugarOption: RadioButton
    private lateinit var createButton: Button
    private lateinit var cancelButton: Button
    private lateinit var spinnerCategory: Spinner

    private lateinit var productsRef: DatabaseReference
    private lateinit var tempImage: MutableList<String>
    private val IMAGE_PICK_CODE = 1001
    private var productId: String? = null
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product_management)

        // Khởi tạo database reference
        val database = FirebaseDatabase.getInstance()
        productsRef = database.getReference("Products")

        // Khởi tạo danh sách ảnh tạm thời
        tempImage = mutableListOf()

        // Lấy tham chiếu đến các trường nhập liệu từ layout
        itemPictureImage = findViewById(R.id.itemPictureImage)
        productNameEditText = findViewById(R.id.productName)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        productPriceEditText = findViewById(R.id.productPrice)
        productDescriptionEditText = findViewById(R.id.productDescription)
        productMediumPriceEditText = findViewById(R.id.productMediumPrice)
        productLargePriceEditText = findViewById(R.id.productLargePrice)
        radioIceOption = findViewById(R.id.radio)
        radioSugarOption = findViewById(R.id.radioSugar)
        createButton = findViewById(R.id.buttonCreateProduct)
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

        // Khi người dùng nhấn vào hình ảnh, mở hộp thoại chọn hình ảnh từ thư viện
        itemPictureImage.setOnClickListener{
            openImageChooser()
        }

        // Khi người dùng nhấn vào nút "Hủy"
        cancelButton.setOnClickListener {
            finish()
        }

        // Khi người dùng nhấn vào nút "Tạo sản phẩm"
        createButton.setOnClickListener {
            val productName = productNameEditText.editText?.text.toString()
            val productCategory = spinnerCategory.selectedItem.toString()
            val productPriceText = productPriceEditText.editText?.text.toString()
            val productDescription = productDescriptionEditText.editText?.text.toString()
            val productMediumPriceText = productMediumPriceEditText.editText?.text.toString()
            val productLargePriceText = productLargePriceEditText.editText?.text.toString()

            // Kiểm tra xem các trường thông tin có bị bỏ trống không
            if (productName.isEmpty() || productCategory.isEmpty() || productPriceText.isEmpty() ||
                productDescription.isEmpty() || productMediumPriceText.isEmpty() ||
                productLargePriceText.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin sản phẩm.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Kiểm tra các trường giá có định dạng số không
            val productPrice = productPriceText.toIntOrNull()
            val productMediumPrice = productMediumPriceText.toIntOrNull()
            val productLargePrice = productLargePriceText.toIntOrNull()

            if (productPrice == null || productMediumPrice == null || productLargePrice == null) {
                Toast.makeText(this, "Vui lòng nhập giá và đánh giá dưới dạng số.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Kiểm tra các giá trị giá và đánh giá có âm không
            if (productPrice <= 0 || productMediumPrice <= 0 || productLargePrice <= 0 ) {
                Toast.makeText(this, "Giá và đánh giá phải lớn hơn 0.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val iceOption = radioIceOption.isChecked
            val sugarOption = radioSugarOption.isChecked

            Log.i("AddProductManagement", "tempImage: $tempImage")

            // Tạo đối tượng ProductModel từ dữ liệu nhập liệu
            val newProduct = ProductModel(
                name = productName,
                category = productCategory,
                price = productPrice,
                description = productDescription,
                medium_price = productMediumPrice,
                large_price = productLargePrice,
                avgRate = 0.0,
                iceOption = iceOption,
                sugarOption = sugarOption,
                tempOption = true,
                img = tempImage
            )

            // Thêm sản phẩm mới vào Firebase
            productId = productsRef.push().key
            if (productId != null) {
                // Gán ID duy nhất cho sản phẩm
                newProduct.id = productId.toString()
                productsRef.child(productId!!).setValue(newProduct)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Sản phẩm mới đã được tạo thành công!", Toast.LENGTH_SHORT).show()
                        // Đóng hoạt động hiện tại
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Đã xảy ra lỗi! Không thể tạo sản phẩm mới.", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    // Hàm để mở hộp thoại chọn hình ảnh từ thư viện
    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    // Xử lý kết quả trả về từ hộp thoại chọn hình ảnh
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            // Cập nhật hình ảnh lên Firebase
            uploadImageToFirebase(selectedImageUri)
        }
    }

    // Hàm để tải lên hình ảnh lên Firebase Storage
    // Trong hàm updateImageToFirebase()
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

                if (imageMimeType != "image/jpg" || imageMimeType != "image/jpeg" || imageMimeType != "image/png" || imageMimeType != "image/webp") {
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
                        Toast.makeText(this, "Không thể tải ảnh lên", Toast.LENGTH_SHORT).show()
                    }
            }
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
