package com.selfcontrol.monitor

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * Faqat sozlash ekrani: bot tokenini kiritish va ruxsatlarni berish.
 * Bu yerda ATAYLAB qulf/ochish tugmasi yo'q — ochish faqat Telegram orqali.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Prefs.load(this)

        val token = findViewById<EditText>(R.id.token)
        token.setText(AppState.botToken)

        findViewById<Button>(R.id.save).setOnClickListener {
            AppState.botToken = token.text.toString().trim()
            Prefs.save(this)
            Toast.makeText(
                this,
                "Saqlandi. Pastdagi ruxsatlarni bering (ayniqsa Accessibility), so'ng botga /start yuboring.",
                Toast.LENGTH_LONG
            ).show()
        }

        findViewById<Button>(R.id.usage).setOnClickListener {
            safeStart(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
        findViewById<Button>(R.id.acc).setOnClickListener {
            safeStart(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        findViewById<Button>(R.id.overlay).setOnClickListener {
            safeStart(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
            )
        }
        findViewById<Button>(R.id.battery).setOnClickListener {
            try {
                safeStart(
                    Intent(
                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        Uri.parse("package:$packageName")
                    )
                )
            } catch (e: Exception) {
                safeStart(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            }
        }

        findViewById<Button>(R.id.hide).setOnClickListener {
            Icon.setHidden(this, true)
            Toast.makeText(
                this,
                "Ikonka yashirildi. Qaytarish uchun Telegramga /show yozing.",
                Toast.LENGTH_LONG
            ).show()
            finish()
        }
    }

    private fun safeStart(i: Intent) {
        try {
            startActivity(i)
        } catch (e: Exception) {
            Toast.makeText(this, "Bu ekran bu telefonda topilmadi", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        findViewById<TextView>(R.id.status).text =
            "Token: " + (if (AppState.botToken.isEmpty()) "yo'q" else "bor") +
            "\nChat ID: " + (if (AppState.chatId.isEmpty()) "hali ulanmagan" else AppState.chatId) +
            "\nKuzatilayotgan: " + AppState.limitsMin.keys.joinToString(", ")
    }
}
