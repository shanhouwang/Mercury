package com.devin.test.mercury

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

        Mercury.init(object : Mercury.MercuryBuilder {
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
                , successCallback = {
        }
                , failCallback = {
        }
        )

//        UpdateAvatarRequest("10086", File("")).request(BaseResponse::class.java
//                , successCallback = {}
//                , failCallback = {}
//        )
    }
}
