package com.devin.test.mercury

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class TestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
//        println(">>>>>main thread: ${Thread.currentThread().id}<<<<<")
//
//        for (i in 0 until 1) {
//            MainRequest("10086", "Devin：$i")
//                    .requestByLifecycle(startCallback = {
//                        progressBar.visibility = View.VISIBLE
//                        println(">>>>>start: ${Thread.currentThread().id}<<<<<")
//                    }
//                            , endCallback = {
//                        progressBar.visibility = View.GONE
//                        println(">>>>>end: ${Thread.currentThread().id}<<<<<")
//                    }
//                            , successCallback = {
//                        println(">>>>>success: ${Thread.currentThread().id}<<<<<")
//                    }
//                            , cacheCallback = {
//                        println(">>>>>cache: ${Thread.currentThread().id}<<<<<")
//                    }
//                            , failedCallback = {
//                        println(">>>>>fail: ${Thread.currentThread().id}<<<<<")
//                    }
//                    )
//        }
        NullRequest().request(successCallback = {})
    }
}
