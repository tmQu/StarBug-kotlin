package intech.co.starbug.model.cart

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "CartItem")
data class CartItemModel(

    @ColumnInfo(name = "productId")
    var productId: String = "",

    @ColumnInfo(name = "typeProduct")
    val typeProduct: String = "",

    @ColumnInfo(name = "quantity")
    var quantity: Int = 1,

    @ColumnInfo(name = "size")
    var size: String = "",

    @ColumnInfo(name = "temperature")
    var temperature: String = "",

    @ColumnInfo(name = "amountIce")
    var amountIce: String = "",

    @ColumnInfo(name = "amountSugar")
    var amountSugar: String = "",

    @ColumnInfo(name = "product_price")
    var productPrice: Int = 0
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L
}