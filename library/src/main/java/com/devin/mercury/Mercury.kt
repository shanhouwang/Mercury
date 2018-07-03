package com.devin.mercury

import okhttp3.OkHttpClient

class Mercury {

    companion object {

        lateinit var mOkHttpClient: OkHttpClient

        fun init(client: OkHttpClient) {
            mOkHttpClient = client
        }
    }
}