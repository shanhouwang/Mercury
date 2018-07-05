package com.devin.test.mercury

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.devin.mercury.Mercury
import com.devin.mercury.MercuryContentType
import com.devin.mercury.interceptor.HttpLoggingInterceptor
import com.readystatesoftware.chuck.ChuckInterceptor
import okhttp3.OkHttpClient

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        println(">>>>>main thread: ${Thread.currentThread().id}<<<<<")

        Mercury.init(object : Mercury.MercuryBuilder {
            override fun getContext(): Context {
                return this@MainActivity
            }

            override fun okHttpClient(): OkHttpClient {
                return OkHttpClient.Builder()
                        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                        .addInterceptor(ChuckInterceptor(this@MainActivity).showNotification(true))
                        .build()
            }

            override fun contentType(): String {
                return MercuryContentType.JSON
            }
        })

        BaseRequest("10086", "Devin").request(BaseResponse::class.java
                , startCallback = {
                    println(">>>>>start: $this<<<<<")
                }
                , endCallback = {
                    println(">>>>>end: $this<<<<<")
                }
                , successCallback = {
                    println(">>>>>success: $this<<<<<")
                }
                , cacheCallback = {
                    println(">>>>>cache: $this<<<<<")
                }
                , failedCallback = {
                    println(">>>>>fail: $this<<<<<")
                }
        )
    }
}
