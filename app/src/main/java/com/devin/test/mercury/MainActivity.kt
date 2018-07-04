package com.devin.test.mercury

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.devin.mercury.Mercury
import com.devin.mercury.MercuryContentType
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
                return MercuryContentType.JSON
            }
        })

        BaseRequest("10086","Devin").request(BaseResponse::class.java
                , successCallback = {
                }
                , failCallback = {
                }
        )
    }
}
