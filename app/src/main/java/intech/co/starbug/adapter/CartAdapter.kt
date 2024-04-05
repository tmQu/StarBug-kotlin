import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.imageview.ShapeableImageView
import intech.co.starbug.R
import intech.co.starbug.model.cart.CartItemModel
import intech.co.starbug.model.cart.DetailCartItem
import intech.co.starbug.utils.Utils


class CartAdapter(private val productList: List<DetailCartItem>, private  val listener: ButtonClickListener) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private lateinit var deleteBtn: ShapeableImageView
    private lateinit var addQuantityBtn: ShapeableImageView
    private lateinit var removeQuantityBtn: ShapeableImageView
    private lateinit var quantityView: TextView

    private lateinit var productImg: ImageView
    private lateinit var productName: TextView
    private lateinit var optionMenu: TextView
    private lateinit var priceView: TextView

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }

        }
    }

    interface ButtonClickListener {
        fun onItemClick(position: Int)
        fun onDeleteClick(position: Int)

        fun onAddQuantityClick(position: Int)

        fun onDecreaseQuantityClick(position: Int)
    }





    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val cartView = inflater.inflate(R.layout.cart_item, parent, false)
        return CartViewHolder(cartView)
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val currentItem = productList[position]
        holder.itemView.apply {
            deleteBtn = findViewById(R.id.delete_btn)
            addQuantityBtn = findViewById(R.id.add_quantity_btn)
            removeQuantityBtn = findViewById(R.id.remove_quantity_btn)
            quantityView = findViewById(R.id.quantity)

            productImg = findViewById(R.id.productImageView)
            productName = findViewById(R.id.productNameTextView)
            optionMenu = findViewById(R.id.menuOption)
            priceView = findViewById(R.id.productPriceTextView)

            productName.text = currentItem.product?.name
            productImg.load(currentItem.product?.img?.get(0))
            optionMenu.text = menuOption(this, currentItem)
            priceView.text = calculateTotalPaid(currentItem)
            quantityView.text = currentItem.quantity.toString()

            deleteBtn.setOnClickListener{
                listener.onDeleteClick(position)
            }

            addQuantityBtn.setOnClickListener{
                listener.onAddQuantityClick(position)
            }

            removeQuantityBtn.setOnClickListener{
                listener.onDecreaseQuantityClick(position)
            }
        }
    }


    private fun calculateTotalPaid(cartItem: DetailCartItem): String {
        return Utils.formatMoney((cartItem.quantity * cartItem.getProductPrice()))
    }
    private fun menuOption(view: View, cartItem: CartItemModel): String
    {
        var option = ""
        option += "Size: ${cartItem.size}"
        if (cartItem.temperature == view.resources.getStringArray(R.array.temp_option)[0])
        {
            option += " | Hot cup"
        }
        else {
            option += " | ${cartItem.amountIce} Ice"
        }

        if(cartItem.amountSugar != "")
        {
            option += " | ${cartItem.amountSugar} sugar"
        }

        return option;
    }
}
