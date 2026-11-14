package com.selfcontrol.monitor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Telefon yoqilganda "yoqildi" xabarini belgilaydi. Accessibility xizmati
 * (yoqilgan bo'lsa) tizim tomonidan avtomatik ishga tushadi va xabarni yuboradi.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Prefs.load(context)
        if (AppState.botToken.isNotEmpty() && AppState.chatId.isNotEmpty()) {
            Prefs.setFlag(context, "pendingBoot", true)
        }
    }
}

/** Telefon o'chishidan oldin (imkon qadar — ishonchli emas). */
class ShutdownReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val pending = goAsync()
        Thread {
            try {
                Prefs.load(context)
                if (Net.online(context) && AppState.chatId.isNotEmpty()) {
                    Telegram.send("📴 Telefon o'chmoqda.")
                }
            } catch (e: Exception) { } finally {
                pending.finish()
            }
        }.start()
    }
}

/** Har kuni 23:59: kunlik hisobot + jonli hisobni nollash + keyingi alarm. */
class DailyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val pending = goAsync()
        Thread {
            try {
                Prefs.load(context)
                if (Net.online(context)) {
                    val txt = Bot.summaryForDay(context, 0, "📅 Bugungi yakun")
                    Telegram.send(txt)
                }
                AppState.usageMs.clear()
                AppState.limitLocked.clear()
                AppState.notified.clear()
                Prefs.save(context)
                Bot.scheduleDailyAlarm(context)
            } catch (e: Exception) { } finally {
                pending.finish()
            }
        }.start()
    }
}
