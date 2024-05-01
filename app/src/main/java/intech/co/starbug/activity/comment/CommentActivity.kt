package intech.co.starbug.activity.comment

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import intech.co.starbug.R
import intech.co.starbug.adapter.CommentAdapter
import intech.co.starbug.adapter.RV
import intech.co.starbug.model.CommentModel
import intech.co.starbug.utils.Utils

class CommentActivity : AppCompatActivity() {
    lateinit var commentList: RecyclerView
    lateinit var commentRef: DatabaseReference
    lateinit var productID: String
    lateinit var avgRating: TextView
    lateinit var comments: MutableList<CommentModel>

    private lateinit var allAvgRate: TextView
    private lateinit var numComment: TextView
    private lateinit var commentBtn: FloatingActionButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        avgRating = findViewById(R.id.avg_rating)
        commentList = findViewById(R.id.comment_list)
        allAvgRate = findViewById(R.id.avg_rating)
        numComment = findViewById(R.id.total_review)
        commentRef = FirebaseDatabase.getInstance().getReference("Comment")
        commentBtn = findViewById(R.id.add_comment)
        productID = intent.getStringExtra("product_id").toString()


        commentBtn.setOnClickListener {
            val intent = Intent(this, CommentFormActivity::class.java)
            intent.putExtra("product_id", productID)
            startActivity(intent)
        }

        checkPermissionToComment()
        getComment()


        commentList.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
    }

    private fun checkPermissionToComment() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val dbRef = FirebaseDatabase.getInstance().getReference("CommentPermission/$productID/$userId")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    commentBtn.visibility = View.VISIBLE
                    commentBtn.show()
                } else {
                    commentBtn.visibility = View.GONE
                    commentBtn.hide()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("CommentActivity", "Error: ${error.message}")
            }
        })
    }


    private fun getComment() {
        comments = mutableListOf()
        commentRef.orderByChild("id_product").equalTo(productID).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                comments = mutableListOf()
                Log.i("CommentActivity", "Data: ${snapshot.childrenCount}")
                for (data in snapshot.children) {
                    val comment = data.getValue(CommentModel::class.java)
                    comment?.let {
                        comments.add(it)
                    }
                }

                sortComment()
                commentList.adapter = CommentAdapter(comments, RV)
                //update avg rating, total comment
                updateAvgRate()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("CommentActivity", "Error: ${error.message}")
            }
        })
    }

    private fun sortComment() {
        // move the own user comment to the first
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        for(i in comments.indices) {
            if(comments[i].user_uid == userId) {
                val temp = comments[i]
                comments.removeAt(i)
                comments.add(0, temp)
                break
            }
        }
    }

    private fun updateAvgRate() {
        var totalRate = 0.0
        var totalComment = 0
        for (comment in comments) {
            totalRate += comment.rating
            totalComment++
        }
        val avgRate = totalRate / totalComment
        allAvgRate.text = Utils.formatAvgRate(avgRate)
        numComment.text = "Based on $totalComment reviews"
    }
}