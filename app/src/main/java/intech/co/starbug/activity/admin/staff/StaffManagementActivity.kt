package intech.co.starbug.activity.admin.staff

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ListView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import intech.co.starbug.R
import intech.co.starbug.adapter.AccountAdapter
import intech.co.starbug.model.UserModel

class StaffManagementActivity : AppCompatActivity() {
    private lateinit var usersReference: DatabaseReference
    private lateinit var listView: ListView
    private lateinit var adapter: AccountAdapter
    private lateinit var addBtn: Button
    private val userList = mutableListOf<UserModel>()
    private val idList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_staff_management)
        // Initialize Firebase Database
        usersReference = FirebaseDatabase.getInstance().getReference("User")
        Log.d("Firebase", "Database reference: $usersReference")
        listView = findViewById(R.id.listViewProducts)

        adapter = AccountAdapter(this, userList)
        listView.adapter = adapter

        // Display all accounts in ListView
        addBtn = findViewById(R.id.addButton)

        displayAccounts()

        listView.setOnItemClickListener { parent, view, position, id ->
            val user = userList[position]
            val intent = Intent(this, UpdateStaffManagementActivity::class.java)
            intent.putExtra("Id", idList[position])
            intent.putExtra("FullName", user.name)
            intent.putExtra("Email", user.email)
            intent.putExtra("Role", user.role)
            intent.putExtra("Phone", user.phoneNumber)
            intent.putExtra("Password", user.password)
            startActivityForResult(intent, 2)
        }

        addBtn.setOnClickListener {
            val intent = Intent(this, AddStaffManagementActivity::class.java)
            startActivity(intent)
        }
    }

    // Display all accounts from Firebase in ListView
    private fun displayAccounts() {
        usersReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                idList.clear()
                for (data in snapshot.children) {
                    val user = data.getValue(UserModel::class.java)
                    val id = data.key
                    if (user?.role == "Admin") {
                        id?.let {
                            idList.add(it)
                        }
                        user.let {
                            userList.add(it)
                        }
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                println("Firebase database error: ${error.message}")
            }
        })
    }
}