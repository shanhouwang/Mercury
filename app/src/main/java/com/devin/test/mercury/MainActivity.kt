package com.devin.test.mercury

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.devin.mercury.JSON
import com.devin.mercury.Mercury
import okhttp3.OkHttpClient

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Mercury.init(object : Mercury.MercuryBuilder {
            override fun okHttpClient(): OkHttpClient {
                return OkHttpClient()
            }

            override fun contentType(): String {
                return JSON
            }
        })

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
