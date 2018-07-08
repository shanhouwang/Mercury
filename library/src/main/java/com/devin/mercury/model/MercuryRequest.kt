package com.devin.model.mercury

import android.annotation.TargetApi
import android.app.Activity
import android.os.Build
import com.alibaba.fastjson.JSON
import com.devin.mercury.Mercury
import com.devin.mercury.MercuryContentType
import com.devin.mercury.annotation.*
import com.devin.mercury.annotation.Cache
import com.devin.mercury.utils.MercuryCache
import com.devin.mercury.utils.ThreadUtils
import okhttp3.*
import java.io.File
import java.io.IOException
import java.util.Map

/**
 * @author devin
 */
abstract class MercuryRequest {

    private var tag: String? = null

    /**
     * 请求会根据Activity的销毁而取消
     */
    fun lifecycle(activity: Activity): MercuryRequest {
        tag = activity?.javaClass?.name + activity?.hashCode()
        Mercury.activities.add(activity)
        return this
    }

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

    /**
     * 请求会根据Activity的销毁而销毁
     *
     * @param responseClazz 回调类型
     * @param successCallback 成功回调方法
     */
    fun <T> requestByLifecycle(responseClazz: Class<T>
                    , successCallback: T.() -> Unit) {
        buildByLifecycle(responseClazz
                , startCallback = {}
                , endCallback = {}
                , successCallback = { successCallback() }
                , cacheCallback = {}
                , failedCallback = {})
    }

    /**
     * 请求会根据Activity的销毁而销毁
     *
     * @param responseClazz 回调类型
     * @param successCallback 成功回调方法
     * @param failedCallback 失败回调方法
     */
    fun <T> requestByLifecycle(responseClazz: Class<T>
                    , successCallback: T.() -> Unit
                    , failedCallback: String.() -> Unit) {
        buildByLifecycle(responseClazz
                , startCallback = {}
                , endCallback = {}
                , successCallback = { successCallback() }
                , cacheCallback = {}
                , failedCallback = { failedCallback() })
    }

    /**
     * 请求会根据Activity的销毁而销毁
     *
     * @param responseClazz 回调类型
     * @param startCallback 调用网络接口之前的回调
     * @param endCallback 调用网络接口之后的回调
     * @param successCallback 成功回调方法
     */
    fun <T> requestByLifecycle(responseClazz: Class<T>
                    , startCallback: () -> Unit
                    , endCallback: () -> Unit
                    , successCallback: T.() -> Unit) {
        buildByLifecycle(responseClazz
                , startCallback = { startCallback() }
                , endCallback = { endCallback() }
                , successCallback = { successCallback() }
                , cacheCallback = {}
                , failedCallback = {})
    }

    /**
     * 请求会根据Activity的销毁而销毁
     *
     * @param responseClazz 回调类型
     * @param startCallback 调用网络接口之前的回调
     * @param endCallback 调用网络接口之后的回调
     * @param successCallback 成功回调方法
     * @param failedCallback 失败回调方法
     */
    fun <T> requestByLifecycle(responseClazz: Class<T>
                    , startCallback: () -> Unit
                    , endCallback: () -> Unit
                    , successCallback: T.() -> Unit
                    , failedCallback: String.() -> Unit) {
        buildByLifecycle(responseClazz
                , startCallback = { startCallback() }
                , endCallback = { endCallback() }
                , successCallback = { successCallback() }
                , cacheCallback = {}
                , failedCallback = { failedCallback() })
    }

    /**
     * 请求会根据Activity的销毁而销毁
     *
     * @param responseClazz 回调类型
     * @param startCallback 调用网络接口之前的回调
     * @param endCallback 调用网络接口之后的回调
     * @param successCallback 成功回调方法
     * @param cacheCallback 本地缓存回调
     * @param failedCallback 失败回调方法
     */
    fun <T> requestByLifecycle(responseClazz: Class<T>
                               , startCallback: () -> Unit
                               , endCallback: () -> Unit
                               , successCallback: T.() -> Unit
                               , cacheCallback: T.() -> Unit
                               , failedCallback: String.() -> Unit) {
        buildByLifecycle(responseClazz
                , startCallback = { startCallback() }
                , endCallback = { endCallback() }
                , successCallback = { successCallback() }
                , cacheCallback = { cacheCallback() }
                , failedCallback = { failedCallback() })
    }

