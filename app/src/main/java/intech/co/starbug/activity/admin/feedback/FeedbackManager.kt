package intech.co.starbug.activity.admin.feedback

import android.content.Intent
import android.os.Bundle
import android.util.Log
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback_manager)

        database = FirebaseDatabase.getInstance()
        feedbackRef = database.getReference("Feedbacks")
        initRecyclerView()

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
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewFeedback)
        recyclerView.layoutManager = LinearLayoutManager(this)
        feedbackAdapter = FeedbackManagerAdapter(feedbackList)

        recyclerView.adapter = feedbackAdapter

        feedbackAdapter.setOnItemClickListener { feedback ->
            val intent = Intent(this, FeedbackDetailActivity::class.java)

            intent.putExtra("EXTRA_FEEDBACK", feedback)

            startActivity(intent)
        }
    }

    private fun generateSampleFeedbackList(): ArrayList<FeedbackModel> {
        val feedbackList = ArrayList<FeedbackModel>()

        feedbackList.add(FeedbackModel( "Description 1", "", "Sender 1", "123456789"))
        feedbackList.add(FeedbackModel( "Description 2", "", "Sender 2", "987654321"))
        feedbackList.add(FeedbackModel( "Description 2", "", "Sender 2", "987654321"))
        feedbackList.add(FeedbackModel( "Description 2", "", "Sender 2", "987654321"))
        feedbackList.add(FeedbackModel( "Description 2", "", "Sender 2", "987654321"))
        feedbackList.add(FeedbackModel( "Description 2", "", "Sender 2", "987654321"))
        feedbackList.add(FeedbackModel( "Description 2", "", "Sender 2", "987654321"))

        return feedbackList
    }
}