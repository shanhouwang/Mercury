package com.devin.mercury

import okhttp3.OkHttpClient

class Mercury {

    companion object {

        lateinit var mOkHttpClient: OkHttpClient
        lateinit var mediaType: String

        fun init(builder: MercuryBuilder) {
            mOkHttpClient = builder.okHttpClient()
            mediaType = builder.mediaType()
        }
    }

    interface MercuryBuilder {

        fun okHttpClient(): OkHttpClient

        fun mediaType(): String

    }
}