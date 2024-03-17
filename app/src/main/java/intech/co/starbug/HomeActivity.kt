package intech.co.starbug

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import intech.co.starbug.adapter.ItemAdapter
import intech.co.starbug.model.ProductModel

class HomeActivity : AppCompatActivity() {

    private lateinit var productsRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        productsRef = FirebaseDatabase.getInstance().getReference("Products")

        // Retrieve data from Firebase and set up RecyclerView
        retrieveProductsFromFirebase()
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
