package com.example.usbtetheringapp

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.lang.reflect.Method

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var toggleButton: Button
    private lateinit var settingsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        toggleButton = findViewById(R.id.toggleButton)
        settingsButton = findViewById(R.id.settingsButton)

        updateStatus()

        toggleButton.setOnClickListener {
            toggleUsbTethering()
        }

        settingsButton.setOnClickListener {
            openTetherSettings()
        }
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    /**
     * USBテザリングの現在の状態を取得して表示を更新
     */
    private fun updateStatus() {
        val isEnabled = isUsbTetheringEnabled()
        if (isEnabled) {
            statusText.text = "🟢 USBテザリング: オン"
            statusText.setTextColor(getColor(R.color.status_on))
            toggleButton.text = "タップしてオフにする"
            toggleButton.setBackgroundColor(getColor(R.color.button_off))
        } else {
            statusText.text = "🔴 USBテザリング: オフ"
            statusText.setTextColor(getColor(R.color.status_off))
            toggleButton.text = "タップしてオンにする"
            toggleButton.setBackgroundColor(getColor(R.color.button_on))
        }
    }

    /**
     * リフレクションでUSBテザリングの状態を確認
     */
    private fun isUsbTetheringEnabled(): Boolean {
        return try {
            val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val method: Method = cm.javaClass.getDeclaredMethod("getTetheredIfaces")
            method.isAccessible = true
            val result = method.invoke(cm) as Array<*>
            result.any { iface ->
                iface.toString().contains("rndis", ignoreCase = true) ||
                iface.toString().contains("usb", ignoreCase = true)
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * リフレクションでUSBテザリングをトグル
     * Android 9 では ConnectivityManager の隠しAPIを使用
     */
    private fun toggleUsbTethering() {
        val currentState = isUsbTetheringEnabled()
        val targetState = !currentState

        val success = trySetUsbTethering(targetState)

        if (success) {
            // 少し待ってから状態を更新（非同期で状態が変わるため）
            toggleButton.postDelayed({
                updateStatus()
            }, 1000)
            val msg = if (targetState) "USBテザリングをオンにしました" else "USBテザリングをオフにしました"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        } else {
            // 直接制御に失敗した場合はシステム設定を開く
            Toast.makeText(
                this,
                "直接制御できませんでした。設定画面を開きます。",
                Toast.LENGTH_LONG
            ).show()
            openTetherSettings()
        }
    }

    /**
     * リフレクションで setUsbTethering を呼び出す
     */
    private fun trySetUsbTethering(enable: Boolean): Boolean {
        return try {
            val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val method: Method = cm.javaClass.getDeclaredMethod("setUsbTethering", Boolean::class.java)
            method.isAccessible = true
            val result = method.invoke(cm, enable)
            // 戻り値は ConnectivityManager.TETHER_ERROR_NO_ERROR (0) が成功
            result == 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * テザリング設定画面を開く（フォールバック）
     */
    private fun openTetherSettings() {
        try {
            // Androidの隠し設定アクティビティを直接起動
            val intent = Intent()
            intent.setClassName(
                "com.android.settings",
                "com.android.settings.TetherSettings"
            )
            startActivity(intent)
        } catch (e: Exception) {
            try {
                // フォールバック: 一般的なワイヤレス設定を開く
                startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
            } catch (e2: Exception) {
                Toast.makeText(this, "設定画面を開けませんでした", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
