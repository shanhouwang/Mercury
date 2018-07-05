package com.devin.mercury.utils

import android.text.TextUtils
import android.util.Base64
import com.alibaba.fastjson.JSON
import com.devin.mercury.Mercury
import java.io.*

class MercuryCache {

    companion object {

        const val MERCURY_CACHE = "mercury_cache"

        private fun getCacheDir(): File {
            return File(Mercury.mOkHttpClient.cache()?.directory()?.path
                    ?: (Mercury.context.externalCacheDir.path + File.separator + MERCURY_CACHE))
        }

        fun <T> get(key: String, clazz: Class<T>): T? {
            return try {
                JSON.parseObject(get(key), clazz)
            } catch (e: Exception) {
                null
            }
        }

        fun get(key: String): String? {
            var f = File(getCacheDir(), String(Base64.encode(key.toByteArray(), Base64.DEFAULT)))
            var data = ""
            if (f.exists()) {
                var reader: BufferedReader? = null
                try {
                    reader = BufferedReader(FileReader(f))
                    var currentLine: String
                    while (true) {
                        currentLine = reader.readLine()
                        if (TextUtils.isEmpty(currentLine)) break
                        data += currentLine
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    reader?.close()
                }
            }
            return data
        }

        fun put(key: String, data: String?) {
            var f = File(getCacheDir(), String(Base64.encode(key.toByteArray(), Base64.DEFAULT)))
            f.mkdir()
            var out: BufferedWriter? = null
            try {
                out = BufferedWriter(FileWriter(f))
                out.write(data)
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                out?.flush()
                out?.close()
            }
        }
    }

}