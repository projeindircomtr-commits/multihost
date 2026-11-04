package com.salman.multihost

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import dalvik.system.PathClassLoader
import java.lang.reflect.Field

/**
 * Uygulama process'ini bir "slot" (1..N) olarak işaretleyip, o slot için
 * hedef paketi kendi class loader'ı ve izole dosya alanıyla yüklüyor.
 * Teknik: VirtualApp/Parallel Space gibi araçların kullandığı, bilinen
 * genel bir Android tekniği (ApplicationInfo + ClassLoader değişimi).
 */
class CloneApp : Application() {

    companion object {
        /** Klonlanacak hedef uygulamanın paketi. */
        const val TARGET_PACKAGE = "com.mykomobileonline.k1"

        /** AndroidManifest'teki :slot1, :slot2 ... process adlarının öneki. */
        const val SLOT_PROCESS_PREFIX = "com.salman.multihost:slot"
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)

        val slot = slotNumberFromProcessName() ?: return // ana process ise klonlama yok

        val targetInfo = try {
            base.packageManager.getApplicationInfo(TARGET_PACKAGE, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            return // hedef uygulama kurulu değil
        }

        redirectClassLoader(base, targetInfo)
        Slot.current = slot // dosya izolasyonu bunu kullanacak
    }

    private fun slotNumberFromProcessName(): Int? {
        val processName = Application.getProcessName() ?: return null
        if (!processName.startsWith(SLOT_PROCESS_PREFIX)) return null
        return processName.removePrefix(SLOT_PROCESS_PREFIX).toIntOrNull()
    }

    private fun redirectClassLoader(context: Context, targetInfo: ApplicationInfo) {
        val classLoader = PathClassLoader(
            targetInfo.sourceDir,
            targetInfo.nativeLibraryDir,
            context.classLoader
        )

        val activityThread = getField(context, "mPackageInfo")
        setField(activityThread, "mClassLoader", classLoader)
        setField(activityThread, "mAppDir", targetInfo.sourceDir)
        setField(activityThread, "mResDir", targetInfo.sourceDir)

        Thread.currentThread().contextClassLoader = classLoader
    }

    private fun getField(obj: Any, name: String): Any {
        val field = findField(obj.javaClass, name)
        return field.get(obj)!!
    }

    private fun setField(obj: Any, name: String, value: Any?) {
        findField(obj.javaClass, name).set(obj, value)
    }

    private fun findField(startClass: Class<*>, name: String): Field {
        var cls: Class<*>? = startClass
        while (cls != null) {
            try {
                val field = cls.getDeclaredField(name)
                field.isAccessible = true
                return field
            } catch (e: NoSuchFieldException) {
                cls = cls.superclass
            }
        }
        throw NoSuchFieldException(name)
    }
}

/** Aktif slot numarasını tutar. */
object Slot {
    var current: Int = 0 // 0 = ana process, klonlama yok
    fun isActive() = current > 0
    fun tag() = "slot$current"
}
