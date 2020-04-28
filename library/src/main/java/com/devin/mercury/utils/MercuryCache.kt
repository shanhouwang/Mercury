package com.devin.mercury.utils

import android.text.TextUtils
import android.util.Base64
import com.devin.mercury.config.MercuryConfig
import com.google.gson.Gson
import java.io.*

class MercuryCache {

    companion object {

        private const val MERCURY_CACHE = "mercury_cache"

        private fun getCacheDir(mercury: MercuryConfig): File {
            return File(mercury.getOkClient()?.cache()?.directory()?.path
                    ?: (mercury.getApplication()?.externalCacheDir?.path + File.separator + MERCURY_CACHE + File.separator)).apply {
                if (!exists()) mkdirs()
            }
        }

        fun <T> get(mercury: MercuryConfig, key: String, clazz: Class<T>): T? {
            return try {
                Gson().fromJson(get(mercury, key), clazz)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        fun get(mercury: MercuryConfig, key: String): String? {
            val f = File(getCacheDir(mercury), String(Base64.encode(key.toByteArray(), Base64.DEFAULT)))
            var data = ""
            if (f.exists()) {
                var reader: BufferedReader?
                try {
                    reader = BufferedReader(FileReader(f))
                    var currentLine: String?
                    while (true) {
                        currentLine = reader.readLine()
                        if (TextUtils.isEmpty(currentLine)) break
                        data += currentLine
                    }
                    reader.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            return data
        }

        fun put(mercury: MercuryConfig, key: String, data: String?) {
            val f = File(getCacheDir(mercury), String(Base64.encode(key.toByteArray(), Base64.DEFAULT)))
            var out: BufferedWriter? = null
            var fw: FileWriter? = null
            try {
                fw = FileWriter(f)
                out = BufferedWriter(fw)
                out.write(data)
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                out?.flush()
                out?.close()
                fw?.close()
            }
        }
    }

}