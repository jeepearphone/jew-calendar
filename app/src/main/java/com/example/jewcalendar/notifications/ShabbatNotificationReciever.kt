import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.jewcalendar.R

class ShabbatNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra("notification_type") ?: return
        val message = intent.getStringExtra("message") ?: return

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "jewish_calendar_channel"

        if (nm.getNotificationChannel(channelId) == null) {
            val channel = NotificationChannel(
                channelId,
                "Еврейский календарь",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Уведомления о Шаббате и праздниках"
            }
            nm.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(
                when (type) {
                    "shabbat_soon" -> "Шаббат скоро"
                    "shabbat_start" -> "Шаббат Шалом! ✡️"
                    "holiday" -> "Праздник ✡️"
                    else -> "Еврейский календарь"
                }
            )
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        nm.notify(System.currentTimeMillis().toInt(), notification)
    }
}