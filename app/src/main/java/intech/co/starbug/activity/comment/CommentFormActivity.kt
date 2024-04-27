package intech.co.starbug.activity.comment

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.RatingBar
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import intech.co.starbug.R
import intech.co.starbug.fragment.HomeFragment
import intech.co.starbug.model.CommentModel

class CommentFormActivity : AppCompatActivity() {

    private lateinit var commentRef: DatabaseReference
    private lateinit var foodRate: RatingBar
    private lateinit var packageRate: RatingBar
    private lateinit var deliveryRate: RatingBar
    private lateinit var commentTxt: TextInputEditText
    private lateinit var submitBtn: Button
    private lateinit var productID: String

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
//        productID = "-Nv0HS0Nq-OYxYUJ-109"



        submitBtn.setOnClickListener {
            if (foodRate.rating == 0f || packageRate.rating == 0f || deliveryRate.rating == 0f) {
                Toast.makeText(this, "Please fill all ratings", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            add2DB()
            Toast.makeText(this, "Your comment added", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, HomeFragment::class.java))
        }
    }

    private fun add2DB() {
        val user = FirebaseAuth.getInstance().currentUser
        val userName = user?.displayName ?: "Unknown"
        val userAvatar = user?.photoUrl.toString() ?: ""

        val rating = (foodRate.rating + packageRate.rating + deliveryRate.rating) / 3

        val comment = CommentModel(
            userAvatar, userName,
            commentTxt.text.toString() ?: "",
            rating.toDouble(),
            productID,
            ""
        )

        commentRef.push().setValue(comment)
    }


}