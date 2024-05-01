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


class AdminCartAdapter(private val productList: List<DetailCartItem>) : RecyclerView.Adapter<AdminCartAdapter.CartViewHolder>() {

    private lateinit var quantityView: TextView

    private lateinit var productImg: ImageView
    private lateinit var productName: TextView
    private lateinit var optionMenu: TextView

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        init {


        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val cartView = inflater.inflate(R.layout.admin_cart_item, parent, false)
        return CartViewHolder(cartView)
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val currentItem = productList[position]
        holder.itemView.apply {
            quantityView = findViewById(R.id.quantity)

            productImg = findViewById(R.id.productImageView)
            productName = findViewById(R.id.productNameTextView)
            optionMenu = findViewById(R.id.menuOption)

            productName.text = currentItem.product?.name
            productImg.load(currentItem.product?.img?.get(0))
            optionMenu.text = menuOption(this, currentItem)
            quantityView.text = currentItem.quantity.toString()

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
            option += " | ${cartItem.amountIce}"
        }

        if(cartItem.amountSugar != "")
        {
            option += " | ${cartItem.amountSugar}"
        }

        return option;
    }
}
