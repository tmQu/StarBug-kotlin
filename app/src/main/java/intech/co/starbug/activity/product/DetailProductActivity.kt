package intech.co.starbug.activity.product


import android.app.Activity
import android.app.assist.AssistContent
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup

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
import androidx.core.view.children
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import intech.co.starbug.StarbugApp
import intech.co.starbug.activity.CommentActivity
import intech.co.starbug.adapter.VP
import intech.co.starbug.database.StarbugDatabase
import intech.co.starbug.model.cart.CartItemDAO
import intech.co.starbug.model.cart.CartItemModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.Serializable

private const val HOT_OPTION = 0
private const val ICED_OPTION = 1
class DetailProductActivity : AppCompatActivity() {

    private val handler: Handler = Handler(Looper.getMainLooper())
    private val runnable = Runnable {
        if(sliderImage.currentItem == product.img.size - 1) {
            sliderImage.currentItem = 0
        } else{
            sliderImage.currentItem = (sliderImage.currentItem + 1)
        }
    }


    private var productId = ""
    private lateinit var listComment: MutableList<CommentModel>

    private lateinit var product: ProductModel
    private var avgRating = 0.0
    private var numOfRate = 0

    private var cartItem: CartItemModel = CartItemModel()

    // main view
    private lateinit var sliderImage: ViewPager2
    private lateinit var sliderComment: ViewPager2
    private lateinit var circleIndicator: CircleIndicator3
    private lateinit var bottemSheet: BottomSheetBehavior<LinearLayout>
    private lateinit var btnSeeMore: Button
    private lateinit var productName: TextView
    private lateinit var category: TextView
    private lateinit var avgRate: TextView
    private lateinit var numRate: TextView
    private lateinit var desc: TextView
    private lateinit var price: TextView
    private lateinit var addCartBtn: FloatingActionButton

    //option menu view
//    private lateinit var buyBtn: Button
    private lateinit var sizeGroup: RadioGroup
    private lateinit var iceGroup: RadioGroup
    private lateinit var sugarGroup: RadioGroup
    private lateinit var tempGroup: RadioGroup

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
        productId = intent.getStringExtra("product_id") ?: ""
        product = getSerializable(this, "product", ProductModel::class.java)
        listComment = mutableListOf()

        sliderImage = findViewById(R.id.vp_slider)
        sliderComment = findViewById(R.id.comment_vp)
        circleIndicator = findViewById(R.id.circleIndicator)
        btnSeeMore = findViewById(R.id.see_more_btn)
        addCartBtn = findViewById(R.id.add_cart_btn)
        // menu view
        sugarGroup = findViewById(R.id.sugarGroup)
        sizeGroup = findViewById(R.id.sizeGroup)
        iceGroup = findViewById(R.id.iceGroup)
        tempGroup = findViewById(R.id.tempGroup)


        productName = findViewById(R.id.product_name)
        category = findViewById(R.id.category)
        avgRate = findViewById(R.id.avg_rating)
        numRate = findViewById(R.id.num_rate)
        desc = findViewById(R.id.description)
        price = findViewById(R.id.price)

        
        
        initializeBottomSheet()
        getAllComment()
        showProductInfor()
        getProductDetail()
        initializeTheMenuOption()

