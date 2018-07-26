package com.devin.test.mercury

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.devin.mercury.config.MercuryFilter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        println(">>>>>main thread: ${Thread.currentThread().id}<<<<<")

        for (i in 0 until 1) {
            MainRequest("10086", "Devin：$i")
                    .filter(object : MercuryFilter {
                        override fun body(body: String): String {
                            return ""
                        }
                    })
                    .requestByLifecycle(BaseResponse::class.java
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
        tv_skip.setOnClickListener {
            startActivity(Intent(this@MainActivity, TestActivity::class.java))
            finish()
        }
    }
}
