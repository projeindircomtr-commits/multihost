package com.salman.multihost

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Bu Activity AndroidManifest'te ayrı bir process'te (:slot1) çalışacak
 * şekilde tanımlı. CloneApp.attachBaseContext bu process'te çalıştığında
 * Slot.current set edilmiş ve hedef uygulamanın class loader'ı devreye
 * girmiş olur.
 *
 * ÖNEMLİ SINIR: Burada sadece class loader yönlendirmesinin çalıştığını
 * doğruluyoruz (diagnostik). Hedef uygulamanın gerçek Activity'sini bu
 * process İÇİNDE, kendi lifecycle'ıyla çalıştırmak (orijinal EmbAct'in
 * yaptığı gibi) IActivityManager seviyesinde hook gerektirir — bu,
 * VirtualApp gibi projelerin aylar süren ayrı bir mühendislik işi.
 * Şimdilik hedefi normal Intent ile (kendi process'inde) başlatıyoruz;
 * tam "process içine gömme" bir sonraki adım olarak ayrıca planlanmalı.
 */
open class LauncherSlotActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val status = StringBuilder()
        status.append("Process: ${android.app.Application.getProcessName()}\n")
        status.append("Slot: ${Slot.current}\n")

        try {
            val cl = classLoader
            val targetClass = cl.loadClass("android.app.Application")
            status.append("Class loader testi: OK (${targetClass.name} yüklendi)\n")
        } catch (e: Throwable) {
            status.append("Class loader testi: HATA - $e\n")
        }

        val launchIntent = packageManager.getLaunchIntentForPackage(CloneApp.TARGET_PACKAGE)
        if (launchIntent != null) {
            status.append("\nHedef uygulama bulundu, kendi process'inde başlatılıyor.")
            startActivity(launchIntent)
        } else {
            status.append("\nHedef uygulama (${CloneApp.TARGET_PACKAGE}) bu cihazda kurulu değil.")
        }

        setContentView(TextView(this).apply {
            text = status.toString()
            textSize = 16f
            setPadding(48, 96, 48, 48)
        })
    }
}
