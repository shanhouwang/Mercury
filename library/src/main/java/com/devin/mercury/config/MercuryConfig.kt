package com.devin.mercury.config

import android.app.Application
import okhttp3.OkHttpClient

interface MercuryConfig {

    fun getApplication(): Application?

    fun getOkClient(): OkHttpClient?

    fun getContentType(): String?

    fun getHost(): String?

    fun getGlobalFilter(): MercuryFilter?

    fun getConfigName(): String

}