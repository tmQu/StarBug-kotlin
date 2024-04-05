package intech.co.starbug.model.cart


import intech.co.starbug.model.ProductModel
import java.io.Serializable


// this model have data of the product
class DetailCartItem(
    var cartItemModel: CartItemModel,
    var product: ProductModel? = null
): CartItemModel(cartItemModel), Serializable {

    fun getProductPrice(): Int{
        if(product != null)
            return cartItemModel.getProductPrice(product!!)
        return 0
    }
}