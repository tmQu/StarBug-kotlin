package intech.co.starbug

import android.app.Application
import intech.co.starbug.database.StarbugDatabase

class StarbugApp: Application() {
    val dbSQLite by lazy {
        StarbugDatabase.getInstance(this)
    }
}