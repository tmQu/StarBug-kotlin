package intech.co.starbug.fragment

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.os.Build
import android.os.Bundle

import android.util.Log
import android.util.TypedValue
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.widget.PopupMenu

import androidx.annotation.MenuRes
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import intech.co.starbug.R
import intech.co.starbug.activity.product.DetailProductActivity
import intech.co.starbug.adapter.CategoryAdapter
import intech.co.starbug.adapter.ItemAdapter
import intech.co.starbug.adapter.SliderAdapter
import intech.co.starbug.constants.CONSTANT
import intech.co.starbug.model.ProductModel
import intech.co.starbug.model.SliderModel


class HomeFragment : Fragment() {

    private lateinit var shimmer: ShimmerFrameLayout
    private lateinit var productsRef: DatabaseReference
    private lateinit var categoryButtons: MutableList<Button>
    private lateinit var gridItem: RecyclerView

    private lateinit var listAllProduct: MutableList<ProductModel>
    private lateinit var productList: MutableList<ProductModel>


    private var queryName: String = ""
    private var selectedCategory: String = CONSTANT.ALL_CATEGORY
    private var sortCriteria: String = CONSTANT.SORT_BY_DEFAULT

    private lateinit var layout: View
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        layout = inflater.inflate(R.layout.fragment_home, container, false)
        productsRef = FirebaseDatabase.getInstance().getReference("Products")
        categoryButtons = mutableListOf()
        gridItem = layout.findViewById(R.id.itemView)

        listAllProduct = mutableListOf()
        productList = mutableListOf()

        shimmer = layout.findViewById(R.id.shimmerFrameLayout)
        shimmer.startShimmer()
        retrieveCategoriesFromFirebase()
        retrieveProductsFromFirebase()

        layout.findViewById<ImageButton>(R.id.filter_btn).setOnClickListener {
            it.startAnimation(AnimationUtils.loadAnimation(context, R.anim.click_animation))
            showMenuSort(it, R.menu.sort_criteria_menu)

        }

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
            selectedCategory = category
            filterProduct()
        }
        Log.d("HomeActivity", "Category selected: $categoryAdapter")
        categoryRecyclerView.adapter = categoryAdapter
        categoryRecyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
    }


//    private fun onCategorySelected(category: String) {
//        Log.d("HomeActivity", "Category selected: $category")
//        Log.d("HomeActivity", "Done change button color")
//        filterProductByCategory(category, CONSTANT.SORT_BY_DEFAULT, "")
//    }

