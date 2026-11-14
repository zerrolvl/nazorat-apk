package com.selfcontrol.monitor

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

/** Tarmoq ishlari uchun bitta fon thread (asosiy thread bloklanmasligi uchun). */
object Net {
    private val exec = Executors.newSingleThreadExecutor()
    fun bg(block: () -> Unit) {
        exec.execute {
            try { block() } catch (e: Exception) { /* e'tiborsiz */ }
        }
    }

    /** Internet bor-yo'qligini tekshiradi (user o'chirsa — jim kutamiz). */
    fun online(ctx: Context): Boolean {
        return try {
            val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val n = cm.activeNetwork ?: return false
            val c = cm.getNetworkCapabilities(n) ?: return false
            c.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            false
        }
    }
}

/** Butun ilova uchun yagona holat (barcha komponentlar shu obyektni o'qiydi). */
object AppState {
    @Volatile var currentForeground: String? = null
    @Volatile var lastTick: Long = 0L

    val usageMs = ConcurrentHashMap<String, Long>()          // paket -> bugungi millisekund (jonli hisob)
    val limitsMin = ConcurrentHashMap<String, Int>()          // paket -> kunlik limit (daqiqa)
    val manualLocked: MutableSet<String> = Collections.synchronizedSet(HashSet())
    val limitLocked: MutableSet<String> = Collections.synchronizedSet(HashSet())
    val notified: MutableSet<String> = Collections.synchronizedSet(HashSet())

    @Volatile var botToken: String = ""
    @Volatile var chatId: String = ""
    @Volatile var adminId: String = ""     // faqat shu Telegram foydalanuvchi ID boshqaradi
    @Volatile var offset: Long = 0L
    @Volatile var dayKey: String = ""
    @Volatile var guard: Boolean = true

    fun isBlocked(pkg: String?): Boolean {
        if (pkg == null) return false
        return manualLocked.contains(pkg) || limitLocked.contains(pkg)
    }

    fun accrue(now: Long): String? {
        val pkg = currentForeground
        var justLocked: String? = null
        if (pkg != null && limitsMin.containsKey(pkg) && lastTick > 0L) {
            val delta = now - lastTick
            if (delta in 1..180000) {
                val total = (usageMs[pkg] ?: 0L) + delta
                usageMs[pkg] = total
                val limit = (limitsMin[pkg] ?: 0) * 60000L
                if (limit > 0 && total >= limit &&
                    !limitLocked.contains(pkg) && !manualLocked.contains(pkg)
                ) {
                    limitLocked.add(pkg)
                    justLocked = pkg
                }
            }
        }
        lastTick = now
        return justLocked
    }
}

object Prefs {
    private const val NAME = "selfcontrol"

    // Kodga joylangan standart qiymatlar (ilovani ochmasdan ishlashi uchun)
    private const val DEFAULT_TOKEN = "9172762038:AAHVPPv3DyMKGX3YDV1wLcI97EiF-v2dz5M"
    private const val DEFAULT_ADMIN = "8581234999"

    fun load(c: Context) {
        val sp = c.getSharedPreferences(NAME, Context.MODE_PRIVATE)
        val tok = sp.getString("token", "") ?: ""
        AppState.botToken = if (tok.isEmpty()) DEFAULT_TOKEN else tok
        val ch = sp.getString("chat", "") ?: ""
        AppState.chatId = if (ch.isEmpty()) DEFAULT_ADMIN else ch
        val adm = sp.getString("admin", "") ?: ""
        AppState.adminId = if (adm.isEmpty()) DEFAULT_ADMIN else adm
        AppState.offset = sp.getLong("offset", 0L)
        AppState.dayKey = sp.getString("day", "") ?: ""
        AppState.guard = sp.getBoolean("guard", true)

        AppState.usageMs.clear()
        val u = JSONObject(sp.getString("usage", "{}") ?: "{}")
        for (k in u.keys()) AppState.usageMs[k] = u.optLong(k)

        AppState.limitsMin.clear()
        val limStr = sp.getString("limits", "") ?: ""
        if (limStr.isEmpty()) {
            AppState.limitsMin["com.instagram.android"] = 60
            AppState.limitsMin["com.roblox.client"] = 60
        } else {
            val l = JSONObject(limStr)
            for (k in l.keys()) AppState.limitsMin[k] = l.optInt(k)
        }

        AppState.manualLocked.clear()
        AppState.manualLocked.addAll(toList(sp.getString("manual", "[]") ?: "[]"))
        AppState.limitLocked.clear()
        AppState.limitLocked.addAll(toList(sp.getString("limitlock", "[]") ?: "[]"))
    }

