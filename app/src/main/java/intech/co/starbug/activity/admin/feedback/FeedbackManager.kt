package intech.co.starbug.activity.admin.feedback

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import intech.co.starbug.R
import intech.co.starbug.adapter.FeedbackManagerAdapter
import intech.co.starbug.model.FeedbackModel

class FeedbackManager : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var feedbackRef: DatabaseReference
    private lateinit var feedbackAdapter: FeedbackManagerAdapter
    private var feedbackList = mutableListOf<FeedbackModel>()
    private var idList = mutableListOf<String>()
    private lateinit var cancelImageBtn: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback_manager)

        database = FirebaseDatabase.getInstance()
        feedbackRef = database.getReference("Feedbacks")
        initRecyclerView()

        cancelImageBtn = findViewById(R.id.imageBackButton)
        cancelImageBtn.setOnClickListener {
            finish()
        }

        feedbackRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                feedbackList.clear()
                idList.clear()
                for (snapshot in dataSnapshot.children) {
                    val feedback = snapshot.getValue(FeedbackModel::class.java)
                    val id = snapshot.key
                    id?.let { idList.add(it) }
                    feedback?.let { feedbackList.add(it) }
                }
                feedbackAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("FeedbackManager", "Failed to read value.", databaseError.toException())
            }
        })
    }

    private fun initRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewFeedbacks)
        recyclerView.layoutManager = LinearLayoutManager(this)
        feedbackAdapter = FeedbackManagerAdapter(feedbackList)

        recyclerView.adapter = feedbackAdapter

        feedbackAdapter.setOnDetailClickListener { feedback ->
            val intent = Intent(this, FeedbackDetailActivity::class.java)

            intent.putExtra("EXTRA_FEEDBACK", feedback)

            startActivity(intent)
        }

        // Trong phương thức onCreate của ProductManagementActivity
        feedbackAdapter.setOnDeleteClickListener { feedback ->
            val alertDialogBuilder = AlertDialog.Builder(this@FeedbackManager)
            alertDialogBuilder.apply {
                setTitle("Confirm Delete")
                setMessage("Are you sure you want to delete this feedback?")
                setPositiveButton("Yes") { _, _ ->
                    // Xóa feedback từ cơ sở dữ liệu Firebase
                    feedbackRef.child(feedback.id).removeValue()
                        .addOnSuccessListener {
                            // Xử lý thành công
                            Toast.makeText(
                                this@FeedbackManager,
                                "Feedback deleted successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener { e ->
                            // Xử lý thất bại
                            Log.e("Feedback Manager", "Error deleting feedback", e)
                            Toast.makeText(
                                this@FeedbackManager,
                                "Failed to delete feedback",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
                setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
            }
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }
    }
}