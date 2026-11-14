package com.selfcontrol.monitor

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Bosh dvigatel. Accessibility xizmati Android 8–16 da doim tirik turadi va
 * foreground service / bildirishnoma talab qilmaydi. Shu yerda:
 *  - Telegram poll sikli (buyruqlar/menyu)
 *  - foydalanish vaqti hisobi + limit/qulf
 *  - force-stop sahifasidan himoya (qo'riqchi)
 *  - kunlik alarm va "telefon yoqildi" xabari
 */
class AppBlockerService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private var ticks = 0

    @Volatile private var polling = false
    private var pollThread: Thread? = null

    // --- har 3 soniyada: vaqtni hisoblash + qulfni ushlab turish ---
    private val tick = object : Runnable {
        override fun run() {
            val now = System.currentTimeMillis()
            val locked = AppState.accrue(now)
            if (locked != null && !AppState.notified.contains(locked)) {
                AppState.notified.add(locked)
                Net.bg { Telegram.send("⛔ $locked kunlik limitga yetdi — quluflandi.") }
            }
            if (AppState.isBlocked(AppState.currentForeground)) {
                performGlobalAction(GLOBAL_ACTION_HOME)
            }
            ticks++
            if (ticks % 15 == 0) {
                val ctx = applicationContext
                Net.bg { Prefs.save(ctx) }
            }
            handler.postDelayed(this, 1000)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Prefs.load(this)
        AppState.lastTick = System.currentTimeMillis()
        Bot.scheduleDailyAlarm(this)
        Bot.checkDayRollover(this)
        startPolling()
        handler.postDelayed(tick, 1000)
    }

    private fun startPolling() {
        if (polling) return
        polling = true
        pollThread = Thread {
            while (polling) {
                try {
                    if (AppState.botToken.isEmpty()) { Thread.sleep(5000); continue }
                    if (!Net.online(this)) { Thread.sleep(8000); continue }

                    // Telefon yoqilgani xabari (internet qaytganda)
                    if (Prefs.getFlag(this, "pendingBoot", false) && AppState.chatId.isNotEmpty()) {
                        Telegram.send("📱 Telefon yoqildi.")
                        Prefs.setFlag(this, "pendingBoot", false)
                    }

                    val updates = Telegram.getUpdates()
                    if (updates == null) {
                        Thread.sleep(3000)
                    } else {
                        for (i in 0 until updates.length()) {
                            val up = updates.getJSONObject(i)
                            AppState.offset = up.getLong("update_id") + 1
                            Bot.handleUpdate(this, up)
                        }
                        Prefs.save(this)
                    }
                    Bot.checkDayRollover(this)
                } catch (e: Exception) {
                    try { Thread.sleep(5000) } catch (ie: InterruptedException) { }
                }
            }
        }
        pollThread?.isDaemon = true
        pollThread?.start()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName) return

        val now = System.currentTimeMillis()
        val locked = AppState.accrue(now)
        if (locked != null && !AppState.notified.contains(locked)) {
            AppState.notified.add(locked)
            Net.bg { Telegram.send("⛔ $locked kunlik limitga yetdi — quluflandi.") }
        }
        AppState.currentForeground = pkg
        AppState.lastTick = now

        if (AppState.isBlocked(pkg)) {
            performGlobalAction(GLOBAL_ACTION_HOME)
            return
        }

        // Qo'riqchi: o'z ilova sahifamiz (force-stop/o'chirish) ochilsa -> Home.
        // /guard off (Telegram) bilan o'chiriladi.
        if (AppState.guard && AppState.chatId.isNotEmpty() && isSettingsLike(pkg)) {
            if (windowMentionsSelf()) performGlobalAction(GLOBAL_ACTION_HOME)
        }
    }

    private fun isSettingsLike(pkg: String): Boolean {
        return pkg.contains("settings") || pkg.contains("packageinstaller")
    }

    private fun windowMentionsSelf(): Boolean {
        val root = rootInActiveWindow ?: return false
        return try {
            scan(root, "com.selfcontrol.monitor", intArrayOf(0))
        } catch (e: Exception) {
            false
        }
    }

    private fun scan(node: AccessibilityNodeInfo?, needle: String, count: IntArray): Boolean {
        if (node == null) return false
        if (count[0] > 400) return false
        count[0]++
        val t = node.text?.toString()
        if (t != null && t.contains(needle, true)) return true
        val d = node.contentDescription?.toString()
        if (d != null && d.contains(needle, true)) return true
        for (i in 0 until node.childCount) {
            if (scan(node.getChild(i), needle, count)) return true
        }
        return false
    }

    override fun onInterrupt() { }

    override fun onDestroy() {
        super.onDestroy()
        polling = false
        handler.removeCallbacks(tick)
    }
}