        btnSeeMore.setOnClickListener {
            val intent = Intent(this, CommentActivity::class.java)
            startActivity(intent)
        }



    }
    private fun resetAllTheRadioButton(radioGroup: RadioGroup)
    {
        val childCount = radioGroup.childCount
        for (i in 0 until childCount)
        {
            radioGroup.getChildAt(i).alpha = 0.3f
        }
    }
    private fun initializeTheMenuOption()
    {
        cartItem.productId = productId
        if(product.medium_price == -1){
            sizeGroup.getChildAt(1).visibility = View.GONE
        }
        if(product.large_price == -1)
        {
            sizeGroup.getChildAt(2).visibility = View.GONE
        }
        sizeGroup.setOnCheckedChangeListener { group, checkedId ->
            val checkedRadio = group.findViewById<RadioButton>(checkedId)
            if(checkedRadio.text == "S"){
                cartItem.productPrice = product.price
                cartItem.size = "S"
            }
            else if(checkedRadio.text == "M")
            {
                cartItem.productPrice = product.medium_price
                cartItem.size = "M"
            }
            else{
                cartItem.productPrice = product.large_price
                cartItem.size = "M"
            }
        }
        setDefaultMenu(sizeGroup)

        iceGroup.setOnCheckedChangeListener { group, checkedId ->
            resetAllTheRadioButton(group)
            val checkedRadio = group.findViewById<RadioButton>(checkedId)
            checkedRadio.alpha = 1f
            val idx = group.indexOfChild(checkedRadio)
            cartItem.amountIce = resources.getStringArray(R.array.ice_option)[idx]
        }
        setDefaultMenu(iceGroup)


        if(product.tempOption) {
            tempGroup.visibility = View.VISIBLE
            val tempOption = resources.getStringArray(R.array.temp_option)
            tempGroup.setOnCheckedChangeListener { group, checkedId ->
                resetAllTheRadioButton(group)
                val checkedRadio = group.findViewById<RadioButton>(checkedId)
                checkedRadio.alpha = 1f
                val idx = group.indexOfChild(checkedRadio)
                cartItem.temperature = tempOption[idx]
                if(idx == HOT_OPTION)
                {
                    iceGroup.visibility = View.INVISIBLE
                }
                else {
                    iceGroup.visibility = View.VISIBLE
                }
            }
            setDefaultMenu(tempGroup)

        }

        if(product.sugarOption) {
            sugarGroup.setOnCheckedChangeListener { group, checkedId ->
                resetAllTheRadioButton(group)
                val checkedRadio = group.findViewById<RadioButton>(checkedId)
                checkedRadio.alpha = 1f
                val idx = group.indexOfChild(checkedRadio)
                cartItem.amountSugar = resources.getStringArray(R.array.sugar_option)[idx]
            }
            setDefaultMenu(sugarGroup)
        }


    }
    private fun setDefaultMenu(radioGroup: RadioGroup)
    {
        radioGroup.check(radioGroup.getChildAt(0).id)
    }
    private fun addItemToCart()
    {
        val cartItemDAO = (application as StarbugApp).dbSQLite.cartItemDAO()
        GlobalScope.launch {
            try {
                cartItemDAO.insertCartItem(cartItem)
            }
            finally {

            }
        }
    }
    private fun getProductDetail()
    {
        val productRef = FirebaseDatabase.getInstance().getReference("Products/$productId")
        productRef.addChildEventListener(object: ChildEventListener {
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val productSnapshot = snapshot.getValue(ProductModel::class.java)
                if(productSnapshot != null)
                {
                    product = productSnapshot
                    showProductInfor()
                }
            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                // notification -> the item have been deleted and finish activity
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
    private fun getAllComment()
    {
        val commentRef = FirebaseDatabase.getInstance().getReference("Comments")
        val query = commentRef.orderByChild("id_product").equalTo(product.id)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (commentSnapshot in snapshot.children) {
                    val comment = commentSnapshot.getValue(CommentModel::class.java)
                    if(comment != null)
                        listComment.add(comment)
                }
                numOfRate = listComment.size
                if(numOfRate != 0)
                {
                    for (comment in listComment)
                    {
                        avgRating += comment.rating / numOfRate.toDouble()
                    }
                    findViewById<TextView>(R.id.txt_first_comment).visibility = View.GONE
                    sliderComment.visibility = View.VISIBLE
                    val topComment = listComment.sortByDescending { it.rating } as List<CommentModel>
                    createCommentSlider(topComment.take(5))

                    numRate.text = numOfRate.toString()
                    avgRate.text = avgRating.toString()
                }


            }

            override fun onCancelled(error: DatabaseError) {
                println("Firebase database error: ${error.message}")
            }
        })
    }

    private fun showProductInfor()
    {
        productName.text = product.name
        category.text = product.category
        desc.text = product.description

        // more option on price
        price.text = product.price.toString()

        initializeSliderImg()
    }
    private fun createCommentSlider(topComment: List<CommentModel>) {
        sliderComment.offscreenPageLimit = 3 // left, center, right
        sliderComment.clipToPadding = false
        sliderComment.clipChildren = false
        val transformer = CompositePageTransformer()
        transformer.addTransformer(MarginPageTransformer(1))

        sliderComment.setPageTransformer(transformer)

        sliderComment.adapter = CommentAdapter(topComment, VP)
    }

    private fun initializeSliderImg() {
        val adapterSlider = SliderAdapter(product.img)
        sliderImage.adapter = adapterSlider
        circleIndicator.setViewPager(sliderImage)

        sliderImage.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                handler.removeCallbacks(runnable)
                handler.postDelayed(runnable, SETTING.SLIDER_DELAY_TIME.toLong())
            }
        })
    }

    private fun initializeBottomSheet() {
        bottemSheet = BottomSheetBehavior.from(findViewById<LinearLayout>(R.id.bottom_sheet))
        bottemSheet.isHideable = true
        bottemSheet.peekHeight = 300
        bottemSheet.state = STATE_COLLAPSED
        bottemSheet.isGestureInsetBottomIgnored = true

        val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if(newState == STATE_HIDDEN)
                {
                    addCartBtn.visibility = View.VISIBLE
                }
                else {
                    addCartBtn.visibility = View.INVISIBLE
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // Do something for slide offset.
            }
        }

        bottemSheet.addBottomSheetCallback(bottomSheetCallback)
        addCartBtn.setOnClickListener{
            bottemSheet.state = STATE_EXPANDED
        }
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



    fun <T : Serializable?> getSerializable(activity: Activity, name: String, clazz: Class<T>): T
    {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            activity.intent.getSerializableExtra(name, clazz)!!
        else
            activity.intent.getSerializableExtra(name) as T
    }

}