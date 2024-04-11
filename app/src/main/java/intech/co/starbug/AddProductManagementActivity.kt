package intech.co.starbug

import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import intech.co.starbug.model.ProductModel

class AddProductManagementActivity : AppCompatActivity() {

    private lateinit var productNameEditText: TextInputLayout
    private lateinit var productCategoryEditText: TextInputLayout
    private lateinit var productPriceEditText: TextInputLayout
    private lateinit var productDescriptionEditText: TextInputLayout
    private lateinit var productMediumPriceEditText: TextInputLayout
    private lateinit var productLargePriceEditText: TextInputLayout
    private lateinit var productAvgRateEditText: TextInputLayout
    private lateinit var radioIceOption: RadioButton
    private lateinit var radioSugarOption: RadioButton
    private lateinit var createButton: Button
    private lateinit var cancelButton: Button

    private lateinit var productsRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product_management)

        // Khởi tạo database reference
        val database = FirebaseDatabase.getInstance()
        productsRef = database.getReference("Products")

        // Lấy tham chiếu đến các trường nhập liệu từ layout
        productNameEditText = findViewById(R.id.productName)
        productCategoryEditText = findViewById(R.id.productCategory)
        productPriceEditText = findViewById(R.id.productPrice)
        productDescriptionEditText = findViewById(R.id.productDescription)
        productMediumPriceEditText = findViewById(R.id.productMediumPrice)
        productLargePriceEditText = findViewById(R.id.productLargePrice)
        productAvgRateEditText = findViewById(R.id.productAvgRate)
        radioIceOption = findViewById(R.id.radio)
        radioSugarOption = findViewById(R.id.radioSugar)
        createButton = findViewById(R.id.buttonCreateProduct)
        cancelButton = findViewById(R.id.cancelButton)

        createButton.setOnClickListener {
            val productName = productNameEditText.editText?.text.toString()
            val productCategory = productCategoryEditText.editText?.text.toString()
            val productPriceText = productPriceEditText.editText?.text.toString()
            val productDescription = productDescriptionEditText.editText?.text.toString()
            val productMediumPriceText = productMediumPriceEditText.editText?.text.toString()
            val productLargePriceText = productLargePriceEditText.editText?.text.toString()
            val productAvgRateText = productAvgRateEditText.editText?.text.toString()

            // Kiểm tra xem các trường thông tin có bị bỏ trống không
            if (productName.isEmpty() || productCategory.isEmpty() || productPriceText.isEmpty() ||
                productDescription.isEmpty() || productMediumPriceText.isEmpty() ||
                productLargePriceText.isEmpty() || productAvgRateText.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin sản phẩm.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Kiểm tra các trường giá có định dạng số không
            val productPrice = productPriceText.toIntOrNull()
            val productMediumPrice = productMediumPriceText.toIntOrNull()
            val productLargePrice = productLargePriceText.toIntOrNull()
            val productAvgRate = productAvgRateText.toDoubleOrNull()

            if (productPrice == null || productMediumPrice == null || productLargePrice == null || productAvgRate == null) {
                Toast.makeText(this, "Vui lòng nhập giá và đánh giá dưới dạng số.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Kiểm tra các giá trị giá và đánh giá có âm không
            if (productPrice <= 0 || productMediumPrice <= 0 || productLargePrice <= 0 || productAvgRate <= 0) {
                Toast.makeText(this, "Giá và đánh giá phải lớn hơn 0.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val iceOption = radioIceOption.isChecked
            val sugarOption = radioSugarOption.isChecked

            val newProduct = ProductModel(
                name = productName,
                category = productCategory,
                price = productPrice,
                description = productDescription,
                medium_price = productMediumPrice,
                large_price = productLargePrice,
                avgRate = productAvgRate,
                iceOption = iceOption,
                sugarOption = sugarOption,
                tempOption = true,
                img = listOf("https://i.pinimg.com/736x/57/dc/33/57dc339c0944a33bb2be77089d126399.jpg", "https://i.pinimg.com/564x/00/57/af/0057af0727f6776293d52e8782f3f908.jpg" )
            )

            // Thêm sản phẩm mới vào Firebase
            val productId = productsRef.push().key
            if (productId != null) {
                // Gán ID duy nhất cho sản phẩm
                newProduct.id = productId
                productsRef.child(productId).setValue(newProduct)
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
}
