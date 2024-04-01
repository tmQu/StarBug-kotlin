package intech.co.starbug.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import intech.co.starbug.model.cart.CartItemDAO
import intech.co.starbug.model.cart.CartItemModel


@Database(
    entities = [CartItemModel::class],
    version = 1,
    exportSchema = false
)
abstract class StarbugDatabase: RoomDatabase() {
    abstract fun cartItemDAO(): CartItemDAO

    companion object {
        @Volatile   private var inastance: StarbugDatabase? =null

        fun getInstance(context: Context): StarbugDatabase {
            return inastance ?: synchronized(this){
                inastance ?: BuildDatabase(context).also {
                    inastance=it
                }
            }
        }
        private fun BuildDatabase(context: Context)= Room.databaseBuilder(context.applicationContext,StarbugDatabase::class.java,"StarBugDB")
                                                            .fallbackToDestructiveMigration()
                                                            .build()
    }
}