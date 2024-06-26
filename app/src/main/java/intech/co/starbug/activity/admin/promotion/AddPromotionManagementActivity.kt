package intech.co.starbug.activity.admin.promotion

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import intech.co.starbug.R
import intech.co.starbug.model.PromotionModel
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar

class AddPromotionManagementActivity : AppCompatActivity() {

    private lateinit var itemPictureImage: ImageView
    private lateinit var PromotionNameEditText: TextInputLayout
    private lateinit var PromotionDiscountEditText: TextInputLayout
    private lateinit var PromotionMinimumBillEditText: TextInputLayout
    private lateinit var PromotionStartDayEditText: TextInputLayout
    private lateinit var PromotionEndDayEditText: TextInputLayout
    private lateinit var createButton: Button
    private lateinit var cancelButton: Button

    private lateinit var PromotionsRef: DatabaseReference
    private lateinit var tempImage: String
    private val IMAGE_PICK_CODE = 1001
    private var PromotionId: String? = null
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_promotion_management)

        // Khởi tạo database reference
        val database = FirebaseDatabase.getInstance()
        PromotionsRef = database.getReference("Promotions")

        // Lấy tham chiếu đến các trường nhập liệu từ layout
        itemPictureImage = findViewById(R.id.itemPictureImage)
        PromotionNameEditText = findViewById(R.id.PromotionName)
        PromotionDiscountEditText = findViewById(R.id.PromotionDiscount)
        PromotionMinimumBillEditText = findViewById(R.id.PromotionMinimumBill)
        PromotionStartDayEditText = findViewById(R.id.PromotionStartDay)
        PromotionEndDayEditText = findViewById(R.id.PromotionEndDay)
        createButton = findViewById(R.id.buttonCreatePromotion)
        cancelButton = findViewById(R.id.cancelButton)

        // Khi người dùng nhấn vào hình ảnh, mở hộp thoại chọn hình ảnh từ thư viện
        itemPictureImage.setOnClickListener {
            openImageChooser()
        }

        cancelButton.setOnClickListener {
            finish()
        }
        handleDatePicker()
        // Khi người dùng nhấn vào nút "Tạo sản phẩm"
        createButton.setOnClickListener {
            val PromotionName = PromotionNameEditText.editText?.text.toString()
            val PromotionDiscountText = PromotionDiscountEditText.editText?.text.toString()
            val PromotionMinimumBillText = PromotionMinimumBillEditText.editText?.text.toString()
            val PromotionStartDay = PromotionStartDayEditText.editText?.text.toString()
            val PromotionEndDay = PromotionEndDayEditText.editText?.text.toString()

            // Kiểm tra xem các trường thông tin có bị bỏ trống không
            if (PromotionName.isEmpty() || PromotionDiscountText.isEmpty() || PromotionMinimumBillText.isEmpty() ||
                PromotionMinimumBillText.isEmpty() || PromotionStartDay.isEmpty() ||
                PromotionEndDay.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin sản phẩm.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Kiểm tra các trường giá có định dạng số không
            val PromotionDiscountPercentage = PromotionDiscountText.toDoubleOrNull()
            val PromotionMinimumBillMoney = PromotionMinimumBillText.toDoubleOrNull()

            if (PromotionDiscountPercentage == null || PromotionMinimumBillMoney == null) {
                Toast.makeText(this, "Vui lòng nhập dưới dạng số.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Kiểm tra các giá trị giá và đánh giá có âm không
            if (PromotionDiscountPercentage <= 0 || PromotionMinimumBillMoney <= 0) {
                Toast.makeText(this, "Số phải lớn hơn 0.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Tạo đối tượng PromotionModel từ dữ liệu nhập liệu
            val newPromotion = PromotionModel(
                name = PromotionName,
                discount = PromotionDiscountPercentage,
                minimumBill = PromotionMinimumBillMoney,
                startDay = PromotionStartDay,
                endDay = PromotionEndDay,
                img = tempImage
            )

            // Thêm sản phẩm mới vào Firebase
            PromotionId = PromotionsRef.push().key
            if (PromotionId != null) {
                // Gán ID duy nhất cho sản phẩm
                newPromotion.id = PromotionId.toString()
                PromotionsRef.child(PromotionId!!).setValue(newPromotion)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Chương trình mới đã được tạo thành công!", Toast.LENGTH_SHORT).show()
                        // Đóng hoạt động hiện tại
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Đã xảy ra lỗi! Không thể tạo chương trình mới.", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }


    private fun compareDate(startDate: String, endDate: String): Int {
        if (startDate.isEmpty() || endDate.isEmpty())
            return -1
        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val date1 = sdf.parse(startDate)
        val date2 = sdf.parse(endDate)
        return date1.compareTo(date2)
    }
    private fun handleDatePicker() {
        val calendar: Calendar = Calendar.getInstance()
        val day: Int = calendar.get(Calendar.DAY_OF_MONTH)
        val month: Int = calendar.get(Calendar.MONTH) + 1 // Month starts from 0, so add 1
        val year: Int = calendar.get(Calendar.YEAR)

        PromotionStartDayEditText.editText?.setOnClickListener {
            Log.i("AddPromotionManagement", "Start Date Clicked")
            if (PromotionStartDayEditText.editText?.text.toString().isNotEmpty())
            {
                val getStartDate = PromotionStartDayEditText.editText?.text.toString().split("/")
                DatePickerDialog(this,
                    { _, year, month, dayOfMonth ->
                        val dateSelection = "$dayOfMonth/${month + 1}/$year"
                        if (compareDate(dateSelection, PromotionEndDayEditText.editText?.text.toString()) >= 0)
                        {
                            Toast.makeText(this, "error: start date > end date", Toast.LENGTH_SHORT).show()
                            return@DatePickerDialog
                        }
                        else
                            PromotionStartDayEditText.editText?.setText("$dayOfMonth/${month + 1}/$year")
                    },getStartDate[2].toInt(),getStartDate[1].toInt() - 1,getStartDate[0].toInt()).show()
            }
            else if (PromotionEndDayEditText.editText?.text.toString().isNotEmpty())
            {
                val getEndDate = PromotionEndDayEditText.editText?.text.toString().split("/")
                DatePickerDialog(this,
                    { _, year, month, dayOfMonth ->
                        val dateSelection = "$dayOfMonth/${month + 1}/$year"
                        if (compareDate(dateSelection, PromotionEndDayEditText.editText?.text.toString()) >= 0)
                        {
                            Toast.makeText(this, "error: start date > end date", Toast.LENGTH_SHORT).show()
                            return@DatePickerDialog
                        }
                        else
                            PromotionStartDayEditText.editText?.setText("$dayOfMonth/${month + 1}/$year")
                    },getEndDate[2].toInt(),getEndDate[1].toInt() - 1,getEndDate[0].toInt()).show()
            }else
            {
                DatePickerDialog(this,
                    { _, year, month, dayOfMonth ->
                        PromotionStartDayEditText.editText?.setText("$dayOfMonth/${month + 1}/$year")
                    },year,month - 1,day).show()
            }
        }

        PromotionEndDayEditText.editText?.setOnClickListener {
            if(PromotionEndDayEditText.editText?.text.toString().isNotEmpty())
            {
                val getEndDate = PromotionEndDayEditText.editText?.text.toString().split("/")
                DatePickerDialog(this,
                    { _, year, month, dayOfMonth ->
                        val dateSelection = "$dayOfMonth/${month + 1}/$year"
                        if (compareDate(dateSelection, PromotionEndDayEditText.editText?.text.toString()) >= 0)
                        {
                            Toast.makeText(this, "error: start date > end date", Toast.LENGTH_SHORT).show()
                            return@DatePickerDialog
                        }
                        else
                            PromotionStartDayEditText.editText?.setText("$dayOfMonth/${month + 1}/$year")
                    },getEndDate[2].toInt(),getEndDate[1].toInt() - 1,getEndDate[0].toInt()).show()

            }
            if (PromotionStartDayEditText.editText?.text.toString().isNotEmpty())
            {
                val getStartDate = PromotionStartDayEditText.editText?.text.toString().split("/")
                DatePickerDialog(this,
                    { _, year, month, dayOfMonth ->
                        val dateSelection = "$dayOfMonth/${month + 1}/$year"
                        Log.i("AddPromotionManagement", "End Date Clicked ${compareDate(PromotionStartDayEditText.editText?.text.toString(), dateSelection)}")
                        if (compareDate(PromotionStartDayEditText.editText?.text.toString(), dateSelection) >= 0)
                        {
                            Toast.makeText(this, "error: start date >= end date", Toast.LENGTH_SHORT).show()
                            return@DatePickerDialog
                        }
                        else
                            PromotionEndDayEditText.editText?.setText("$dayOfMonth/${month + 1}/$year")
                    },getStartDate[2].toInt(),getStartDate[1].toInt() - 1,getStartDate[0].toInt()).show()
            }else
            {
                DatePickerDialog(this,
                    { _, year, month, dayOfMonth ->
                        PromotionEndDayEditText.editText?.setText("$dayOfMonth/${month + 1}/$year")
                    },year,month - 1,day).show()
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
            // Kiểm tra và chuyển đổi định dạng hình ảnh (nếu cần)
            val convertedImageUri = convertImageToSupportedFormat(selectedImageUri)
            // Cập nhật hình ảnh lên Firebase
            updateImageToFirebase(convertedImageUri)
        }
    }

    // Hàm để chuyển đổi định dạng hình ảnh nếu cần
    private fun convertImageToSupportedFormat(imageUri: Uri?): Uri? {
        if (imageUri != null) {
            try {
                // Kiểm tra định dạng của hình ảnh
                val inputStream = contentResolver.openInputStream(imageUri)
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeStream(inputStream, null, options)
                inputStream?.close()

                val imageMimeType = options.outMimeType ?: ""

                if (imageMimeType != "image/jpeg" && imageMimeType != "image/png") {
                    // Chuyển đổi hình ảnh sang định dạng JPG (nếu không phải là JPG hoặc PNG)
                    return convertImageToJpeg(imageUri)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        // Trả về URI của hình ảnh (đã được kiểm tra và không cần chuyển đổi)
        return imageUri
    }

    // Hàm để chuyển đổi hình ảnh sang định dạng JPG
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

            // Thực hiện xoay hình ảnh nếu cần
            val rotatedBitmap = rotateBitmap(bitmap, getOrientation(inputUri))

            // Tiến hành nén và lưu hình ảnh dưới dạng JPG
            val outputStream = ByteArrayOutputStream()
            rotatedBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)

            // Tạo và trả về URI của tệp hình ảnh đã chuyển đổi
            val tempFile = File.createTempFile("tempImage", ".jpg", cacheDir)
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

    // Hàm để xoay hình ảnh dựa trên thông tin định dạng Exif
    private fun rotateBitmap(source: Bitmap?, orientation: Int): Bitmap? {
        source ?: return null
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            else -> return source
        }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    // Hàm để lấy thông tin xoay hình ảnh từ Exif
    private fun getOrientation(uri: Uri): Int {
        val inputStream = contentResolver.openInputStream(uri)
        val exif = ExifInterface(inputStream!!)
        return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    }

    // Hàm để tải lên hình ảnh lên Firebase Storage
    private fun updateImageToFirebase(imageUri: Uri?) {
        if (imageUri != null) {
            val storageRef = FirebaseStorage.getInstance().reference
                .child("Promotion_img/${System.currentTimeMillis()}")

            storageRef.putFile(imageUri)
                .addOnSuccessListener { taskSnapshot ->
                    // Lấy URL của ảnh đã tải lên từ Firebase Storage
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        val updatedImageURL = uri.toString()
                        // Thêm URL mới vào danh sách ảnh tạm thời
                        tempImage = updatedImageURL
                        Log.i("AddPromotionManagement", "Image URL: $updatedImageURL")

                        // Hiển thị hình ảnh trong ImageView
                        Picasso.get().load(updatedImageURL).into(itemPictureImage)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("AddPromotionManagement", "Error: ${e.message}")
                }
        }
    }

}
