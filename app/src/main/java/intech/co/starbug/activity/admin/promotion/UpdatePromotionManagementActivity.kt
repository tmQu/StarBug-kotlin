package intech.co.starbug.activity.admin.promotion

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import intech.co.starbug.R
import intech.co.starbug.activity.admin.promotion.PromotionManagementActivity
import intech.co.starbug.model.PromotionModel
import java.text.SimpleDateFormat
import java.util.Calendar

class UpdatePromotionManagementActivity : AppCompatActivity() {

    private lateinit var PromotionId: String
    private lateinit var database: FirebaseDatabase
    private lateinit var PromotionsRef: DatabaseReference

    private lateinit var editTextPromotionName: EditText
    private lateinit var editTextPromotionDiscount: EditText
    private lateinit var editTextPromotionMimimumBill: EditText
    private lateinit var editTextPromotionStartDay: EditText
    private lateinit var editTextPromotionEndDay: EditText
    private lateinit var buttonSave: Button
    private lateinit var itemPictureImage: ImageView
    private lateinit var cancelButton: Button

    private lateinit var tempImage: String
    private val IMAGE_PICK_CODE = 1000 // Mã để xác định kết quả trả về từ hộp thoại chọn ảnh

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_promotion_management)

        editTextPromotionName = findViewById(R.id.editTextPromotionName)
        editTextPromotionDiscount = findViewById(R.id.editTextDiscount)
        editTextPromotionMimimumBill = findViewById(R.id.editTextMinimumBill)
        editTextPromotionStartDay = findViewById(R.id.editTextStartDay)
        editTextPromotionEndDay = findViewById(R.id.editTextEndDay)
        buttonSave = findViewById(R.id.buttonSavePromotion)
        itemPictureImage = findViewById(R.id.itemPictureImage)
        cancelButton = findViewById(R.id.cancelButton)


        // Nhận ID sản phẩm từ Intent
        PromotionId = intent.getStringExtra("Promotion_ID") ?: ""

        // Khởi tạo Firebase
        database = FirebaseDatabase.getInstance()
        PromotionsRef = database.getReference("Promotions")

        // Lấy thông tin sản phẩm từ Firebase và hiển thị trên giao diện
        PromotionsRef.child(PromotionId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val Promotion = dataSnapshot.getValue(PromotionModel::class.java)
                    Promotion?.let {
                        // Hiển thị thông tin sản phẩm lên giao diện
                        editTextPromotionName.setText(it.name)
                        editTextPromotionDiscount.setText(it.discount.toString())
                        editTextPromotionMimimumBill.setText(it.minimumBill.toString())
                        editTextPromotionStartDay.setText(it.startDay)
                        editTextPromotionEndDay.setText(it.endDay)
                        Picasso.get().load(it.img).into(itemPictureImage)

                        tempImage = it.img
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(
                        this@UpdatePromotionManagementActivity,
                        "Failed to load Promotion data",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })

        // Xử lý sự kiện khi người dùng nhấn nút "Lưu lại"
        buttonSave.setOnClickListener {
            updatePromotionInFirebase()
        }

        cancelButton.setOnClickListener {
            finish() // Kết thúc UpdatePromotionManagementActivity
            startActivity(
                Intent(
                    this@UpdatePromotionManagementActivity,
                    PromotionManagementActivity::class.java
                )
            ) // Khởi động PromotionManagementActivity
        }

        // Xử lý sự kiện khi người dùng chọn ảnh từ thư viện
        itemPictureImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        handleDatePicker()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            val imageUri = data.data
            updateImageToFirebase(imageUri)
        }
    }

    private fun compareDate(startDate: String, endDate: String): Int {
        if(startDate.isEmpty() || endDate.isEmpty())
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

        editTextPromotionStartDay.setOnClickListener {
            if (editTextPromotionStartDay.text.toString().isNotEmpty())
            {
                val getStartDate = editTextPromotionStartDay.text.toString().split("/")
                DatePickerDialog(this,
                    { _, year, month, dayOfMonth ->
                        val dateSelection = "$dayOfMonth/${month + 1}/$year"
                        if (compareDate(dateSelection, editTextPromotionEndDay.text.toString()) > 0)
                        {
                            Toast.makeText(this, "error: start date > end date", Toast.LENGTH_SHORT).show()
                            return@DatePickerDialog
                        }
                        else
                            editTextPromotionStartDay.setText("$dayOfMonth/${month + 1}/$year")
                    },getStartDate[2].toInt(),getStartDate[1].toInt() - 1,getStartDate[0].toInt()).show()
            }
            else
            {
                DatePickerDialog(this,
                    { _, year, month, dayOfMonth ->
                        editTextPromotionStartDay.setText("$dayOfMonth/${month + 1}/$year")
                    },year,month - 1,day).show()
            }
        }

        editTextPromotionEndDay.setOnClickListener {
            if(editTextPromotionEndDay.text.toString().isNotEmpty())
            {
                val getEndDate = editTextPromotionEndDay.text.toString().split("/")
                DatePickerDialog(this,
                    { _, year, month, dayOfMonth ->
                        val dateSelection = "$dayOfMonth/${month + 1}/$year"
                        if (compareDate(editTextPromotionStartDay.text.toString(), dateSelection) >= 0)
                        {
                            Toast.makeText(this, "error: start date > end date", Toast.LENGTH_SHORT).show()
                            return@DatePickerDialog
                        }
                        else
                            editTextPromotionEndDay.setText("$dayOfMonth/${month + 1}/$year")
                    },getEndDate[2].toInt(),getEndDate[1].toInt() - 1,getEndDate[0].toInt()).show()
            }
            else
            {
                DatePickerDialog(this,
                    { _, year, month, dayOfMonth ->
                        editTextPromotionEndDay.setText("$dayOfMonth/${month + 1}/$year")
                    },year,month - 1,day).show()
            }
        }
    }

    private fun updateImageToFirebase(imageUri: Uri?) {
        if (imageUri != null) {
            Log.i("UpdatePromotionManagementActivity", "imageUri: $imageUri")
            val storageRef =
                FirebaseStorage.getInstance().reference.child("Promotion_img/${System.currentTimeMillis()}")

            storageRef.putFile(imageUri)
                .addOnSuccessListener { taskSnapshot ->
                    // Lấy URL của ảnh đã tải lên từ Firebase Storage
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        val updatedImageURL = uri.toString()
                        val PromotionImageRef = PromotionsRef.child(PromotionId).child("img")
                        Log.i(
                            "UpdatePromotionManagementActivity",
                            "updateImageToFirebase: $updatedImageURL"
                        )
                        tempImage = updatedImageURL
                        PromotionImageRef.setValue(updatedImageURL)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this@UpdatePromotionManagementActivity,
                                    "Image updated successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                                // Hiển thị ảnh mới lên ImageView
                                Picasso.get().load(updatedImageURL).into(itemPictureImage)
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    this@UpdatePromotionManagementActivity,
                                    "Failed to update image",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updatePromotionInFirebase() {
        // Lấy dữ liệu mới từ giao diện
        val newName = editTextPromotionName.text.toString()
        val newDiscount = editTextPromotionDiscount.text.toString().toDoubleOrNull()
        val newMinimumBill = editTextPromotionMimimumBill.text.toString().toDoubleOrNull()
        val newStartDay = editTextPromotionStartDay.text.toString()
        val newEndDay = editTextPromotionEndDay.text.toString()

        // Kiểm tra dữ liệu nhập liệu có hợp lệ không
        if (newName.isNotEmpty() &&
            newDiscount != null &&
            newMinimumBill != null &&
            newStartDay.isNotEmpty() &&
            newEndDay.isNotEmpty() &&
            tempImage.isNotEmpty()) {
            // Tạo đối tượng PromotionModel mới
            val updatedPromotion = PromotionModel(
                PromotionId,
                name = newName,
                discount = newDiscount,
                minimumBill = newMinimumBill,
                startDay = newStartDay,
                endDay = newEndDay,
                img = tempImage
            )

            // Cập nhật thông tin sản phẩm vào Firebase
            PromotionsRef.child(PromotionId).setValue(updatedPromotion)
                .addOnSuccessListener {
                    Toast.makeText(
                        this@UpdatePromotionManagementActivity,
                        "Promotion updated successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish() // Kết thúc activity sau khi cập nhật thành công
                }
                .addOnFailureListener {
                    Toast.makeText(
                        this@UpdatePromotionManagementActivity,
                        "Failed to update Promotion",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            Toast.makeText(
                this@UpdatePromotionManagementActivity,
                "Please fill in all fields",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
