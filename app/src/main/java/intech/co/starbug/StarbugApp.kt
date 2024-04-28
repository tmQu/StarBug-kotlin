package intech.co.starbug

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import intech.co.starbug.database.StarbugDatabase

class StarbugApp: Application() {
    private val channel_id = "starbug"
    val dbSQLite by lazy {
        StarbugDatabase.getInstance(this)
    }

    override fun onCreate() {
        super.onCreate()
        createChannelNotification()
    }

    private fun createChannelNotification() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channel_id,
                "Starbug",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}