package com.devin.model.mercury

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
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
import java.util.ArrayList

/**
 * @author devin
 */
abstract class MercuryRequest {

    private lateinit var activity: Activity
    private var tag: String? = null

    /**
     * 上下文
     */
    fun lifecycle(activity: Activity): MercuryRequest {
        println("#####activity: ${Thread.currentThread().id}#####")
        this.activity = activity
        tag = activity?.javaClass?.name + activity?.hashCode()
        registerCancelEvent()
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

        println("#####buildRequest: ${Thread.currentThread().id}#####")

        var fields = this@MercuryRequest.javaClass.declaredFields

        var get = this.javaClass.getAnnotation(Get::class.java) ?: null
        if (null != get) {
            return Request.Builder()
                    .url(StringBuilder().apply {
                        append(Mercury.host)
                        append(get?.url)
                        append("?")
                        fields?.forEachIndexed { index, field ->
                            field.isAccessible = true
                            append("${field.name}=${field.get(this@MercuryRequest)}")
                            if (index != fields.size - 1) {
                                append("&")
                            }
                        }
                    }.toString())
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

    private var length: Int = 0

    private fun registerCancelEvent() {

        /** 一个Activity多个请求，只会注册一次 registerActivityLifecycleCallbacks */
        Mercury.context.javaClass.declaredFields.forEach {
            it.isAccessible = true
            if (it.name == "mActivityLifecycleCallbacks") {
                if (Mercury.activityLifecycleCallbacks > length) {
                    return@registerCancelEvent
                }
                length = (it.get(Mercury.context) as ArrayList<*>).size
                Mercury.activityLifecycleCallbacks = length
                return@forEach
            }
        }
        Mercury.context.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityPaused(activity: Activity?) {
            }
            override fun onActivityResumed(activity: Activity?) {
            }
            override fun onActivityStarted(activity: Activity?) {
            }
            override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
            }
            override fun onActivityStopped(activity: Activity?) {
            }
            override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
            }

            override fun onActivityDestroyed(activity: Activity?) {
                if (this@MercuryRequest.activity?.hashCode() == activity?.hashCode()) {
                    cancel()
                    Mercury.context.unregisterActivityLifecycleCallbacks(this)
                    Mercury.activityLifecycleCallbacks--
                }
            }
        })
        Mercury.activityLifecycleCallbacks++
        println(">>>>>registerCancelEvent: ${Mercury.activityLifecycleCallbacks}<<<<<")
    }

    private fun cancel() {
        println(">>>>>cancel, tag: ${this@MercuryRequest.tag}<<<<<")
        Mercury.mOkHttpClient.dispatcher().runningCalls().forEach {
            Log.d(">>>>>cancel", ">>>>>cancel, runningCalls():  ${it.request().tag()}")
            if (this@MercuryRequest.tag == it.request().tag()) {
                Log.e(">>>>>cancel", ">>>>>cancel, runningCalls():  ${it.request().tag()}")
                it.cancel()
            }
        }

        Mercury.mOkHttpClient.dispatcher().queuedCalls().forEach {
            Log.d(">>>>>cancel", ">>>>>cancel, queuedCalls():  ${it.request().tag()}")
            if (this@MercuryRequest.tag == it.request().tag()) {
                Log.e(">>>>>cancel", ">>>>>cancel, queuedCalls():  ${it.request().tag()}")
                it.cancel()
            }
        }
    }

}