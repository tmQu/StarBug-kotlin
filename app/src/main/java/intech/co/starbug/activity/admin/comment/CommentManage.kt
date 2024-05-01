package intech.co.starbug.activity.admin.comment

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.libraries.places.widget.Autocomplete
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import intech.co.starbug.R
import intech.co.starbug.adapter.CommentAdminAdapter
import intech.co.starbug.dialog.CommentDialog
import intech.co.starbug.dialog.ConfirmDialog
import intech.co.starbug.model.CommentModel
import intech.co.starbug.model.ProductModel
import intech.co.starbug.utils.Utils

class CommentManage : AppCompatActivity(), CommentDialog.DialogListener{

    private var productId = ""
    private lateinit var product: ProductModel
    private lateinit var allCommentList: MutableList<CommentModel>
    private lateinit var commentList: MutableList<CommentModel>

    private lateinit var rvComment: RecyclerView
    private lateinit var searchBox: AutoCompleteTextView
    private lateinit var productName: TextView
    private lateinit var productImage: ImageView
    private lateinit var productCategory: TextView
    private lateinit var productAvgRate: TextView
    private lateinit var productTotalRate: TextView

    private var listUserName = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment_manage)

        rvComment = findViewById(R.id.rv_comment)
        searchBox = findViewById(R.id.search_box)
        productName = findViewById(R.id.name_product)
        productImage = findViewById(R.id.img_product)
        productCategory = findViewById(R.id.category_prodcut)
        productAvgRate = findViewById(R.id.avg_rate)
        productTotalRate = findViewById(R.id.total_rate)

        allCommentList = mutableListOf()
        commentList = mutableListOf()
        productId = intent.getStringExtra("product_id").toString()
        product = intent.getSerializableExtra("product") as ProductModel

        updateProductView()
        getCommentFromDB()
    }

    private fun updateProductView() {
        productName.text = product.name
        productCategory.text = product.category
        productImage.load(product.img[0])

    }

    private fun getCommentFromDB()
    {
        val db = FirebaseDatabase.getInstance().getReference("Comment")
        val query = db.orderByChild("id_product").equalTo(productId)
        query.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val comment = snapshot.getValue(CommentModel::class.java)
                comment?.let {
                    // Add comment to list
                    allCommentList.add(it)
                    commentList.add(it)
                    listUserName.add(it.user_name)
                    rvComment.adapter = CommentAdminAdapter(
                        commentList,
                        onReplyClick,
                        onDeleteReplyClick,
                        onDeleteCommentClick
                        )
                    updateAvgRate()
                    updateSearchBox()

                    rvComment.layoutManager = LinearLayoutManager(this@CommentManage, LinearLayoutManager.VERTICAL, false)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val comment = snapshot.getValue(CommentModel::class.java)
                comment?.let {
                    val index = allCommentList.indexOfFirst { it.id == comment.id }
                    if(index != -1)
                    {
                        allCommentList[index] = comment
                        val index2 = commentList.indexOfFirst { it.id == comment.id }
                        commentList[index2] = comment
                        rvComment.adapter?.notifyItemChanged(index2)
                        updateAvgRate()
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val comment = snapshot.getValue(CommentModel::class.java)
                comment?.let {
                    val index = allCommentList.indexOfFirst { it.id == comment.id }
                    if(index != -1)
                    {
                        allCommentList.removeAt(index)
                        val index2 = commentList.indexOfFirst { it.id == comment.id }
                        commentList.removeAt(index2)
                        rvComment.adapter?.notifyItemRemoved(index2)
                        updateAvgRate()
                    }
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun updateSearchBox() {
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, listUserName)
        searchBox.setAdapter(adapter)

        var time = System.currentTimeMillis()
        searchBox.addTextChangedListener {
            if(System.currentTimeMillis() - time < 800)
            {
                return@addTextChangedListener
            }
            val query = searchBox.text.toString()
            if(query.isNotEmpty())
            {
                commentList.clear()
                commentList.addAll(allCommentList.filter { it.user_name.contains(query, true) })
                rvComment.adapter?.notifyDataSetChanged()
            }else
            {
                commentList.clear()
                commentList.addAll(allCommentList)
                rvComment.adapter?.notifyDataSetChanged()
            }
            time = System.currentTimeMillis()
        }

        searchBox.setOnItemClickListener { parent, view, position, id ->
            val query = searchBox.text.toString()
            if(query.isNotEmpty())
            {
                commentList.clear()
                commentList.addAll(allCommentList.filter { it.user_name.contains(query, true) })
                rvComment.adapter?.notifyDataSetChanged()
            }else
            {
                commentList.clear()
                commentList.addAll(allCommentList)
                rvComment.adapter?.notifyDataSetChanged()
            }
            clearFocusAndHideKeyboard(searchBox)
        }
        searchBox.setOnKeyListener { v, keyCode, event ->
            if(keyCode == 66)
            {
                val query = searchBox.text.toString()
                if(query.isNotEmpty())
                {
                    commentList.clear()
                    commentList.addAll(allCommentList.filter { it.user_name.contains(query, true) })
                    rvComment.adapter?.notifyDataSetChanged()
                }else
                {
                    commentList.clear()
                    commentList.addAll(allCommentList)
                    rvComment.adapter?.notifyDataSetChanged()
                }
                clearFocusAndHideKeyboard(searchBox)
            }
            false
        }
    }

    private fun updateAvgRate() {
        var totalRate = 0.0
        allCommentList.forEach {
            totalRate += it.rating
        }
        productAvgRate.text = Utils.formatAvgRate(totalRate / allCommentList.size)
        productTotalRate.text = "Based on ${allCommentList.size} reviews"
    }

    override fun onSendCommentClick(commentIndex: Int, comment: String) {
        commentList[commentIndex].reply = comment
        updateCommentToDB(commentList[commentIndex])
    }



    private fun updateCommentToDB(comment: CommentModel)
    {
        val db = FirebaseDatabase.getInstance().getReference("Comment")
        db.child(comment.id).setValue(comment)
    }

    private val onReplyClick: (position: Int) -> Unit ={
        // Handle reply button click
        val dialog = CommentDialog()
        val bundle = Bundle()
        bundle.putInt("comment_index", it)
        bundle.putString("reply", commentList[it].reply)
        dialog.arguments = bundle
        dialog.show(supportFragmentManager, "CommentDialog")
    }

    private val onDeleteReplyClick: (position: Int) -> Unit ={
        // Handle delete reply button click
        commentList[it].reply = ""
        updateCommentToDB(commentList[it])
    }

     private val onDeleteCommentClick: (Int) -> Unit = {
        // Handle delete comment button click
        val dialog = ConfirmDialog()
        val bundle = Bundle()
        bundle.putString("message", "Are you sure you want to delete this comment?")
        bundle.putString("comment_id", commentList[it].id)
        dialog.arguments = bundle
        dialog.show(supportFragmentManager, "ConfirmDialog")

    }
    private fun clearFocusAndHideKeyboard(searchBox: AutoCompleteTextView) {
        searchBox.clearFocus()
        val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(searchBox.windowToken, 0)
    }

}