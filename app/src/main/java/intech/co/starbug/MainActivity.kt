package intech.co.starbug

import android.content.Context
import android.content.Intent

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import intech.co.starbug.activity.product.DetailProductActivity
import intech.co.starbug.model.ProductModel

class MainActivity : AppCompatActivity() {
    lateinit var dbRef: DatabaseReference
    val productList: MutableList<ProductModel> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.button).setOnClickListener {
            val intent = Intent(this, DetailProductActivity::class.java)

            val product = productList[30]
            intent.putExtra("product_detail", product)
            startActivity(intent)
        }

        val jsonString = readJsonFromAssets(this, "product.json")

        val productList = parseJsonToModel(jsonString)

        for (product in productList)
        {
            saveProduct(product)
        }


    }

    private fun readData() {
        dbRef = FirebaseDatabase.getInstance().getReference("Products")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (postSnapshot in dataSnapshot.children) {
                    val product = postSnapshot.getValue(ProductModel::class.java)
                    productList.add(product!!)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("error: loadPost:onCancelled", databaseError.toException())
                // ...
            }
        })
    }

    private fun getImage(): List<String> {
        return listOf(
            "https://i.pinimg.com/564x/0f/22/f6/0f22f6cb6c5692c695762fa7624b40a5.jpg",
            "https://i.pinimg.com/564x/0f/22/f6/0f22f6cb6c5692c695762fa7624b40a5.jpg",
            "https://i.pinimg.com/564x/0f/22/f6/0f22f6cb6c5692c695762fa7624b40a5.jpg",
            "https://i.pinimg.com/564x/0f/22/f6/0f22f6cb6c5692c695762fa7624b40a5.jpg",
            "https://i.pinimg.com/564x/0f/22/f6/0f22f6cb6c5692c695762fa7624b40a5.jpg",
            "https://i.pinimg.com/564x/0f/22/f6/0f22f6cb6c5692c695762fa7624b40a5.jpg"
        )
    }
    private fun saveProduct(product: ProductModel)
    {
        dbRef = FirebaseDatabase.getInstance().getReference("Products")
        val id = dbRef.push().key!!

        product.id = id
        dbRef.child(id).setValue(product).addOnCompleteListener {
        }
            .addOnCanceledListener {
                Log.i("StarBug", "save not success ${product.id}")

            }
            .addOnFailureListener {
                Log.i("StarBug", it.toString())
            }


    }

    fun readJsonFromAssets(context: Context, fileName: String): String {
        return context.assets.open(fileName).bufferedReader().use { it.readText() }
    }

    fun parseJsonToModel(jsonString: String): List<ProductModel> {
        val gson = Gson()
        return gson.fromJson(jsonString, object : TypeToken<List<ProductModel>>() {}.type)
    }
}