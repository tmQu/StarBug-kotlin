package intech.co.starbug.activity.comment

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import intech.co.starbug.R
import intech.co.starbug.adapter.CommentAdapter
import intech.co.starbug.adapter.RV
import intech.co.starbug.model.CommentModel

class CommentActivity : AppCompatActivity() {
    lateinit var commentList: RecyclerView
    lateinit var commentRef: DatabaseReference
    lateinit var productID: String
    lateinit var avgRating: TextView
    lateinit var comments: MutableList<CommentModel>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        avgRating = findViewById(R.id.avg_rating)
        commentList = findViewById(R.id.comment_list)
        commentRef = FirebaseDatabase.getInstance().getReference("Comment")
//        productID = "-Nv0HS0Nq-OYxYUJ-109"
        getComment()

        productID = intent.getStringExtra("product_id").toString()

        commentList.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
    }

    private fun getTempComment(): List<CommentModel> {
        return listOf(
            CommentModel("", "Trần Minh Quang", "Một comment", 3.0, "0", ""),
            CommentModel("", "Trần Minh Quang", "Một comment dài", 4.0, "0", ""),
            CommentModel("", "Trần Minh Quang", "Một comment dài Một comment dài Một comment dài Một comment dài Một comment dài Một comment dài Một comment dài", 5.0, "0", ""),
            CommentModel("", "Trần Minh Quang", "Một comment dài Một comment dài Một comment dài Một comment dàiMột comment dàiMột comment dài Một comment dài ", 2.5, "0", "")
        )
    }

    private fun getComment() {
        comments = mutableListOf()
        commentRef.orderByChild("id_product").equalTo(productID).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (data in snapshot.children) {
                    val comment = data.getValue(CommentModel::class.java)
                    comment?.let {
                        comments.add(it)
                    }
                }
                commentList.adapter = CommentAdapter(comments, RV)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("CommentActivity", "Error: ${error.message}")
            }
        })
    }
}