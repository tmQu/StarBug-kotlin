package intech.co.starbug.fragment

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import intech.co.starbug.R
import intech.co.starbug.activity.product.DetailProductActivity
import intech.co.starbug.adapter.CategoryAdapter
import intech.co.starbug.adapter.ItemAdapter
import intech.co.starbug.model.ProductModel

class HomeFragment : Fragment() {

    private lateinit var productsRef: DatabaseReference
    private lateinit var categoryButtons: MutableList<Button>
    private var selectedCategory: String? = null


    private lateinit var layout: View
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        layout = inflater.inflate(R.layout.fragment_home, container, false)
        productsRef = FirebaseDatabase.getInstance().getReference("Products")
        categoryButtons = mutableListOf()

        retrieveCategoriesFromFirebase()
        retrieveProductsFromFirebase()

        return layout
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
        val categoryRecyclerView: RecyclerView = layout.findViewById(R.id.categoryRecyclerView)
        val categoryAdapter = CategoryAdapter(categories) { category ->
            onCategorySelected(category)
        }
        Log.d("HomeActivity", "Category selected: $categoryAdapter")
        categoryRecyclerView.adapter = categoryAdapter
        categoryRecyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
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
        val recyclerView: RecyclerView = layout.findViewById(R.id.itemView)
        val layoutManager = GridLayoutManager(activity, 2)
        recyclerView.layoutManager = layoutManager
        val adapter = ItemAdapter(productList)
        recyclerView.adapter = adapter
        adapter.onItemClick = { imageView, position ->
            val intent = Intent(activity, DetailProductActivity::class.java)
            intent.putExtra("product_id", productList[position].id)
            intent.putExtra("product", productList[position])
            val options = ActivityOptions
                .makeSceneTransitionAnimation(activity, imageView, "productImage")

            startActivity(intent, options.toBundle())
        }

    }


}