//    private fun updateCategoryButtons(selectedCategory: String) {
//        for (button in categoryButtons) {
//            Log.d("HomeActivity", "Button.text: ${button.text}")
//            Log.d("HomeActivity", "Selected category: $selectedCategory")
//            if (button.text == selectedCategory) {
//                this.selectedCategory = selectedCategory
//                button.setBackgroundColor(resources.getColor(android.R.color.darker_gray)) // Màu nền của nút được chọn
//            } else {
//                button.setBackgroundColor(resources.getColor(android.R.color.transparent)) // Màu nền của các nút không được chọn
//            }
//        }
//    }


    private fun filterProduct() {
        productList = mutableListOf<ProductModel>()
        for (product in listAllProduct) {
            if (product.category == selectedCategory || selectedCategory == CONSTANT.ALL_CATEGORY){
                productList.add(product)
            }
        }

        // sort product by Price, Name
        if (sortCriteria == CONSTANT.SORT_BY_PRICE) {
            productList.sortBy { it.price }
        }else if (sortCriteria == CONSTANT.SORT_BY_NAME) {
            productList.sortBy { it.name }
        }
        else if (sortCriteria == CONSTANT.SORT_BY_PRICE_DESC) {
            productList.sortByDescending { it.price }
        }else if (sortCriteria == CONSTANT.SORT_BY_NAME_DESC) {
            productList.sortByDescending { it.name }
        }

        // query by name
        if (queryName.isNotEmpty()) {
            productList = productList.filter { it.name.contains(queryName, ignoreCase = true) } as MutableList<ProductModel>
        }

        setupProductRecyclerView(productList)
    }

    private fun retrieveProductsFromFirebase() {
        productsRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val product = snapshot.getValue(ProductModel::class.java)
                product?.let {
                    listAllProduct.add(it)
                }
                shimmer.stopShimmer()
                shimmer.visibility = View.GONE
                filterProduct()
                setUpSearchView(listAllProduct.map { it.name })
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val product = snapshot.getValue(ProductModel::class.java)
                product?.let {
                    var index = productList.indexOfFirst { it.id == product.id }
                    Log.d("HomeActivity", "Index: $index")
                    if (index != -1) {
                        productList[index] = product
                    }

                    index = listAllProduct.indexOfFirst { it.id == product.id }
                    if (index != -1) {
                        listAllProduct[index] = product
                    }
                }
                gridItem.adapter?.notifyDataSetChanged()
                setUpSearchView(listAllProduct.map { it.name })

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                println("Firebase database error: ${error.message}")
            }
        })
    }

    private fun setupProductRecyclerView(productList: List<ProductModel>) {
        val layoutManager = GridLayoutManager(activity, 2)
        gridItem.layoutManager = layoutManager
        val adapter = ItemAdapter(productList)
        gridItem.adapter = adapter
        adapter.onItemClick = { imageView, position ->
            val intent = Intent(activity, DetailProductActivity::class.java)
            intent.putExtra("product_id", productList[position].id)
            intent.putExtra("product", productList[position])
            val options = ActivityOptions
                .makeSceneTransitionAnimation(activity, imageView, "productImage")

            startActivity(intent, options.toBundle())
        }

    }

    @SuppressLint("RestrictedApi")
    private fun showMenuSort(v: View, @MenuRes menuRes: Int) {
        if(context == null)
            return
        val popup = PopupMenu(/* context = */ requireContext(), /* anchor = */ v)

        popup.menuInflater.inflate(menuRes, popup.menu)

        Log.i("HomeFragment", "MenuBuilder out")

        if (popup.menu is  MenuBuilder) {
            val menuBuilder = popup.menu as MenuBuilder
            menuBuilder.setOptionalIconsVisible(true)
            for (item in menuBuilder.visibleItems) {
                val iconMarginPx =
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, (5).toFloat(), resources.displayMetrics
                    )
                        .toInt()
                if (item.icon != null) {
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                        item.icon = InsetDrawable(item.icon, iconMarginPx, 0, iconMarginPx, 0)
                    } else {
                        item.icon =
                            object : InsetDrawable(item.icon, iconMarginPx, 0, iconMarginPx, 0) {
                                override fun getIntrinsicWidth(): Int {
                                    return intrinsicHeight + iconMarginPx + iconMarginPx
                                }
                            }
                    }
                }
            }
        }
        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.sort_by_price_inc -> {
                    sortCriteria = CONSTANT.SORT_BY_PRICE
                    filterProduct()
                    true
                }
                R.id.sort_by_name_inc -> {
                    sortCriteria = CONSTANT.SORT_BY_NAME
                    filterProduct()
                    true
                }
                R.id.sort_by_price_desc -> {
                    sortCriteria = CONSTANT.SORT_BY_PRICE_DESC
                    filterProduct()
                    true
                }
                R.id.sort_by_name_desc -> {
                    sortCriteria = CONSTANT.SORT_BY_NAME_DESC
                    filterProduct()
                    true
                }
                else -> false
            }
        }
        popup.setOnDismissListener {
            // Respond to popup being dismissed.
        }

        popup.show()

    }

    private fun setUpSearchView(listName: List<String>) {
        val searchView = layout.findViewById<AutoCompleteTextView>(R.id.search_box)

        searchView.setAdapter(
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                listName
            )
        )
        searchView.setOnItemClickListener { parent, view, position, id ->
            val product = listAllProduct.find { it.name == searchView.text.toString() }
            val intent = Intent(activity, DetailProductActivity::class.java)
            if (product != null) {
                intent.putExtra("product_id", product.id)
                intent.putExtra("product", product)
                startActivity(intent)
                clearFocusAndHideKeyboard(searchView)
            }


        }

        searchView.setOnKeyListener{ v, keyCode, event ->   // v: View, keyCode: Int, event: KeyEvent
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                queryName = searchView.text.toString()
                filterProduct()
                clearFocusAndHideKeyboard(searchView)
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }

        val searchBtn = layout.findViewById<ImageButton>(R.id.search_btn)
        searchBtn.setOnClickListener {
            it.startAnimation(AnimationUtils.loadAnimation(context, R.anim.click_animation))
            queryName = searchView.text.toString()
            clearFocusAndHideKeyboard(searchView)
            filterProduct()
        }
    }

    private fun clearFocusAndHideKeyboard(searchBox: AutoCompleteTextView) {
        searchBox.clearFocus()
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(searchBox.windowToken, 0)
    }

    private fun showSliderPromotion()
    {
        val slider = layout.findViewById<RecyclerView>(R.id.slider_promotion)
        val sliderRef = FirebaseDatabase.getInstance().getReference("Sliders")
        sliderRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listSlider = mutableListOf<SliderModel>()
                for (sliderSnapshot in snapshot.children) {
                    val slider = sliderSnapshot.getValue(SliderModel::class.java)
                    if (slider != null) {
                        listSlider.add(slider)
                    }
                }
                val adapter = SliderAdapter(listSlider.map { it.imgUrl })
                slider.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                println("Firebase database error: ${error.message}")
            }
        })
    }


}