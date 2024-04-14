package intech.co.starbug

import android.app.Activity
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import intech.co.starbug.databinding.ActivityFeedbackDetailBinding
import intech.co.starbug.model.FeedbackModel
import java.io.Serializable

class FeedbackDetailActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityFeedbackDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFeedbackDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // Nhận dữ liệu từ Intent
        val feedback:FeedbackModel = getSerializable(this,"EXTRA_FEEDBACK",FeedbackModel::class.java)

        // Hiển thị dữ liệu trong các thành phần UI
        binding.imageViewFeedback.setImageURI(Uri.parse(feedback.imageUrl))
        binding.textViewDescription.text = feedback?.description
        binding.textViewSenderName.text = feedback?.senderName
        binding.textViewSenderPhoneNumber.text = feedback?.senderPhoneNumber
    }

    fun <T : Serializable?> getSerializable(activity: Activity, name: String, clazz: Class<T>): T
    {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            activity.intent.getSerializableExtra(name, clazz)!!
        else
            activity.intent.getSerializableExtra(name) as T
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_feedback_detail)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}