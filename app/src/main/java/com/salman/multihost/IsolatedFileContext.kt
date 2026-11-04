package com.salman.multihost

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import java.io.File

/**
 * getFilesDir/getSharedPreferences gibi çağrıları slot'a özel alt klasöre
 * yönlendiren wrapper. CloneApp aktifken (Slot.isActive()) kullanılır.
 */
class IsolatedFileContext(base: Context) : ContextWrapper(base) {

    private fun isolatedDir(original: File?): File? {
        if (original == null || !Slot.isActive()) return original
        val dir = File(original, Slot.tag())
        dir.mkdirs()
        return dir
    }

    override fun getFilesDir(): File = isolatedDir(super.getFilesDir())!!

    override fun getCacheDir(): File = isolatedDir(super.getCacheDir())!!

    override fun getSharedPreferences(name: String, mode: Int): SharedPreferences {
        val isolatedName = if (Slot.isActive()) "${name}_${Slot.tag()}" else name
        return super.getSharedPreferences(isolatedName, mode)
    }
}
