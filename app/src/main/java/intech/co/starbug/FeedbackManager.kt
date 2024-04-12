package intech.co.starbug

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import intech.co.starbug.adapter.FeedbackManagerAdapter
import intech.co.starbug.model.FeedbackModel

class FeedbackManager : AppCompatActivity() {

    private lateinit var feedbackAdapter: FeedbackManagerAdapter
    private lateinit var feedbackList: ArrayList<FeedbackModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback_manager)

        feedbackList = generateSampleFeedbackList()
        initRecyclerView()
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

        feedbackList.add(FeedbackModel("Title 1", "Description 1", "", "Sender 1", "123456789"))
        feedbackList.add(FeedbackModel("Title 2", "Description 2", "", "Sender 2", "987654321"))
        feedbackList.add(FeedbackModel("Title 2", "Description 2", "", "Sender 2", "987654321"))
        feedbackList.add(FeedbackModel("Title 2", "Description 2", "", "Sender 2", "987654321"))
        feedbackList.add(FeedbackModel("Title 5", "Description 2", "", "Sender 2", "987654321"))
        feedbackList.add(FeedbackModel("Title 2", "Description 2", "", "Sender 2", "987654321"))
        feedbackList.add(FeedbackModel("Title 4", "Description 2", "", "Sender 2", "987654321"))

        return feedbackList
    }
}