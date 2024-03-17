package intech.co.starbug

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import intech.co.starbug.adapter.ItemAdapter
import intech.co.starbug.model.ProductModel

class HomeActivity : AppCompatActivity() {

    private lateinit var productsRef: DatabaseReference

    private lateinit var homeBtn: LinearLayout
    private lateinit var cartBtn: LinearLayout
    private lateinit var profileBtn: LinearLayout
    private lateinit var loveBtn: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        productsRef = FirebaseDatabase.getInstance().getReference("Products")

        // Retrieve data from Firebase and set up RecyclerView
        retrieveProductsFromFirebase()

        homeBtn = findViewById(R.id.homeBtnLayout)
        cartBtn = findViewById(R.id.cartBtnLayout)
        profileBtn = findViewById(R.id.profileBtnLayout)
        loveBtn = findViewById(R.id.loveBtnLayout)

        homeBtn.setOnClickListener {
            // Do nothing
        }

        cartBtn.setOnClickListener {
            // Go to CartActivity
            startActivity(Intent(this, CartActivity::class.java))
        }

        profileBtn.setOnClickListener {
            // Go to ProfileActivity
//            startActivity(Intent(this, ProfileActivity::class.java))
        }

        loveBtn.setOnClickListener {
            // Go to LoveActivity
//            startActivity(Intent(this, LoveActivity::class.java))
        }

    }

    private fun retrieveProductsFromFirebase() {
        val productList = mutableListOf<ProductModel>()

        // Retrieve data from Firebase
        productsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (productSnapshot in snapshot.children) {
                    val product = productSnapshot.getValue(ProductModel::class.java)
                    product?.let {
                        productList.add(it)
                    }
                }
                setupRecyclerView(productList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors if any
                println("Firebase database error: ${error.message}")
            }
        })
    }

    private fun setupRecyclerView(productList: List<ProductModel>) {
        val recyclerView: RecyclerView = findViewById(R.id.itemView)

        // Sử dụng GridLayoutManager với spanCount = 2 để hiển thị 2 hàng view
        val layoutManager = GridLayoutManager(this, 2)
        recyclerView.layoutManager = layoutManager

        val adapter = ItemAdapter(productList)
        recyclerView.adapter = adapter
    }
}
