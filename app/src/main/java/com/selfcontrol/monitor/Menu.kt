package com.selfcontrol.monitor

import android.content.Context
import org.json.JSONObject

/** Telegram inline menyularini (matn + tugmalar) yasaydi. */
object Menu {

    private fun fmt(min: Long): String {
        if (min < 60) return "$min daq"
        val h = min / 60
        val m = min % 60
        return if (m == 0L) "$h soat" else "$h soat $m daq"
    }

    private fun lockMark(pkg: String): String = if (AppState.isBlocked(pkg)) "🔒" else "🟢"

    /** Asosiy menyu. */
    fun main(): Pair<String, JSONObject> {
        val text = "🏠 Ota-ona nazorati\n\nQuyidagidan tanlang:"
        val rows = listOf(
            listOf(Pair("📊 Bugun", "today"), Pair("📅 7 kunlik", "week")),
            listOf(Pair("📱 Ilovalar", "apps")),
            listOf(Pair("🔒 Hammasini qulflash", "lockall"), Pair("🔓 Hammasini ochish", "unlockall")),
            listOf(Pair(if (AppState.guard) "🛡 Qo'riqchi: ON" else "🛡 Qo'riqchi: OFF", "guard"))
        )
        return Pair(text, Telegram.kb(rows))
    }

    /** Bugungi batafsil (kuzatilayotgan ilovalar + qurilma umumiy). */
    fun today(ctx: Context): Pair<String, JSONObject> {
        val sb = StringBuilder("📊 Bugun\n\n")
        val top = Stats.topUserApps(ctx, 0, 8)
        if (top.isEmpty()) {
            sb.append("Hali ma'lumot yo'q.\n")
        } else {
            for ((pkg, ms) in top) {
                sb.append("${lockMark(pkg)} ${Stats.label(ctx, pkg)} — ${fmt(ms / 60000L)}\n")
            }
        }
        sb.append("\n📱 Qurilma jami: ${fmt(Stats.deviceMinutes(ctx, 0))}")
        val rows = listOf(listOf(Pair("⬅️ Orqaga", "menu")))
        return Pair(sb.toString(), Telegram.kb(rows))
    }

    /** Oxirgi 7 kun (qurilma umumiy + eng ko'p ilova). */
    fun week(ctx: Context): Pair<String, JSONObject> {
        val sb = StringBuilder("📅 Oxirgi 7 kun\n\n")
        for (off in 0..6) {
            val dev = Stats.deviceMinutes(ctx, off)
            val top = Stats.topUserApps(ctx, off, 1).firstOrNull()
            val topStr = if (top != null) "  (eng ko'p: ${Stats.label(ctx, top.first)})" else ""
            sb.append("${Stats.dayLabel(off)}: ${fmt(dev)}$topStr\n")
        }
        val rows = listOf(listOf(Pair("⬅️ Orqaga", "menu")))
        return Pair(sb.toString(), Telegram.kb(rows))
    }

    /** Ilovalar ro'yxati (eng ko'p ishlatilgan foydalanuvchi ilovalari). */
    fun apps(ctx: Context): Pair<String, JSONObject> {
        val top = Stats.topUserApps(ctx, 0, 14)
        val rows = ArrayList<List<Pair<String, String>>>()
        if (top.isEmpty()) {
            rows.add(listOf(Pair("Bugun ishlatilgan ilova yo'q", "menu")))
        } else {
            for ((pkg, _) in top) {
                if (("app:$pkg").toByteArray().size <= 64) {
                    rows.add(listOf(Pair("${lockMark(pkg)} ${Stats.label(ctx, pkg)}", "app:$pkg")))
                }
            }
        }
        rows.add(listOf(Pair("⬅️ Orqaga", "menu")))
        return Pair("📱 Ilovani tanlang (qulflash/ochish uchun):", Telegram.kb(rows))
    }

    /** Bitta ilova: bugun/kecha vaqti + qulf tugmasi. */
    fun appDetail(ctx: Context, pkg: String): Pair<String, JSONObject> {
        val name = Stats.label(ctx, pkg)
        val today = Stats.appMinutes(ctx, pkg, 0)
        val yest = Stats.appMinutes(ctx, pkg, 1)
        val limit = AppState.limitsMin[pkg]
        val sb = StringBuilder("${lockMark(pkg)} $name\n\n")
        sb.append("Bugun: ${fmt(today)}\n")
        sb.append("Kecha: ${fmt(yest)}\n")
        if (limit != null) sb.append("Kunlik limit: $limit daq\n")
        sb.append("\nHolat: ${if (AppState.isBlocked(pkg)) "QULFLANGAN 🔒" else "ochiq 🟢"}")

        val toggle = if (AppState.isBlocked(pkg))
            Pair("🔓 Ochish", "toggle:$pkg")
        else
            Pair("🔒 Qulflash", "toggle:$pkg")

        val rows = listOf(
            listOf(toggle),
            listOf(Pair("⬅️ Ilovalar", "apps"), Pair("🏠 Menyu", "menu"))
        )
        return Pair(sb.toString(), Telegram.kb(rows))
    }
}