    private fun <T> buildByLifecycle(responseClazz: Class<T>
                                     , startCallback: () -> Unit
                                     , endCallback: () -> Unit
                                     , successCallback: T.() -> Unit
                                     , cacheCallback: T.() -> Unit
                                     , failedCallback: String.() -> Unit) {
        tag = tag ?: Mercury.getCurActivity()?.javaClass?.name + Mercury.getCurActivity()?.hashCode() + "requestByLifecycle"
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

        ThreadUtils
                .get(ThreadUtils.Type.CACHED)
                .apply {
                    callBack {
                        if (null == it) {
                            return@callBack
                        }
                        Mercury.mOkHttpClient.newCall(it as Request).enqueue(object : Callback {
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
                    start(object : ThreadUtils.MercuryRunnable<Request>() {
                        override fun execute(): Request {
                            return buildRequest()
                        }
                    })
                }
    }

    private fun buildRequest(): Request {

        var fields = this@MercuryRequest.javaClass.declaredFields

        var get = this.javaClass.getAnnotation(Get::class.java) ?: null
        if (null != get) {
            return Request.Builder()
                    .url(StringBuilder().apply {
                        append(Mercury.host)
                        append(get?.url)
                        append("?")
                        for (i in fields.indices) {
                            var h = fields[i].getAnnotation(Header::class.java)
                            if (null != h) {
                                continue
                            }
                            fields[i].isAccessible = true
                            append("${fields[i].name}=${fields[i].get(this@MercuryRequest)}")
                            if (i != fields.size - 1) {
                                append("&")
                            }
                        }
                    }.toString())
                    .apply {
                        getHeaders(this, this@MercuryRequest.javaClass)
                    }
                    .tag(tag)
                    .get()
                    .build()
        }

        var post = this.javaClass.getAnnotation(Post::class.java) ?: null
        if (null != post) {
            var type = this.javaClass.getAnnotation(ContentType::class.java)?.type
                    ?: Mercury.contentType
            var requestBody: RequestBody =
                    when (type) {
                        MercuryContentType.JSON -> RequestBody.create(MediaType.parse(type), JSON.toJSONString(this))
                        MercuryContentType.FORM -> FormBody.Builder().apply {
                            fields?.forEach {
                                it.isAccessible = true
                                println(">>>>>${it.name}, ${it.type}, ${it.get(this@MercuryRequest)}<<<<<")
                                addEncoded(it.name, it.get(this@MercuryRequest)?.toString())
                            }
                        }.build()
                        MercuryContentType.FORM_DATA -> MultipartBody.Builder().apply {
                            fields?.forEach {
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
            return Request.Builder()
                    .url(Mercury.host + post?.url)
                    .tag(tag)
                    .post(requestBody)
                    .build()
        }

        var delete = this.javaClass.getAnnotation(Delete::class.java) ?: null
        if (null != delete) {
            return Request.Builder()
                    .url(Mercury.host + delete?.url)
                    .tag(tag)
                    .delete()
                    .build()
        }

        return throw IllegalArgumentException("request must not null.")
    }

    private fun <T> getHeaders(request: Request.Builder, clazz: Class<T>) {
        if (clazz == Object::class.java) {
            return@getHeaders
        }
        var fields = clazz.declaredFields
        for (i in fields.indices) {
            var it = fields[i]
            it.isAccessible = true
            var h = it.getAnnotation(Header::class.java)
            if (null != h) {
                try {
                    var headers = it.get(this@MercuryRequest) as MutableMap<String, String>
                    request.headers(Headers.Builder().apply {
                        headers.mapValues { (k, v) -> add(k, v) }
                    }.build())
                    return@getHeaders
                } catch (e: ClassCastException) {
                    throw IllegalArgumentException("Header must be a Map<String,String>.")
                }
            } else {
                if (i == fields.lastIndex) {
                    getHeaders(request, clazz.superclass)
                }
            }
        }
    }

    private fun <T> getCache(responseClazz: Class<T>, cacheCallback: T.() -> Unit) {

        this.javaClass.getAnnotation(Cache::class.java) ?: return@getCache
        var key = generateKey() ?: return@getCache
        println(">>>>>key：$key<<<<<")
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
            append(Mercury.host)
            append(url)
            fields.forEach {
                append("&")
                it.isAccessible = true
                append("${it.name}=${it.get(this@MercuryRequest)}")
            }
        }.toString()
    }
}