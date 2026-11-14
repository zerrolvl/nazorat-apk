package com.selfcontrol.monitor

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import java.util.Calendar

/**
 * Statistika UsageStatsManager'dan olinadi (tizim o'zi bir necha kunlik
 * ma'lumotni saqlaydi — shuning uchun 7 kunlik tarix ishonchli bo'ladi).
 * Jonli limit/qulf esa AppState.accrue orqali real vaqtda ishlaydi.
 */
object Stats {

    private const val DAY = 24L * 60L * 60L * 1000L

    /** Bugungi kunning 00:00 i (millisekund). O'zbekistonda yozgi vaqt yo'q. */
    fun startOfToday(): Long {
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    /** offset=0 bugun, 1 kecha, 2 avvalgi kun ... (boshlanish vaqti). */
    fun dayStart(offset: Int): Long = startOfToday() - offset * DAY

    /** Bir kunlik foydalanish: paket -> millisekund (faqat > 0). */
    fun usageForDay(ctx: Context, offset: Int): Map<String, Long> {
        return try {
            val usm = ctx.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val start = dayStart(offset)
            var end = start + DAY
            val now = System.currentTimeMillis()
            if (end > now) end = now
            val map = usm.queryAndAggregateUsageStats(start, end)
            val out = HashMap<String, Long>()
            if (map != null) {
                for ((k, v) in map) {
                    val f = v.totalTimeInForeground
                    if (f > 0) out[k] = f
                }
            }
            out
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /** Bitta ilovaning bir kundagi daqiqasi. */
    fun appMinutes(ctx: Context, pkg: String, offset: Int): Long {
        return (usageForDay(ctx, offset)[pkg] ?: 0L) / 60000L
    }

    /** Ilova nomi (yorlig'i). */
    fun label(ctx: Context, pkg: String): String {
        return try {
            val pm = ctx.packageManager
            pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
        } catch (e: Exception) {
            pkg
        }
    }

    /** Tizim ilovasi emasmi (foydalanuvchi o'rnatganmi)? */
    fun isUserApp(ctx: Context, pkg: String): Boolean {
        return try {
            val ai = ctx.packageManager.getApplicationInfo(pkg, 0)
            (ai.flags and ApplicationInfo.FLAG_SYSTEM) == 0 &&
                (ai.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0
        } catch (e: Exception) {
            false
        }
    }

    /** Eng ko'p ishlatilgan foydalanuvchi ilovalari (o'z paketimizdan tashqari). */
    fun topUserApps(ctx: Context, offset: Int, limit: Int): List<Pair<String, Long>> {
        val day = usageForDay(ctx, offset)
        return day.entries
            .filter { it.key != ctx.packageName && isUserApp(ctx, it.key) }
            .sortedByDescending { it.value }
            .take(limit)
            .map { Pair(it.key, it.value) }
    }

    /** Qurilmaning bir kundagi umumiy ekran vaqti (daqiqa). */
    fun deviceMinutes(ctx: Context, offset: Int): Long {
        var total = 0L
        for ((_, v) in usageForDay(ctx, offset)) total += v
        return total / 60000L
    }

    fun dayLabel(offset: Int): String {
        val c = Calendar.getInstance()
        c.timeInMillis = dayStart(offset)
        val d = c.get(Calendar.DAY_OF_MONTH)
        val m = c.get(Calendar.MONTH) + 1
        val name = when (offset) {
            0 -> "Bugun"
            1 -> "Kecha"
            else -> "$d.$m"
        }
        return name
    }
}
