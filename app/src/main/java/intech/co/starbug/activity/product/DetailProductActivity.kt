package intech.co.starbug.activity.product


import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toolbar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import intech.co.starbug.R
import intech.co.starbug.StarbugApp
import intech.co.starbug.activity.comment.CommentActivity
import intech.co.starbug.adapter.CommentAdapter
import intech.co.starbug.adapter.SliderAdapter
import intech.co.starbug.adapter.VP
import intech.co.starbug.constants.SETTING
import intech.co.starbug.model.CommentModel
import intech.co.starbug.model.ProductModel
import intech.co.starbug.model.cart.CartItemModel
import intech.co.starbug.utils.Utils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.relex.circleindicator.CircleIndicator3
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
    private lateinit var btnShowMenu: FloatingActionButton

    //option menu view
    private lateinit var btnAddCart: Button
    private lateinit var sizeGroup: RadioGroup
    private lateinit var iceGroup: RadioGroup
    private lateinit var sugarGroup: RadioGroup
    private lateinit var tempGroup: RadioGroup
    private lateinit var addBtn: ShapeableImageView
    private lateinit var removeBtn: ShapeableImageView
    private lateinit var quantityView: TextView
    private lateinit var totalPrice: TextView

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
//        setSupportActionBar(findViewById(R.id.toolbar))
//
//        supportActionBar?.apply {
//            title = ""
//            // show back button on toolbar
//            // on back button press, it will navigate to parent activity
//            setDisplayHomeAsUpEnabled(true)
//            setDisplayShowHomeEnabled(true)
//
//        }
//
//        findViewById<Toolbar>(R.id.toolbar).setNavigationOnClickListener(OnClickListener { finish() })
        productId = intent.getStringExtra("product_id") ?: ""
        product = getSerializable(this, "product", ProductModel::class.java)
        listComment = mutableListOf()

        sliderImage = findViewById(R.id.vp_slider)
        sliderComment = findViewById(R.id.comment_vp)
        circleIndicator = findViewById(R.id.circleIndicator)
        btnSeeMore = findViewById(R.id.see_more_btn)
        btnShowMenu = findViewById(R.id.btn_show_menu)
        // menu view
        sugarGroup = findViewById(R.id.sugarGroup)
        sizeGroup = findViewById(R.id.sizeGroup)
        iceGroup = findViewById(R.id.iceGroup)
        tempGroup = findViewById(R.id.tempGroup)
        btnAddCart = findViewById(R.id.add_cart_btn)
        addBtn = findViewById(R.id.add_quantity_btn)
        removeBtn = findViewById(R.id.remove_quantity_btn)
        totalPrice = findViewById(R.id.total_price)
        quantityView = findViewById(R.id.quantity)

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
        handleQuantityButton()

        btnAddCart.setOnClickListener {
            addItemToCart()
        }
        btnSeeMore.setOnClickListener {
            val intent = Intent(this, CommentActivity::class.java)
            intent.putExtra("product_id", productId)
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
            when (checkedRadio.text) {
                "S" -> {
                    cartItem.size = "S"
                }
                "M" -> {
                    cartItem.size = "M"
                }
                else -> {
                    cartItem.size = "L"
                }
            }
            price.text = Utils.formatMoney(cartItem.getProductPrice(product))
            showTheTotalPrice()
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
        else {
            findViewById<LinearLayout>(R.id.temp_view).visibility = View.GONE
            if(product.iceOption == false)
                findViewById<LinearLayout>(R.id.ice_view).visibility = View.GONE

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
        else {
            findViewById<LinearLayout>(R.id.sugar_view).visibility = View.GONE
        }


    }
    private fun setDefaultMenu(radioGroup: RadioGroup)
    {
        radioGroup.check(radioGroup.getChildAt(0).id)
    }
    private fun addItemToCart()
    {
        if(cartItem.temperature == "hot")
        {
            cartItem.amountIce = "no ice"
        }
        val cartItemDAO = (application as StarbugApp).dbSQLite.cartItemDAO()
        GlobalScope.launch {
            try {
                val item = cartItemDAO.isExistCartItem(cartItem.id, cartItem.productId, cartItem.size, cartItem.temperature, cartItem.amountSugar, cartItem.amountIce)
                if( item == null)
                {
                    cartItemDAO.insertCartItem(cartItem)
                }else {
                    item.quantity += cartItem.quantity
                    cartItemDAO.updateCartItem(item)
                }
            }
            finally {

            }
        }
        finish()
    }
    private fun getProductDetail()
    {
        val productRef = FirebaseDatabase.getInstance().getReference("Products/$productId")
        productRef.addChildEventListener(object: ChildEventListener {
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                product.setter(snapshot.key?:"", snapshot.value?:0)
                showProductInfor()
                price.text = Utils.formatMoney(cartItem.getProductPrice(product))
                showTheTotalPrice()

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
        val commentRef = FirebaseDatabase.getInstance().getReference("Comment")
        val query = commentRef.orderByChild("id_product").equalTo(product.id)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listComment = mutableListOf()
                for (commentSnapshot in snapshot.children) {
                    val comment = commentSnapshot.getValue(CommentModel::class.java)
                    if(comment != null)
                        listComment.add(comment)
                }
                numOfRate = listComment.size
                avgRating = 0.0
                if(numOfRate != 0)
                {
                    for (comment in listComment)
                    {
                        avgRating += comment.rating / numOfRate.toDouble()
                    }
                    findViewById<TextView>(R.id.txt_first_comment).visibility = View.GONE
                    sliderComment.visibility = View.VISIBLE
                    val sortedComments = listComment.sortedByDescending { it.rating }
                    val topComment = sortedComments.take(5)
                    createCommentSlider(topComment)

                    numRate.text = "Based on $numOfRate reviews"
                    avgRate.text = Utils.formatAvgRate(avgRating)
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
        bottemSheet = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet))
        bottemSheet.isHideable = true
        bottemSheet.peekHeight = 300
        bottemSheet.state = STATE_COLLAPSED
        bottemSheet.isGestureInsetBottomIgnored = true

        val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if(newState == STATE_HIDDEN)
                {
                    btnShowMenu.visibility = View.VISIBLE
                }
                else {
                    btnShowMenu.visibility = View.INVISIBLE
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // Do something for slide offset.
            }
        }

        bottemSheet.addBottomSheetCallback(bottomSheetCallback)
        btnShowMenu.setOnClickListener{
            bottemSheet.state = STATE_EXPANDED
        }
    }

    private fun handleQuantityButton()
    {
        addBtn.setOnClickListener{
            cartItem.quantity += 1
            quantityView.text = cartItem.quantity.toString()
            showTheTotalPrice()
        }

        removeBtn.setOnClickListener {
            if(cartItem.quantity == 1)
            {
                // Toast
            }
            else {
                cartItem.quantity -= 1
                quantityView.text = cartItem.quantity.toString()
                showTheTotalPrice()
            }
        }
    }

    private fun showTheTotalPrice()
    {
        val totalMoney = cartItem.quantity *  cartItem.getProductPrice(product)
        totalPrice.text = Utils.formatMoney(totalMoney)
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