package intech.co.starbug

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
import intech.co.starbug.model.ProductModel
import kotlin.properties.Delegates

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
        productCategoryEditText = findViewById(R.id.productCategory)
        productPriceEditText = findViewById(R.id.productPrice)
        productDescriptionEditText = findViewById(R.id.productDescription)
        productMediumPriceEditText = findViewById(R.id.productMediumPrice)
        productLargePriceEditText = findViewById(R.id.productLargePrice)
        radioIceOption = findViewById(R.id.radio)
        radioSugarOption = findViewById(R.id.radioSugar)
        createButton = findViewById(R.id.buttonCreateProduct)
        cancelButton = findViewById(R.id.cancelButton)

        // Khi người dùng nhấn vào hình ảnh, mở hộp thoại chọn hình ảnh từ thư viện
        itemPictureImage.setOnClickListener{
            openImageChooser()
        }

        // Khi người dùng nhấn vào nút "Tạo sản phẩm"
        createButton.setOnClickListener {
            val productName = productNameEditText.editText?.text.toString()
            val productCategory = productCategoryEditText.editText?.text.toString()
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
            updateImageToFirebase(selectedImageUri)
        }
    }

    // Hàm để tải lên hình ảnh lên Firebase Storage
    // Trong hàm updateImageToFirebase()
    private fun updateImageToFirebase(imageUri: Uri?) {
        if (imageUri != null) {
            val storageRef = FirebaseStorage.getInstance().reference
                .child("product_img/${System.currentTimeMillis()}")

            storageRef.putFile(imageUri)
                .addOnSuccessListener { taskSnapshot ->
                    // Lấy URL của ảnh đã tải lên từ Firebase Storage
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        val updatedImageURL = uri.toString()
                        // Thêm URL mới vào danh sách ảnh tạm thời
                        tempImage.add(updatedImageURL)
                        Log.i("AddProductManagement", "Image URL: $updatedImageURL")

                        // Hiển thị hình ảnh trong ImageView
                        Picasso.get().load(updatedImageURL).into(itemPictureImage)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("AddProductManagement", "Error: ${e.message}")
                }
        }
    }

}
