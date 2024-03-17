import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import intech.co.starbug.R
import intech.co.starbug.model.ProductModel

class CartAdapter(private val productList: List<ProductModel>) :
    RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productNameTextView: TextView = itemView.findViewById(R.id.productNameTextView)
        val productImageView: ImageView = itemView.findViewById(R.id.productImageView)
        val productCategoryTextView: TextView = itemView.findViewById(R.id.productCategoryTextView)
        val productPriceTextView: TextView = itemView.findViewById(R.id.productPriceTextView)
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

        holder.productNameTextView.text = currentItem.name
//        holder.productImageView.load(currentItem.img[0]) {
//
//        }
        holder.productCategoryTextView.text = currentItem.category
        "${currentItem.price} VND".also { holder.productPriceTextView.text = it }
    }
}
