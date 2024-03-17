package intech.co.starbug

import CartAdapter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import intech.co.starbug.model.ProductModel

class CartActivity : AppCompatActivity() {

    private lateinit var cartAdapter: CartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        val productList = listOf(
            ProductModel(name = "Product 1", category = "Loai 1", img = listOf(""), price = 100000, ),
            ProductModel(name = "Product 2", category = "Loai 2", img = listOf(""), price = 150000),
            ProductModel(name = "Product 3", category = "Loai 3", img = listOf(""), price = 200000)
        )

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerProductsView)
        cartAdapter = CartAdapter(productList)
        recyclerView.adapter = cartAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }
}
