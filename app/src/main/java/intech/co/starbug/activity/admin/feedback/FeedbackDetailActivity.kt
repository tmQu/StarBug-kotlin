package intech.co.starbug.activity.admin.feedback

import android.app.Activity
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.squareup.picasso.Picasso
import intech.co.starbug.R
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

        val feedback:FeedbackModel = getSerializable(this,"EXTRA_FEEDBACK",FeedbackModel::class.java)

        Picasso.get().load(feedback?.imageUrl).into(binding.imageViewFeedback)
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