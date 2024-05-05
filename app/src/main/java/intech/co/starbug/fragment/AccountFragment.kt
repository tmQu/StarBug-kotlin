package intech.co.starbug.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import intech.co.starbug.R
import intech.co.starbug.activity.AccountSettingActivity
import intech.co.starbug.activity.Feedback
import intech.co.starbug.activity.authentication.LoginActivity
import intech.co.starbug.helper.SharedPreferencesHelper
import intech.co.starbug.model.UserModel

class AccountFragment : Fragment() {
    private lateinit var logout: Button
    private lateinit var feedback: Button
    private lateinit var accountSetting: Button
    private lateinit var userName: TextView
    private lateinit var avatar: ImageView

    private val REQUEST_CODE = 1
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)

        logout = view.findViewById(R.id.logout)
        feedback = view.findViewById(R.id.feedback)
        accountSetting = view.findViewById(R.id.account_setting)
        avatar = view.findViewById(R.id.avatar)

        userName = view.findViewById(R.id.userName)

        userName.text = getName()
        val avatarUri = FirebaseAuth.getInstance().currentUser?.photoUrl
        Picasso.get().load(avatarUri).into(avatar)

        accountSetting.setOnClickListener {
            startActivityForResult(Intent(requireContext(), AccountSettingActivity::class.java), REQUEST_CODE)
        }

        logout.setOnClickListener {
            // Logout
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        feedback.setOnClickListener {
            val intent = Intent(requireContext(), Feedback::class.java)
            startActivity(intent)
        }
        return view
    }

    private fun getUserFromDB()
    {
        // Get user data from database
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val dbRef = FirebaseDatabase.getInstance().getReference("User").child(uid.toString())
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(UserModel::class.java)
                if (user != null) {
                    userName.text = user.name
                    val avatarUri = FirebaseAuth.getInstance().currentUser?.photoUrl
                    Picasso.get().load(avatarUri).into(avatar)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AccountFragment", "Failed to read value.", error.toException())
            }
        })

    }

    fun logout(){
        val intent = Intent(activity, LoginActivity::class.java)
        Toast.makeText(activity, "Logout", Toast.LENGTH_SHORT).show()
        requireActivity().startActivity(intent)
    }

    private fun getName(): String? {
        val sharedPrefManager = SharedPreferencesHelper(requireContext())
        return sharedPrefManager.getName()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Picasso.get().load(FirebaseAuth.getInstance().currentUser?.photoUrl).into(avatar)
            userName.text = getName()
        }
    }
}