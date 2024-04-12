package intech.co.starbug.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import intech.co.starbug.R
import intech.co.starbug.model.OrderModel

class DetailOrderActivity : AppCompatActivity() {

    private lateinit var order: OrderModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_order)

    }
}