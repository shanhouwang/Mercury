package com.devin.model.mercury

import android.text.TextUtils
import com.alibaba.fastjson.JSON
import com.devin.mercury.*
import com.devin.mercury.annotation.Get
import com.devin.mercury.annotation.ContentType
import com.devin.mercury.annotation.Post
import okhttp3.*
import org.json.JSONObject
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

        Mercury.mOkHttpClient.newCall(buildRequest()).enqueue(object : Callback {
            override fun onResponse(call: Call?, response: Response?) {
                println(">>>>>${response?.body()?.string()}<<<<<")
            }

            override fun onFailure(call: Call?, e: IOException?) {
            }
        })
    }

    private fun buildRequest(): Request {

        var url = this.javaClass.getAnnotation(Get::class.java)?.url
        if (!TextUtils.isEmpty(url)) {
            return Request.Builder().url(url).get().build()
        }

        url = this.javaClass.getAnnotation(Post::class.java)?.url

        if (!TextUtils.isEmpty(url)) {

            var type = this.javaClass.getAnnotation(ContentType::class.java)?.type
                    ?: Mercury.contentType
            var requestBody: RequestBody =
                    when (type) {
                        MercuryContentType.JSON -> RequestBody.create(MediaType.parse(type), JSON.toJSONString(this))
                        MercuryContentType.FORM -> FormBody.Builder().apply {
                            this.javaClass.declaredFields.forEach {
                                it.isAccessible = true
                                addEncoded(it.name, it.get(this@MercuryRequest).toString())
                            }
                        }.build()
                        else -> throw IllegalArgumentException("not find content type $type.")
                    }
            return Request.Builder().url(url).post(requestBody).build()
        }
        return throw IllegalArgumentException("request must not null.")
    }

}