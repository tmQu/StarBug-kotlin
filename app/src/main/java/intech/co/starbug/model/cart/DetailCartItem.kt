package intech.co.starbug.model.cart

import intech.co.starbug.model.ProductModel


// this model have data of the product
class DetailCartItem(
    var cartItemModel: CartItemModel,
    var product: ProductModel? = null
): CartItemModel(cartItemModel){

    fun getProductPrice(): Int{
        if(product != null)
            return cartItemModel.getProductPrice(product!!)
        return 0
    }
}