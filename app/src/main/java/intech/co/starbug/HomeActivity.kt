package intech.co.starbug

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import intech.co.starbug.adapter.CategoryAdapter
import intech.co.starbug.adapter.ItemAdapter
import intech.co.starbug.model.ProductModel
import kotlin.math.log

class HomeActivity : AppCompatActivity() {

    private lateinit var productsRef: DatabaseReference
    private lateinit var categoryButtons: MutableList<Button>
    private var selectedCategory: String? = null

    private lateinit var homeBtn: LinearLayout
    private lateinit var cartBtn: LinearLayout
    private lateinit var profileBtn: LinearLayout
    private lateinit var loveBtn: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        productsRef = FirebaseDatabase.getInstance().getReference("Products")
        categoryButtons = mutableListOf()

        retrieveCategoriesFromFirebase()
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

    private fun retrieveCategoriesFromFirebase() {
        val categoryList = mutableListOf<String>()
        productsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (productSnapshot in snapshot.children) {
                    val product = productSnapshot.getValue(ProductModel::class.java)
                    product?.let {
                        val category = it.category
                        if (category != null && category.isNotEmpty()) {
                            categoryList.add(category)
                        }
                    }
                }
                val distinctCategories = categoryList.distinct()
                showCategories(distinctCategories)
            }

            override fun onCancelled(error: DatabaseError) {
                println("Firebase database error: ${error.message}")
            }
        })
    }

    private fun showCategories(categories: List<String>) {
        val categoryRecyclerView: RecyclerView = findViewById(R.id.categoryRecyclerView)
        val categoryAdapter = CategoryAdapter(categories) { category ->
            onCategorySelected(category)
        }
        Log.d("HomeActivity", "Category selected: $categoryAdapter")
        categoryRecyclerView.adapter = categoryAdapter
        categoryRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }


    private fun onCategorySelected(category: String) {
        Log.d("HomeActivity", "Category selected: $category")
        updateCategoryButtons(category)
        Log.d("HomeActivity", "Done change button color")
        filterProductByCategory(category)
    }

    private fun updateCategoryButtons(selectedCategory: String) {
        for (button in categoryButtons) {
            Log.d("HomeActivity", "Button.text: ${button.text}")
            Log.d("HomeActivity", "Selected category: $selectedCategory")
            if (button.text == selectedCategory) {
                this.selectedCategory = selectedCategory
                button.setBackgroundColor(resources.getColor(android.R.color.darker_gray)) // Màu nền của nút được chọn
            } else {
                button.setBackgroundColor(resources.getColor(android.R.color.transparent)) // Màu nền của các nút không được chọn
            }
        }
    }


    private fun filterProductByCategory(category: String) {
        productsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productList = mutableListOf<ProductModel>()
                Log.d("HomeActivity", "Snapshot: $productList")
                for (productSnapshot in snapshot.children) {
                    val product = productSnapshot.getValue(ProductModel::class.java)
                    Log.d("HomeActivity", "Product: $product")
                    product?.let {
                        if (it.category == category) {
                            productList.add(it)
                        }
                    }
                }
                setupProductRecyclerView(productList)
            }

            override fun onCancelled(error: DatabaseError) {
                println("Firebase database error: ${error.message}")
            }
        })
    }

    private fun retrieveProductsFromFirebase() {
        val productList = mutableListOf<ProductModel>()
        productsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (productSnapshot in snapshot.children) {
                    val product = productSnapshot.getValue(ProductModel::class.java)
                    product?.let {
                        productList.add(it)
                    }
                }
                setupProductRecyclerView(productList)
            }

            override fun onCancelled(error: DatabaseError) {
                println("Firebase database error: ${error.message}")
            }
        })
    }

    private fun setupProductRecyclerView(productList: List<ProductModel>) {
        val recyclerView: RecyclerView = findViewById(R.id.itemView)
        val layoutManager = GridLayoutManager(this, 2)
        recyclerView.layoutManager = layoutManager
        val adapter = ItemAdapter(productList)
        recyclerView.adapter = adapter
    }
}
