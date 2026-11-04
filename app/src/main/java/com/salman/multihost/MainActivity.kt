package com.salman.multihost

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 96, 48, 48)
        }

        root.addView(TextView(this).apply {
            text = "Hedef: ${CloneApp.TARGET_PACKAGE}\n\n" +
                "Her buton, hedef uygulamayı ayrı bir process'te,\n" +
                "kendi izole dosya alanıyla başlatır."
            textSize = 16f
        })

        root.addView(Button(this).apply {
            text = "Kopya 1'i başlat"
            setOnClickListener {
                startActivity(Intent(this@MainActivity, LauncherSlotActivity::class.java))
            }
        })

        root.addView(Button(this).apply {
            text = "Kopya 2'yi başlat"
            setOnClickListener {
                startActivity(Intent(this@MainActivity, LauncherSlotActivity2::class.java))
            }
        })

        setContentView(root)
    }
}
