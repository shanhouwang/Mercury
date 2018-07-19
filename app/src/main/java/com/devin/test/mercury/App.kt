package com.devin.test.mercury

import android.app.Application
import com.devin.mercury.Mercury
import com.devin.mercury.MercuryContentType
import com.devin.mercury.interceptor.HttpLoggingInterceptor
import com.readystatesoftware.chuck.ChuckInterceptor
import okhttp3.OkHttpClient

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        Mercury.init(object : Mercury.MercuryBuilder {
            override fun host(): String {
                return "http://www.baidu.com/"
            }

            override fun getContext(): Application {
                return this@App
            }

            override fun okHttpClient(): OkHttpClient {
                return OkHttpClient.Builder()
                        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                        .addInterceptor(ChuckInterceptor(this@App).showNotification(true))
                        .build()
            }

            override fun defaultContentType(): String {
                return MercuryContentType.JSON
            }
        })
    }
}