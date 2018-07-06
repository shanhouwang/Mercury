package com.devin.model.mercury

import android.text.TextUtils
import com.alibaba.fastjson.JSON
import com.devin.mercury.Mercury
import com.devin.mercury.MercuryContentType
import com.devin.mercury.annotation.Cache
import com.devin.mercury.annotation.ContentType
import com.devin.mercury.annotation.Get
import com.devin.mercury.annotation.Post
import com.devin.mercury.utils.MercuryCache
import com.devin.mercury.utils.ThreadUtils
import okhttp3.*
import java.io.File
import java.io.IOException

/**
 * @author devin
 */
abstract class MercuryRequest {

    /**
     * @param responseClazz 回调类型
     * @param successCallback 成功回调方法
     */
    fun <T> request(responseClazz: Class<T>
                    , successCallback: T.() -> Unit) {
        build(responseClazz
                , startCallback = {}
                , endCallback = {}
                , successCallback = { successCallback() }
                , cacheCallback = {}
                , failedCallback = {})
    }

    /**
     * @param responseClazz 回调类型
     * @param successCallback 成功回调方法
     * @param failedCallback 失败回调方法
     */
    fun <T> request(responseClazz: Class<T>
                    , successCallback: T.() -> Unit
                    , failedCallback: String.() -> Unit) {
        build(responseClazz
                , startCallback = {}
                , endCallback = {}
                , successCallback = { successCallback() }
                , cacheCallback = {}
                , failedCallback = { failedCallback() })
    }

    /**
     * @param responseClazz 回调类型
     * @param startCallback 调用网络接口之前的回调
     * @param endCallback 调用网络接口之后的回调
     * @param successCallback 成功回调方法
     */
    fun <T> request(responseClazz: Class<T>
                    , startCallback: () -> Unit
                    , endCallback: () -> Unit
                    , successCallback: T.() -> Unit) {
        build(responseClazz
                , startCallback = { startCallback() }
                , endCallback = { endCallback() }
                , successCallback = { successCallback() }
                , cacheCallback = {}
                , failedCallback = {})
    }

    /**
     * @param responseClazz 回调类型
     * @param startCallback 调用网络接口之前的回调
     * @param endCallback 调用网络接口之后的回调
     * @param successCallback 成功回调方法
     * @param failedCallback 失败回调方法
     */
    fun <T> request(responseClazz: Class<T>
                    , startCallback: () -> Unit
                    , endCallback: () -> Unit
                    , successCallback: T.() -> Unit
                    , failedCallback: String.() -> Unit) {
        build(responseClazz
                , startCallback = { startCallback() }
                , endCallback = { endCallback() }
                , successCallback = { successCallback() }
                , cacheCallback = {}
                , failedCallback = { failedCallback() })
    }

    /**
     * @param responseClazz 回调类型
     * @param startCallback 调用网络接口之前的回调
     * @param endCallback 调用网络接口之后的回调
     * @param successCallback 成功回调方法
     * @param cacheCallback 本地缓存回调
     * @param failedCallback 失败回调方法
     */
    fun <T> request(responseClazz: Class<T>
                    , startCallback: () -> Unit
                    , endCallback: () -> Unit
                    , successCallback: T.() -> Unit
                    , cacheCallback: T.() -> Unit
                    , failedCallback: String.() -> Unit) {
        build(responseClazz
                , startCallback = { startCallback() }
                , endCallback = { endCallback() }
                , successCallback = { successCallback() }
                , cacheCallback = { cacheCallback() }
                , failedCallback = { failedCallback() })
    }

    private fun <T> build(responseClazz: Class<T>
                          , startCallback: () -> Unit
                          , endCallback: () -> Unit
                          , successCallback: T.() -> Unit
                          , cacheCallback: T.() -> Unit
                          , failedCallback: String.() -> Unit) {
        startCallback.invoke()

        /** 判断是否使用缓存 */
        getCache(responseClazz, cacheCallback = { cacheCallback() })

        Mercury.mOkHttpClient.newCall(buildRequest()).enqueue(object : Callback {
            override fun onResponse(call: Call?, response: Response?) {
                Mercury.handler.post({
                    endCallback.invoke()
                })
                var body = response?.body()?.string()
                println(">>>>>onResponse: $body<<<<<")
                Mercury.handler.post({
                    try {
                        successCallback.invoke(JSON.parseObject(body, responseClazz))
                        /** 说明此时业务数据是正常的 判断是否存储 */
                        store(body)
                    } catch (e: Exception) {
                        Mercury.handler.post({
                            failedCallback.invoke(e.message ?: "exception")
                        })
                    }
                })
            }

            override fun onFailure(call: Call?, e: IOException?) {
                Mercury.handler.post({
                    failedCallback.invoke(e?.message ?: "exception")
                })
            }
        })
    }

