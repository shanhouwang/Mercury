package com.devin.test.mercury

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.devin.mercury.Mercury
import com.devin.mercury.MercuryContentType
import com.devin.mercury.interceptor.HttpLoggingInterceptor
import com.readystatesoftware.chuck.ChuckInterceptor
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        println(">>>>>main thread: ${Thread.currentThread().id}<<<<<")

        Mercury.init(object : Mercury.MercuryBuilder {
            override fun host(): String {
                return "http://www.baidu.com/"
            }

            override fun getContext(): Application {
                return this@MainActivity.application
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

        for (i in 0 until 1000) {
            BaseRequest("10086", "Devin：$i")
                    .activity(this@MainActivity)
                    .request(BaseResponse::class.java
                            , startCallback = {
                        progressBar.visibility = View.VISIBLE
                        println(">>>>>start: ${Thread.currentThread().id}<<<<<")
                    }
                            , endCallback = {
                        progressBar.visibility = View.GONE
                        println(">>>>>end: ${Thread.currentThread().id}<<<<<")
                    }
                            , successCallback = {
                        println(">>>>>success: ${Thread.currentThread().id}<<<<<")
                    }
                            , cacheCallback = {
                        println(">>>>>cache: ${Thread.currentThread().id}<<<<<")
                    }
                            , failedCallback = {
                        println(">>>>>fail: ${Thread.currentThread().id}<<<<<")
                    }
                    )
        }
        NullRequest().request(BaseResponse::class.java, successCallback = {})
    }
}
