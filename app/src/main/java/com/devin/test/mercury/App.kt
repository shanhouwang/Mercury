package com.devin.test.mercury

import android.app.Application
import com.devin.mercury.Mercury
import com.devin.mercury.MercuryContentType
import com.devin.mercury.config.MercuryConfig
import com.devin.mercury.config.MercuryFilter
import com.devin.mercury.interceptor.HttpLoggingInterceptor
import com.devin.mercury.model.MercuryFilterModel
import com.google.gson.JsonParser
import com.readystatesoftware.chuck.ChuckInterceptor
import okhttp3.OkHttpClient

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        Mercury.init(Mercury.Builder()
                .context(this@App)
                .host("http://www.baidu.com/")
                .okHttpClient(OkHttpClient.Builder()
                        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                        .addInterceptor(ChuckInterceptor(this@App).showNotification(true))
                        .build())
                .filter(object : MercuryFilter {
                    override fun body(body: String, clazz: Class<*>): MercuryFilterModel {
                        val model = MercuryFilterModel()
                        model.body = body
                        return model
                    }
                })
                .contentType(MercuryContentType.JSON))

        Mercury.addMercuryConfig(object : MercuryConfig {
            override fun getApplication(): Application? {
                return this@App
            }

            override fun getOkClient(): OkHttpClient? {
                return OkHttpClient.Builder()
                        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                        .addInterceptor(ChuckInterceptor(this@App).showNotification(true))
                        .build()
            }

            override fun getContentType(): String? {
                return MercuryContentType.JSON
            }

            override fun getHost(): String? {
                return "https://www.baidu.com/"
            }

            override fun getGlobalFilter(): MercuryFilter? {
                return null
            }

            override fun getConfigName(): String {
                return "IM"
            }
        })
    }
}