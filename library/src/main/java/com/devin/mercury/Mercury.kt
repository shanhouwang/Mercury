package com.devin.mercury

import android.content.Context
import android.os.Handler
import android.os.Looper
import okhttp3.OkHttpClient

class Mercury {

    companion object {

        lateinit var context: Context
        lateinit var mOkHttpClient: OkHttpClient
        lateinit var contentType: String
        var handler = Handler(Looper.getMainLooper())

        fun init(builder: MercuryBuilder) {
            mOkHttpClient = builder.okHttpClient()
            contentType = builder.contentType()
            context = builder.getContext()
        }
    }

    interface MercuryBuilder {

        fun getContext(): Context

        fun okHttpClient(): OkHttpClient

        fun contentType(): String

    }
}