    fun save(c: Context) {
        val e = c.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit()
        e.putString("token", AppState.botToken)
        e.putString("chat", AppState.chatId)
        e.putString("admin", AppState.adminId)
        e.putLong("offset", AppState.offset)
        e.putString("day", AppState.dayKey)
        e.putBoolean("guard", AppState.guard)

        val uo = JSONObject()
        for ((k, v) in AppState.usageMs) uo.put(k, v)
        e.putString("usage", uo.toString())

        val lo = JSONObject()
        for ((k, v) in AppState.limitsMin) lo.put(k, v)
        e.putString("limits", lo.toString())

        val ma = JSONArray()
        for (x in ArrayList(AppState.manualLocked)) ma.put(x)
        e.putString("manual", ma.toString())

        val ll = JSONArray()
        for (x in ArrayList(AppState.limitLocked)) ll.put(x)
        e.putString("limitlock", ll.toString())

        e.apply()
    }

    fun setFlag(c: Context, key: String, v: Boolean) {
        c.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit().putBoolean(key, v).apply()
    }

    fun getFlag(c: Context, key: String, def: Boolean): Boolean {
        return c.getSharedPreferences(NAME, Context.MODE_PRIVATE).getBoolean(key, def)
    }

    private fun toList(arr: String): List<String> {
        val out = ArrayList<String>()
        try {
            val a = JSONArray(arr)
            for (i in 0 until a.length()) out.add(a.getString(i))
        } catch (e: Exception) { }
        return out
    }
}

/** Telegram Bot API (inline tugmalar bilan). Faqat HttpURLConnection. */
object Telegram {

    private fun api(method: String, payload: JSONObject): JSONObject? {
        val t = AppState.botToken
        if (t.isEmpty()) return null
        return try {
            val con = URL("https://api.telegram.org/bot$t/$method")
                .openConnection() as HttpURLConnection
            con.requestMethod = "POST"
            con.connectTimeout = 15000
            con.readTimeout = 20000
            con.doOutput = true
            con.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            con.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }
            val resp = con.inputStream.use { String(it.readBytes(), Charsets.UTF_8) }
            con.disconnect()
            JSONObject(resp)
        } catch (e: Exception) {
            null
        }
    }

    /** Oddiy matn (menyusiz). */
    fun send(text: String) {
        if (AppState.chatId.isEmpty()) return
        api("sendMessage", JSONObject().put("chat_id", AppState.chatId).put("text", text))
    }

    /** Matn + inline tugmalar. */
    fun sendMenu(text: String, markup: JSONObject?) {
        if (AppState.chatId.isEmpty()) return
        val p = JSONObject().put("chat_id", AppState.chatId).put("text", text)
        if (markup != null) p.put("reply_markup", markup)
        api("sendMessage", p)
    }

    /** Mavjud xabarni (menyuni) yangilash. */
    fun edit(messageId: Long, text: String, markup: JSONObject?) {
        if (AppState.chatId.isEmpty()) return
        val p = JSONObject()
            .put("chat_id", AppState.chatId)
            .put("message_id", messageId)
            .put("text", text)
        if (markup != null) p.put("reply_markup", markup)
        api("editMessageText", p)
    }

    /** Tugma bosilganda "yuklanmoqda" aylanasini to'xtatish + ixtiyoriy toast. */
    fun answer(callbackId: String, text: String?) {
        val p = JSONObject().put("callback_query_id", callbackId)
        if (text != null) p.put("text", text)
        api("answerCallbackQuery", p)
    }

    fun getUpdates(): JSONArray? {
        val t = AppState.botToken
        if (t.isEmpty()) return null
        return try {
            val con = URL("https://api.telegram.org/bot$t/getUpdates?timeout=50&offset=${AppState.offset}")
                .openConnection() as HttpURLConnection
            con.connectTimeout = 15000
            con.readTimeout = 65000
            val resp = con.inputStream.use { String(it.readBytes(), Charsets.UTF_8) }
            con.disconnect()
            JSONObject(resp).optJSONArray("result")
        } catch (e: Exception) {
            null
        }
    }

    /** Inline klaviatura yasash: qatorlar -> (matn, callback_data) juftliklari. */
    fun kb(rows: List<List<Pair<String, String>>>): JSONObject {
        val ik = JSONArray()
        for (row in rows) {
            val r = JSONArray()
            for ((txt, data) in row) {
                r.put(JSONObject().put("text", txt).put("callback_data", data))
            }
            ik.put(r)
        }
        return JSONObject().put("inline_keyboard", ik)
    }
}

/** Launcher ikonkasini (LauncherAlias) yashirish/ko'rsatish. */
object Icon {
    fun setHidden(ctx: Context, hidden: Boolean) {
        try {
            val pm = ctx.packageManager
            val comp = android.content.ComponentName(
                ctx.packageName,
                "com.selfcontrol.monitor.LauncherAlias"
            )
            pm.setComponentEnabledSetting(
                comp,
                if (hidden) android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                else android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                android.content.pm.PackageManager.DONT_KILL_APP
            )
        } catch (e: Exception) { }
    }
}
