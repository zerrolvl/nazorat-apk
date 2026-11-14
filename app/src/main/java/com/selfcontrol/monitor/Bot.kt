package com.selfcontrol.monitor

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Telegram mantiqi (menyu, buyruqlar, kunlik hisobot). Hech qanday xizmatga
 * bog'liq emas — Accessibility xizmati shu obyekt funksiyalarini chaqiradi.
 */
object Bot {

    fun isAdmin(fromId: String): Boolean {
        if (AppState.adminId.isEmpty()) {
            AppState.adminId = fromId   // birinchi murojaat qilgan = admin
            return true
        }
        return fromId == AppState.adminId
    }

    fun handleUpdate(ctx: Context, up: JSONObject) {
        val cb = up.optJSONObject("callback_query")
        if (cb != null) {
            val fromId = cb.optJSONObject("from")?.optLong("id")?.toString() ?: return
            val msg = cb.optJSONObject("message")
            val chat = msg?.optJSONObject("chat")
            if (AppState.adminId.isEmpty() && chat != null) {
                AppState.chatId = chat.optLong("id").toString()
            }
            if (!isAdmin(fromId)) return
            val cbId = cb.optString("id", "")
            val data = cb.optString("data", "")
            val messageId = msg?.optLong("message_id") ?: 0L
            handleCallback(ctx, data, messageId, cbId)
            return
        }

        val msg = up.optJSONObject("message") ?: return
        val fromId = msg.optJSONObject("from")?.optLong("id")?.toString() ?: return
        val chatIdNow = msg.optJSONObject("chat")?.optLong("id")?.toString() ?: ""

        val firstContact = AppState.adminId.isEmpty()
        if (firstContact && chatIdNow.isNotEmpty()) AppState.chatId = chatIdNow
        if (!isAdmin(fromId)) return
        if (AppState.chatId.isEmpty()) AppState.chatId = chatIdNow

        if (firstContact) Telegram.send("Ulandi ✅ Endi faqat shu akkaunt boshqaradi.")

        val text = msg.optString("text", "")
        if (text.isNotEmpty()) handleCommand(ctx, text)
    }

    // ---------------- Tugma bosilganda ----------------

    fun handleCallback(ctx: Context, data: String, messageId: Long, cbId: String) {
        when {
            data == "menu" -> { val (t, m) = Menu.main(); Telegram.edit(messageId, t, m); Telegram.answer(cbId, null) }
            data == "today" -> { val (t, m) = Menu.today(ctx); Telegram.edit(messageId, t, m); Telegram.answer(cbId, null) }
            data == "week" -> { val (t, m) = Menu.week(ctx); Telegram.edit(messageId, t, m); Telegram.answer(cbId, null) }
            data == "apps" -> { val (t, m) = Menu.apps(ctx); Telegram.edit(messageId, t, m); Telegram.answer(cbId, null) }

            data == "lockall" -> {
                lockAll(ctx); Prefs.save(ctx)
                val (t, m) = Menu.main(); Telegram.edit(messageId, t, m)
                Telegram.answer(cbId, "Hammasi qulflandi 🔒")
            }
            data == "unlockall" -> {
                AppState.manualLocked.clear(); AppState.limitLocked.clear(); Prefs.save(ctx)
                val (t, m) = Menu.main(); Telegram.edit(messageId, t, m)
                Telegram.answer(cbId, "Hammasi ochildi 🔓")
            }
            data == "guard" -> {
                AppState.guard = !AppState.guard; Prefs.save(ctx)
                val (t, m) = Menu.main(); Telegram.edit(messageId, t, m)
                Telegram.answer(cbId, if (AppState.guard) "Qo'riqchi yoqildi" else "Qo'riqchi o'chdi")
            }
            data.startsWith("app:") -> {
                val pkg = data.substring(4)
                val (t, m) = Menu.appDetail(ctx, pkg); Telegram.edit(messageId, t, m); Telegram.answer(cbId, null)
            }
            data.startsWith("toggle:") -> {
                val pkg = data.substring(7)
                if (AppState.isBlocked(pkg)) {
                    AppState.manualLocked.remove(pkg); AppState.limitLocked.remove(pkg)
                } else {
                    AppState.manualLocked.add(pkg)
                }
                Prefs.save(ctx)
                val (t, m) = Menu.appDetail(ctx, pkg); Telegram.edit(messageId, t, m)
                Telegram.answer(cbId, if (AppState.isBlocked(pkg)) "Qulflandi" else "Ochildi")
            }
            else -> Telegram.answer(cbId, null)
        }
    }

    fun lockAll(ctx: Context) {
        AppState.manualLocked.addAll(AppState.limitsMin.keys)
        for ((pkg, _) in Stats.topUserApps(ctx, 0, 30)) AppState.manualLocked.add(pkg)
    }

    // ---------------- Matnli buyruqlar ----------------

