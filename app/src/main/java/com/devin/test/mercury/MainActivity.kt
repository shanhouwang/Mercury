package com.devin.test.mercury

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.devin.mercury.Mercury
import okhttp3.OkHttpClient

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Mercury.init(OkHttpClient())

        BaseRequest("","").request(BaseResponse::class.java
                , startCallback = {
                }
                , endCallback = {
                }
                , successCallback = {

                }
                , cacheCallback = {

                }
                , failCallback = {

                }
        )

        BaseRequest("","").request(BaseResponse::class.java
                , successCallback = {

                }
                , failCallback = {

                }
        )
    }
}
