package com.devin.mercury

import okhttp3.OkHttpClient

class Mercury {

    companion object {

        lateinit var mOkHttpClient: OkHttpClient
        lateinit var contentType: String

        fun init(builder: MercuryBuilder) {
            mOkHttpClient = builder.okHttpClient()
            contentType = builder.contentType()
        }
    }

    interface MercuryBuilder {

        fun okHttpClient(): OkHttpClient

        fun contentType(): String

    }
}