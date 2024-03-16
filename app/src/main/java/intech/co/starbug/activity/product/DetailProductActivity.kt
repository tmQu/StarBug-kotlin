package intech.co.starbug.activity.product


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.LinearLayout

import androidx.activity.enableEdgeToEdge
import androidx.appcompat.widget.Toolbar
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import intech.co.starbug.R
import intech.co.starbug.adapter.CommentAdapter
import intech.co.starbug.adapter.SliderAdapter
import intech.co.starbug.model.ProductModel
import intech.co.starbug.constants.SETTING
import intech.co.starbug.model.CommentModel
import me.relex.circleindicator.CircleIndicator3
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED

class DetailProductActivity : AppCompatActivity() {

    private val handler: Handler = Handler(Looper.getMainLooper())
    private val runnable = Runnable {
        if(sliderImage.currentItem == product.img.size - 1) {
            sliderImage.currentItem = 0
        } else{
            sliderImage.currentItem = (sliderImage.currentItem + 1)
        }
    }
    private lateinit var sliderImage: ViewPager2
    private lateinit var sliderComment: ViewPager2
    private lateinit var circleIndicator: CircleIndicator3
    private lateinit var product: ProductModel
    private lateinit var bottemSheet: BottomSheetBehavior<LinearLayout>

    private lateinit var productName: TextView
    private lateinit var category: TextView
    private lateinit var avgRate: TextView
    private lateinit var numRate: TextView
    private lateinit var desc: TextView

    private lateinit var price: TextView
    override fun onCreate(savedInstanceState: Bundle?) {

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_detail_product)

        // hide the system bar
        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView)
        // Configure the behavior of the hidden system bars.
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())


        // show up the back button
        // set toolbar as support action bar
        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))

        supportActionBar?.apply {
            title = ""

            // show back button on toolbar
            // on back button press, it will navigate to parent activity
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)

        }
        product = intent.getSerializableExtra("product_detail") as ProductModel

        // find View
        sliderImage = findViewById(R.id.vp_slider)
        sliderComment = findViewById(R.id.comment_vp)
        circleIndicator = findViewById(R.id.circleIndicator)

        // create bottomsheet
        bottemSheet = BottomSheetBehavior.from(findViewById<LinearLayout>(R.id.bottom_sheet))
        bottemSheet.isHideable = false
        bottemSheet.peekHeight = 100
        bottemSheet.state = STATE_COLLAPSED
        bottemSheet.isGestureInsetBottomIgnored = true



        // create slider
        val adapterSlider = SliderAdapter(product.img)
        sliderImage.adapter = adapterSlider
        circleIndicator.setViewPager(sliderImage)

        sliderImage.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback()
        {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                handler.removeCallbacks(runnable)
                handler.postDelayed(runnable, SETTING.SLIDER_DELAY_TIME.toLong())
            }
        })

        // create slider comment


        sliderComment.offscreenPageLimit = 3 // left, center, right
        sliderComment.clipToPadding = false
        sliderComment.clipChildren = false
        val transformer = CompositePageTransformer()
        transformer.addTransformer(MarginPageTransformer(1))

        sliderComment.setPageTransformer(transformer)
        sliderComment.adapter = CommentAdapter(getTempComment())
    }


    // save the current slider and resume it
    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed(runnable, SETTING.SLIDER_DELAY_TIME.toLong())
    }


    private fun getTempComment(): List<CommentModel> {
        return listOf(
            CommentModel("", "Trần Minh Quang", "Một comment", 3.0, "0"),
            CommentModel("", "Trần Minh Quang", "Một comment dài", 4.0, "0"),
            CommentModel("", "Trần Minh Quang", "Một comment dài Một comment dài Một comment dài Một comment dài Một comment dài Một comment dài Một comment dài", 5.0, "0"),
            CommentModel("", "Trần Minh Quang", "Một comment dài Một comment dài Một comment dài Một comment dàiMột comment dàiMột comment dài Một comment dài ", 2.5, "0")
        )
    }


}