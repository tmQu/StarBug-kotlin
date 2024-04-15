package intech.co.starbug

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
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
import intech.co.starbug.model.ProductModel

class ProductManagementActivity : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var productsRef: DatabaseReference
    private lateinit var adapter: ProductAdapter
    private lateinit var recyclerViewProducts: RecyclerView

    private lateinit var buttonAddProduct: Button
    private lateinit var imageBackButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_management)

        // Khởi tạo recyclerViewProducts
        recyclerViewProducts = findViewById(R.id.recyclerViewProducts)
        buttonAddProduct = findViewById(R.id.buttonAddProduct)
        imageBackButton = findViewById(R.id.imageBackButton)

        database = FirebaseDatabase.getInstance()
        productsRef = database.getReference("Products")

        adapter = ProductAdapter()
        recyclerViewProducts.layoutManager = LinearLayoutManager(this)
        recyclerViewProducts.adapter = adapter

        productsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val products = mutableListOf<ProductModel>()
                for (snapshot in dataSnapshot.children) {
                    val product = snapshot.getValue(ProductModel::class.java)
                    product?.let { products.add(it) }
                }
                adapter.submitList(products)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("ProductManagement", "Failed to read value.", databaseError.toException())
            }
        })

        adapter.setOnDetailClickListener { product ->
            // Chuyển sang UpdateProductManagementActivity và truyền ID sản phẩm
            val intent =
                Intent(this@ProductManagementActivity, UpdateProductManagementActivity::class.java)
            intent.putExtra("PRODUCT_ID", product.id)
            startActivity(intent)
        }


        // Trong phương thức onCreate của ProductManagementActivity
        adapter.setOnDeleteClickListener { product ->
            val alertDialogBuilder = AlertDialog.Builder(this@ProductManagementActivity)
            alertDialogBuilder.apply {
                setTitle("Confirm Delete")
                setMessage("Are you sure you want to delete this product?")
                setPositiveButton("Yes") { _, _ ->
                    // Xóa sản phẩm từ cơ sở dữ liệu Firebase
                    productsRef.child(product.id).removeValue()
                        .addOnSuccessListener {
                            // Xử lý thành công
                            Toast.makeText(
                                this@ProductManagementActivity,
                                "Product deleted successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener { e ->
                            // Xử lý thất bại
                            Log.e("ProductManagement", "Error deleting product", e)
                            Toast.makeText(
                                this@ProductManagementActivity,
                                "Failed to delete product",
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

        buttonAddProduct.setOnClickListener {
            val intent =
                Intent(this@ProductManagementActivity, AddProductManagementActivity::class.java)
            startActivity(intent)
        }

        imageBackButton.setOnClickListener { finish() }

    }
}
