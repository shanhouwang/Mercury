package com.devin.model.mercury

import com.devin.mercury.Mercury
import com.devin.mercury.annotation.Get
import okhttp3.*
import java.io.IOException

abstract class MercuryRequest {

    /**
     * @param responseClazz 回调类型
     *
     * @param successCallback 成功回调方法
     */
    fun <T> request(responseClazz: Class<T>
                    , successCallback: T.() -> Unit) {
        build(responseClazz
                , startCallback = {}
                , endCallback = {}
                , successCallback = { successCallback }
                , cacheCallback = {}
                , failCallback = {})
    }

    /**
     * @param responseClazz 回调类型
     *
     * @param successCallback 成功回调方法
     *
     * @param failCallback 失败回调方法
     */
    fun <T> request(responseClazz: Class<T>
                    , successCallback: T.() -> Unit
                    , failCallback: String.() -> Unit) {
        build(responseClazz
                , startCallback = {}
                , endCallback = {}
                , successCallback = { successCallback }
                , cacheCallback = {}
                , failCallback = { failCallback })
    }

    /**
     * @param responseClazz 回调类型
     *
     * @param startCallback 调用网络接口之前的回调
     *
     * @param endCallback 调用网络接口之后的回调
     *
     * @param successCallback 成功回调方法
     */
    fun <T> request(responseClazz: Class<T>
                    , startCallback: () -> Unit
                    , endCallback: () -> Unit
                    , successCallback: T.() -> Unit) {
        build(responseClazz
                , startCallback = { startCallback }
                , endCallback = { endCallback }
                , successCallback = { successCallback }
                , cacheCallback = {}
                , failCallback = {})
    }

    /**
     * @param responseClazz 回调类型
     *
     * @param startCallback 调用网络接口之前的回调
     *
     * @param endCallback 调用网络接口之后的回调
     *
     * @param successCallback 成功回调方法
     *
     * @param failCallback 失败回调方法
     */
    fun <T> request(responseClazz: Class<T>
                    , startCallback: () -> Unit
                    , endCallback: () -> Unit
                    , successCallback: T.() -> Unit
                    , failCallback: String.() -> Unit) {
        build(responseClazz
                , startCallback = { startCallback }
                , endCallback = { endCallback }
                , successCallback = { successCallback }
                , cacheCallback = {}
                , failCallback = { failCallback })
    }

    /**
     * @param responseClazz 回调类型
     *
     * @param startCallback 调用网络接口之前的回调
     *
     * @param endCallback 调用网络接口之后的回调
     *
     * @param successCallback 成功回调方法
     *
     * @param cacheCallback 本地缓存回调
     *
     * @param failCallback 失败回调方法
     */
    fun <T> request(responseClazz: Class<T>
                    , startCallback: () -> Unit
                    , endCallback: () -> Unit
                    , successCallback: T.() -> Unit
                    , cacheCallback: T.() -> Unit
                    , failCallback: String.() -> Unit) {
        build(responseClazz
                , startCallback = { startCallback }
                , endCallback = { endCallback }
                , successCallback = { successCallback }
                , cacheCallback = { cacheCallback }
                , failCallback = { failCallback })
    }

    private fun <T> build(responseClazz: Class<T>
                          , startCallback: () -> Unit
                          , endCallback: () -> Unit
                          , successCallback: T.() -> Unit
                          , cacheCallback: T.() -> Unit
                          , failCallback: String.() -> Unit) {
        var url = this.javaClass.getAnnotation(Get::class.java)?.url
        println(">>>>>url：$url<<<<<")
        var request = Request.Builder().url(url).get().build()
        Mercury.mOkHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
            }

            override fun onResponse(call: Call?, response: Response?) {
                println(">>>>>${response?.body()?.string()}<<<<<")
            }
        })
    }

}