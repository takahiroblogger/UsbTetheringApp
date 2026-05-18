package com.example.usbtetheringapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 起動と同時にテザリング設定画面を開く
        try {
            val intent = Intent()
            intent.setClassName(
                "com.android.settings",
                "com.android.settings.TetherSettings"
            )
            startActivity(intent)
        } catch (e: Exception) {
            // 一部メーカー端末用フォールバック
            val intent = Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS)
            startActivity(intent)
        }

        // このActivityは不要なので閉じる
        finish()
    }
}