    fun handleCommand(ctx: Context, raw: String) {
        val parts = raw.trim().split(Regex("\\s+"))
        val cmd = parts[0].lowercase(Locale.ROOT).removePrefix("/")
        when (cmd) {
            "start", "menu", "help" -> { val (t, m) = Menu.main(); Telegram.sendMenu(t, m) }
            "stats", "status" -> Telegram.send(statsText(ctx))
            "lock" -> {
                val pkg = resolve(parts.getOrNull(1))
                if (pkg == null) { lockAll(ctx); Telegram.send("Hammasi qulflandi 🔒") }
                else { AppState.manualLocked.add(pkg); Telegram.send("$pkg qulflandi 🔒") }
            }
            "unlock" -> {
                val pkg = resolve(parts.getOrNull(1))
                if (pkg == null) { AppState.manualLocked.clear(); AppState.limitLocked.clear(); Telegram.send("Hammasi ochildi 🔓") }
                else { AppState.manualLocked.remove(pkg); AppState.limitLocked.remove(pkg); Telegram.send("$pkg ochildi 🔓") }
            }
            "limit" -> {
                val pkg = resolve(parts.getOrNull(1)); val min = parts.getOrNull(2)?.toIntOrNull()
                if (pkg != null && min != null) { AppState.limitsMin[pkg] = min; Telegram.send("$pkg limit: $min daq") }
                else Telegram.send("Namuna: /limit instagram 60")
            }
            "add" -> {
                val pkg = parts.getOrNull(1); val min = parts.getOrNull(2)?.toIntOrNull() ?: 60
                if (pkg != null) { AppState.limitsMin[pkg] = min; Telegram.send("Qo'shildi: $pkg ($min daq)") }
                else Telegram.send("Namuna: /add com.whatsapp 30")
            }
            "remove" -> {
                val pkg = resolve(parts.getOrNull(1))
                if (pkg != null) {
                    AppState.limitsMin.remove(pkg); AppState.usageMs.remove(pkg)
                    AppState.manualLocked.remove(pkg); AppState.limitLocked.remove(pkg)
                    Telegram.send("Olib tashlandi: $pkg")
                }
            }
            "reset" -> { AppState.usageMs.clear(); AppState.limitLocked.clear(); AppState.notified.clear(); Telegram.send("Bugungi hisob nollandi") }
            "guard" -> {
                when (parts.getOrNull(1)?.lowercase(Locale.ROOT)) {
                    "on" -> { AppState.guard = true; Telegram.send("Qo'riqchi YOQILDI 🛡") }
                    "off" -> { AppState.guard = false; Telegram.send("Qo'riqchi O'CHIRILDI") }
                    else -> Telegram.send("Qo'riqchi: " + if (AppState.guard) "yoqilgan" else "o'chirilgan")
                }
            }
            else -> { val (t, m) = Menu.main(); Telegram.sendMenu(t, m) }
        }
        Prefs.save(ctx)
    }

    private fun resolve(name: String?): String? {
        if (name == null) return null
        val n = name.lowercase(Locale.ROOT)
        return when (n) {
            "instagram", "insta", "ig" -> "com.instagram.android"
            "roblox" -> "com.roblox.client"
            else -> if (n.contains(".")) name else null
        }
    }

    fun statsText(ctx: Context): String = buildString {
        append("📊 Bugun:\n\n")
        for ((pkg, ms) in Stats.topUserApps(ctx, 0, 8)) {
            val mark = if (AppState.isBlocked(pkg)) "🔒" else "🟢"
            append("$mark ${Stats.label(ctx, pkg)} — ${ms / 60000L} daq\n")
        }
        append("\n📱 Qurilma jami: ${Stats.deviceMinutes(ctx, 0)} daq")
    }

    // ---------------- Kunlik hisobot / nollash / alarm ----------------

    fun checkDayRollover(ctx: Context) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(System.currentTimeMillis())
        if (AppState.dayKey.isEmpty()) { AppState.dayKey = today; Prefs.save(ctx); return }
        if (AppState.dayKey != today) {
            if (Net.online(ctx)) {
                val txt = summaryForDay(ctx, 1, "📅 Kecha yakuni")
                Net.bg { Telegram.send(txt) }
            }
            AppState.usageMs.clear(); AppState.limitLocked.clear(); AppState.notified.clear()
            AppState.dayKey = today
            Prefs.save(ctx)
        }
    }

    /** offset=0 bugun, 1 kecha ... uchun hisobot matni. */
    fun summaryForDay(ctx: Context, offset: Int, title: String): String = buildString {
        append("$title:\n\n")
        val top = Stats.topUserApps(ctx, offset, 6)
        if (top.isEmpty()) append("Ma'lumot yo'q\n")
        for ((pkg, ms) in top) append("${Stats.label(ctx, pkg)}: ${ms / 60000L} daq\n")
        append("\nJami: ${Stats.deviceMinutes(ctx, offset)} daq")
    }

    /** Kunlik 23:59 alarm (noaniq — Android 14+ da ruxsat talab qilmaydi). */
    fun scheduleDailyAlarm(ctx: Context) {
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = PendingIntent.getBroadcast(
            ctx, 100, Intent(ctx, DailyReceiver::class.java),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            else PendingIntent.FLAG_UPDATE_CURRENT
        )
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 0)
        if (cal.timeInMillis <= System.currentTimeMillis()) cal.add(Calendar.DAY_OF_YEAR, 1)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
            else
                am.set(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
        } catch (e: Exception) { }
    }
}
