package intech.co.starbug.model.cart

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
abstract class CartItemDAO {
    @Query("Select * from CartItem")
    abstract fun findAllCartItem(): Flow<List<CartItemModel>>

    @Query("Select * from CartItem where id=:id")
    abstract fun findCartItemById(id: Long): Flow<CartItemModel>

    @Insert
    abstract suspend fun insertCartItem(cartItem: CartItemModel)

    @Update
    abstract suspend fun updateCartItem(cartItem: CartItemModel)

    @Delete
    abstract suspend fun deleteCartItem(cartItem: CartItemModel)

    @Query("Select * from CartItem where id!=:id and productId=:productId and size=:size and temperature=:temperature and amountSugar=:amountSugar and amountIce=:amountIce")
    abstract fun isExistCartItem(id: Long, productId: String, size: String, temperature: String, amountSugar: String, amountIce: String): CartItemModel?
}