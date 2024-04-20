package intech.co.starbug.activity.admin.promotion

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
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
import intech.co.starbug.PromotionAdapter
import intech.co.starbug.R
import intech.co.starbug.model.PromotionModel

class PromotionManagementActivity : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var PromotionsRef: DatabaseReference
    private lateinit var adapter: PromotionAdapter
    private lateinit var recyclerViewPromotions: RecyclerView

    private lateinit var buttonAddPromotion: Button
    private lateinit var imageBackButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_promotion_management)

        // Khởi tạo recyclerViewPromotions
        recyclerViewPromotions = findViewById(R.id.recyclerViewPromotions)
        buttonAddPromotion = findViewById(R.id.buttonAddPromotion)
        imageBackButton = findViewById(R.id.imageBackButton)

        database = FirebaseDatabase.getInstance()
        PromotionsRef = database.getReference("Promotions")

        adapter = PromotionAdapter()
        recyclerViewPromotions.layoutManager = LinearLayoutManager(this)
        recyclerViewPromotions.adapter = adapter

        PromotionsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val Promotions = mutableListOf<PromotionModel>()
                for (snapshot in dataSnapshot.children) {
                    val Promotion = snapshot.getValue(PromotionModel::class.java)
                    Promotion?.let { Promotions.add(it) }
                }
                adapter.submitList(Promotions)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("PromotionManagement", "Failed to read value.", databaseError.toException())
            }
        })

        adapter.setOnDetailClickListener { Promotion ->
            // Chuyển sang UpdatePromotionManagementActivity và truyền ID sản phẩm
            val intent =
                Intent(this@PromotionManagementActivity, UpdatePromotionManagementActivity::class.java)
            intent.putExtra("Promotion_ID", Promotion.id)
            startActivity(intent)
        }


        // Trong phương thức onCreate của PromotionManagementActivity
        adapter.setOnDeleteClickListener { Promotion ->
            val alertDialogBuilder = AlertDialog.Builder(this@PromotionManagementActivity)
            alertDialogBuilder.apply {
                setTitle("Confirm Delete")
                setMessage("Are you sure you want to delete this Promotion?")
                setPositiveButton("Yes") { _, _ ->
                    // Xóa sản phẩm từ cơ sở dữ liệu Firebase
                    PromotionsRef.child(Promotion.id).removeValue()
                        .addOnSuccessListener {
                            // Xử lý thành công
                            Toast.makeText(
                                this@PromotionManagementActivity,
                                "Promotion deleted successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener { e ->
                            // Xử lý thất bại
                            Log.e("PromotionManagement", "Error deleting Promotion", e)
                            Toast.makeText(
                                this@PromotionManagementActivity,
                                "Failed to delete Promotion",
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

        buttonAddPromotion.setOnClickListener {
            val intent =
                Intent(this@PromotionManagementActivity, AddPromotionManagementActivity::class.java)
            startActivity(intent)
        }

        imageBackButton.setOnClickListener { finish() }

    }
}
