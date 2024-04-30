package intech.co.starbug.model.cart

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import intech.co.starbug.model.ProductModel
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "CartItem")
open class CartItemModel(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,

    @ColumnInfo(name = "productId")
    var productId: String = "",

    @ColumnInfo(name = "quantity")
    var quantity: Int = 1,

    @ColumnInfo(name = "size")
    var size: String = "",

    @ColumnInfo(name = "temperature")
    var temperature: String = "",

    @ColumnInfo(name = "amountIce")
    var amountIce: String = "",

    @ColumnInfo(name = "amountSugar")
    var amountSugar: String = ""
): Parcelable {
    constructor(cartItem: CartItemModel) : this(
        cartItem.id,
        cartItem.productId,
        cartItem.quantity,
        cartItem.size,
        cartItem.temperature,
        cartItem.amountIce,
        cartItem.amountSugar
    )

    fun getProductPrice(product: ProductModel): Int
    {
        when(size)
        {
            "S" -> return product.price
            "M" -> return product.medium_price
            "L" -> return product.large_price
        }
        return 0
    }
}