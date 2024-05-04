package intech.co.starbug.activity.comment


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import coil.load
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import intech.co.starbug.R
import intech.co.starbug.helper.SharedPreferencesHelper
import intech.co.starbug.model.CommentModel
import intech.co.starbug.model.ProductModel

class CommentFormActivity : AppCompatActivity() {

    private lateinit var commentRef: DatabaseReference
    private lateinit var foodRate: RatingBar
    private lateinit var packageRate: RatingBar
    private lateinit var deliveryRate: RatingBar
    private lateinit var commentTxt: TextInputLayout
    private lateinit var submitBtn: Button
    private lateinit var productID: String

    private lateinit var product: ProductModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.comment_form_activity)

        commentRef = FirebaseDatabase.getInstance().getReference("Comment")

        foodRate = findViewById(R.id.rate_food)
        packageRate = findViewById(R.id.rate_package)
        deliveryRate = findViewById(R.id.rate_delivery)
        commentTxt = findViewById(R.id.comment)
        submitBtn = findViewById(R.id.send_review_btn)
        productID = intent.getStringExtra("product_id").toString()


        getProduct()
        submitBtn.setOnClickListener {
            if (foodRate.rating == 0f || packageRate.rating == 0f || deliveryRate.rating == 0f) {
                Toast.makeText(this, "Please fill all ratings", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (commentTxt.editText?.text.toString().isEmpty()) {
                Toast.makeText(this, "Please fill comment", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            add2DB()
            Toast.makeText(this, "Your comment added", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun getProduct() {
        val db = FirebaseDatabase.getInstance().getReference("Products/$productID")
        db.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productData = snapshot.getValue(ProductModel::class.java)
                product = productData ?: ProductModel()
                setUpProductView()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun setUpProductView() {
        // Set product name
        // Set product image
        val productName = findViewById<TextView>(R.id.name_product)
        val productImg = findViewById<ImageView>(R.id.img_product)
        val productCategory = findViewById<TextView>(R.id.category_prodcut)

        productCategory.text = product.category
        productName.text = product.name
        // Set product image
        productImg.load(product.img[0])
    }

    private fun add2DB() {
        val key = commentRef.push().key ?: ""


        val user = FirebaseAuth.getInstance().currentUser
        val userName = SharedPreferencesHelper(this).getName() ?: "Unknown"
        val userAvatar = user?.photoUrl.toString() ?: ""

        val rating = (foodRate.rating + packageRate.rating + deliveryRate.rating) / 3

        val comment = CommentModel(
            key,
            userAvatar,
            userName,
            commentTxt.editText?.text.toString() ?: "",
            food_rate = foodRate.rating.toDouble(),
            package_rate = packageRate.rating.toDouble(),
            delivery_rate = deliveryRate.rating.toDouble(),
            rating = rating.toDouble(),
            productID,
            "",
            user?.uid ?: ""
        )

        commentRef.child(key).setValue(comment)
    }


}