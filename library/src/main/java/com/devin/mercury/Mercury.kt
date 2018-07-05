package com.devin.mercury

import android.content.Context
import okhttp3.OkHttpClient

class Mercury {

    companion object {

        lateinit var context: Context
        lateinit var mOkHttpClient: OkHttpClient
        lateinit var contentType: String

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