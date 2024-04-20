package intech.co.starbug.activity.admin.promotion

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import intech.co.starbug.R
import intech.co.starbug.model.PromotionModel

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
        itemPictureImage.setOnClickListener{
            openImageChooser()
        }

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
            if (PromotionDiscountPercentage <= 0 || PromotionMinimumBillMoney <= 0 ) {
                Toast.makeText(this, "Số phải lớn hơn 0.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.i("AddPromotionManagement", "tempImage: $tempImage")

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
            updateImageToFirebase(selectedImageUri)
        }
    }

    // Hàm để tải lên hình ảnh lên Firebase Storage
    // Trong hàm updateImageToFirebase()
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