    private fun buildRequest(): Request {

        var fields = this@MercuryRequest.javaClass.declaredFields

        var url = this.javaClass.getAnnotation(Get::class.java)?.url ?: ""
        if (!TextUtils.isEmpty(url)) {
            return Request.Builder().url(StringBuilder().apply {
                append(url)
                append("?")
                fields.forEachIndexed { index, field ->
                    field.isAccessible = true
                    append("${field.name}=${field.get(this@MercuryRequest)}")
                    if (index != fields.size - 1) {
                        append("&")
                    }
                }
            }.toString()).get().build()
        }

        url = this.javaClass.getAnnotation(Post::class.java)?.url ?: ""
        if (!TextUtils.isEmpty(url)) {

            var type = this.javaClass.getAnnotation(ContentType::class.java)?.type
                    ?: Mercury.contentType
            var requestBody: RequestBody =
                    when (type) {
                        MercuryContentType.JSON -> RequestBody.create(MediaType.parse(type), JSON.toJSONString(this))
                        MercuryContentType.FORM -> FormBody.Builder().apply {
                            fields.forEach {
                                it.isAccessible = true
                                println(">>>>>${it.name}, ${it.type}, ${it.get(this@MercuryRequest)}<<<<<")
                                addEncoded(it.name, it.get(this@MercuryRequest)?.toString())
                            }
                        }.build()
                        MercuryContentType.FORM_DATA -> MultipartBody.Builder().apply {
                            fields.forEach {
                                if (it.type == File::class.java) {
                                    var file = it.get(this@MercuryRequest) as File
                                    addFormDataPart(it.name
                                            , file.name
                                            , RequestBody.create(MediaType.parse("application/octet-stream"), file))
                                } else {
                                    addFormDataPart(it.name, it.get(this@MercuryRequest).toString())
                                }
                            }
                        }.build()
                        else -> throw IllegalArgumentException("not find content type $type.")
                    }
            return Request.Builder().url(url).post(requestBody).build()
        }
        return throw IllegalArgumentException("request must not null.")
    }

    private fun <T> getCache(responseClazz: Class<T>, cacheCallback: T.() -> Unit) {

        this.javaClass.getAnnotation(Cache::class.java) ?: return@getCache
        var key = generateKey() ?: return@getCache

        ThreadUtils
                .get(ThreadUtils.Type.CACHED)
                .apply {
                    callBack {
                        if (null == it) {
                            return@callBack
                        }
                        Mercury.handler.post({
                            cacheCallback.invoke(it as T)
                        })
                    }
                    start(object : ThreadUtils.MercuryRunnable<T>() {
                        override fun execute(): T? {
                            return MercuryCache.get(key, responseClazz)
                        }
                    })
                }
    }

    private fun store(body: String?) {

        body ?: return@store
        this.javaClass.getAnnotation(Cache::class.java) ?: return@store
        var key = generateKey() ?: return@store
        println(">>>>>key：$key<<<<<")
        ThreadUtils
                .get(ThreadUtils.Type.CACHED)
                .start {
                    MercuryCache.put(key, body)
                }
    }

    private fun generateKey(): String? {

        var fields = this@MercuryRequest.javaClass.declaredFields

        var url = this.javaClass.getAnnotation(Get::class.java)?.url
                ?: this.javaClass.getAnnotation(Post::class.java)?.url
                ?: return null
        return StringBuilder().apply {
            append(url)
            fields.forEachIndexed { index, field ->
                append("&")
                field.isAccessible = true
                append("${field.name}=${field.get(this@MercuryRequest)}")
            }
        }.toString()
    }

}