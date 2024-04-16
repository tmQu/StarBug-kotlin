package intech.co.starbug.model.cart


import android.os.Parcel
import android.os.Parcelable
import intech.co.starbug.model.ProductModel
import kotlinx.parcelize.Parcelize
import java.io.Serializable


// this model have data of the product

@Parcelize
class DetailCartItem(
    var cartItemModel: CartItemModel = CartItemModel(),
    var product: ProductModel? = null
): CartItemModel(cartItemModel), Parcelable, Serializable{


    fun getProductPrice(): Int{
        if(product != null)
            return cartItemModel.getProductPrice(product!!)
        return 0
    }
}
