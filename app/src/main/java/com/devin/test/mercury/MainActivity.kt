package com.devin.test.mercury

import android.app.Application
import android.content.Context
import android.content.Intent
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

        for (i in 0 until 1000) {
            BaseRequest("10086", "Devin：$i")
                    .lifecycle(this@MainActivity)
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
        tv_skip.setOnClickListener {
            startActivity(Intent(this@MainActivity, TestActivity::class.java))
            finish()
        }
    }
}
