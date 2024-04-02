package intech.co.starbug.activity.comment

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import intech.co.starbug.R
import intech.co.starbug.adapter.CommentAdapter
import intech.co.starbug.adapter.RV
import intech.co.starbug.model.CommentModel

class CommentActivity : AppCompatActivity() {
    lateinit var commentList: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        commentList = findViewById(R.id.comment_list)

        commentList.adapter = CommentAdapter(getTempComment(), RV)
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
}