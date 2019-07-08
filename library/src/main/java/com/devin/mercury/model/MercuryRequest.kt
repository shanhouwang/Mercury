package com.devin.model.mercury

import android.app.Activity
import android.net.Uri
import android.text.TextUtils
import android.util.Base64
import com.devin.mercury.Mercury
import com.devin.mercury.MercuryContentType
import com.devin.mercury.annotation.*
import com.devin.mercury.annotation.Cache
import com.devin.mercury.config.MercuryFilter
import com.devin.mercury.model.*
import com.devin.mercury.utils.MercuryCache
import com.devin.mercury.utils.ThreadUtils
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.*
import java.io.File
import java.io.IOException
import java.lang.reflect.Field

/**
 * @author devin
 */
abstract class MercuryRequest {

    @Ignore
    private var tag: String? = null
    @Ignore
    private var mercuryBuildHeaders: MercuryBuildHeaders? = null
    @Ignore
    private var filter: MercuryFilter? = null
    /** don't have host */
    @Ignore
    private var url: String = ""

    /**
     * 请求会根据Activity的销毁而取消
     */
    fun lifecycle(activity: Activity): MercuryRequest {
        tag = activity.javaClass.name + activity.hashCode()
        Mercury.activities.add(activity)
        return this
    }

    /** 本次请求的过滤器 */
    fun filter(filter: MercuryFilter): MercuryRequest {
        this.filter = filter
        return this
    }

    /**
     * @param responseClazz 回调类型
     * @param successCallback 成功回调方法
     */
    fun <T> request(responseClazz: Class<T>, successCallback: T.() -> Unit) {
        build(responseClazz, {}, {}, successCallback, {}, {})
    }

    /**
     * @param responseClazz 回调类型
     * @param successCallback 成功回调方法
     */
    fun <T> request(responseClazz: Class<T>, successCallback: MercurySuccessCallback<T>) {
        build(responseClazz, null, null, successCallback, null, null)
    }

    /**
     * @param responseClazz 回调类型
     * @param successCallback 成功回调方法
     * @param failedCallback 失败回调方法
     */
    fun <T> request(responseClazz: Class<T>, successCallback: T.() -> Unit, failedCallback: String.() -> Unit) {
        build(responseClazz, {}, {}, successCallback, {}, failedCallback)
    }

    /**
     * @param responseClazz 回调类型
     * @param successCallback 成功回调方法
     * @param failedCallback 失败回调方法
     */
    fun <T> request(responseClazz: Class<T>, successCallback: MercurySuccessCallback<T>, failedCallback: MercuryFailedCallback) {
        build(responseClazz, null, null, successCallback, null, failedCallback)
    }

    /**
     * @param responseClazz 回调类型
     * @param startCallback 调用网络接口之前的回调
     * @param endCallback 调用网络接口之后的回调
     * @param successCallback 成功回调方法
     */
    fun <T> request(responseClazz: Class<T>, startCallback: () -> Unit, endCallback: () -> Unit, successCallback: T.() -> Unit) {
        build(responseClazz, startCallback, endCallback, successCallback, {}, {})
    }

    /**
     * @param responseClazz 回调类型
     * @param startCallback 调用网络接口之前的回调
     * @param endCallback 调用网络接口之后的回调
     * @param successCallback 成功回调方法
     */
    fun <T> request(responseClazz: Class<T>, startCallback: MercuryStartCallback, endCallback: MercuryEndCallback, successCallback: MercurySuccessCallback<T>) {
        build(responseClazz, startCallback, endCallback, successCallback, null, null)
    }

    /**
     * @param responseClazz 回调类型
     * @param startCallback 调用网络接口之前的回调
     * @param endCallback 调用网络接口之后的回调
     * @param successCallback 成功回调方法
     * @param failedCallback 失败回调方法
     */
    fun <T> request(responseClazz: Class<T>, startCallback: () -> Unit, endCallback: () -> Unit, successCallback: T.() -> Unit, failedCallback: String.() -> Unit) {
        build(responseClazz, startCallback, endCallback, successCallback, {}, failedCallback)
    }

    /**
     * @param responseClazz 回调类型
     * @param startCallback 调用网络接口之前的回调
     * @param endCallback 调用网络接口之后的回调
     * @param successCallback 成功回调方法
     * @param failedCallback 失败回调方法
     */
    fun <T> request(responseClazz: Class<T>, startCallback: MercuryStartCallback, endCallback: MercuryEndCallback, successCallback: MercurySuccessCallback<T>, failedCallback: MercuryFailedCallback) {
        build(responseClazz, startCallback, endCallback, successCallback, null, failedCallback)
    }

    /**
     * @param responseClazz 回调类型
     * @param startCallback 调用网络接口之前的回调
     * @param endCallback 调用网络接口之后的回调
     * @param successCallback 成功回调方法
     * @param cacheCallback 本地缓存回调
     * @param failedCallback 失败回调方法
     */
    fun <T> request(responseClazz: Class<T>, startCallback: () -> Unit, endCallback: () -> Unit, successCallback: T.() -> Unit, cacheCallback: T.() -> Unit, failedCallback: String.() -> Unit) {
        build(responseClazz, startCallback, endCallback, successCallback, cacheCallback, failedCallback)
    }

    /**
     * @param responseClazz 回调类型
     * @param startCallback 调用网络接口之前的回调
     * @param endCallback 调用网络接口之后的回调
     * @param successCallback 成功回调方法
     * @param cacheCallback 本地缓存回调
     * @param failedCallback 失败回调方法
     */
    fun <T> request(responseClazz: Class<T>, startCallback: MercuryStartCallback, endCallback: MercuryEndCallback, successCallback: MercurySuccessCallback<T>, cacheCallback: MercuryCacheCallback<T>, failedCallback: MercuryFailedCallback) {
        build(responseClazz, startCallback, endCallback, successCallback, cacheCallback, failedCallback)
    }

    /**
     * 请求会根据Activity的销毁而销毁
     *
     * @param responseClazz 回调类型
     * @param successCallback 成功回调方法
     */
    fun <T> requestByLifecycle(responseClazz: Class<T>, successCallback: T.() -> Unit) {
        buildByLifecycle(responseClazz, {}, {}, successCallback, {}, {})
    }

    /**
     * 请求会根据Activity的销毁而销毁
     *
     * @param responseClazz 回调类型
     * @param successCallback 成功回调方法
     */
    fun <T> requestByLifecycle(responseClazz: Class<T>, successCallback: MercurySuccessCallback<T>) {
        buildByLifecycle(responseClazz, null, null, successCallback, null, null)
    }

    /**
     * 请求会根据Activity的销毁而销毁
     *
     * @param responseClazz 回调类型
     * @param successCallback 成功回调方法
     * @param failedCallback 失败回调方法
     */
    fun <T> requestByLifecycle(responseClazz: Class<T>, successCallback: T.() -> Unit, failedCallback: String.() -> Unit) {
        buildByLifecycle(responseClazz, {}, {}, successCallback, {}, failedCallback)
    }

    /**
     * 请求会根据Activity的销毁而销毁
     *
     * @param responseClazz 回调类型
     * @param successCallback 成功回调方法
     * @param failedCallback 失败回调方法
     */
    fun <T> requestByLifecycle(responseClazz: Class<T>, successCallback: MercurySuccessCallback<T>, failedCallback: MercuryFailedCallback) {
        buildByLifecycle(responseClazz, null, null, successCallback, null, failedCallback)
    }

    /**
     * 请求会根据Activity的销毁而销毁
     *
     * @param responseClazz 回调类型
     * @param startCallback 调用网络接口之前的回调
     * @param endCallback 调用网络接口之后的回调
     * @param successCallback 成功回调方法
     */
    fun <T> requestByLifecycle(responseClazz: Class<T>, startCallback: () -> Unit, endCallback: () -> Unit, successCallback: T.() -> Unit) {
        buildByLifecycle(responseClazz, startCallback, endCallback, successCallback, {}, {})
    }

    /**
     * 请求会根据Activity的销毁而销毁
     *
     * @param responseClazz 回调类型
     * @param startCallback 调用网络接口之前的回调
     * @param endCallback 调用网络接口之后的回调
     * @param successCallback 成功回调方法
     */
    fun <T> requestByLifecycle(responseClazz: Class<T>, startCallback: MercuryStartCallback, endCallback: MercuryEndCallback, successCallback: MercurySuccessCallback<T>) {
        buildByLifecycle(responseClazz, startCallback, endCallback, successCallback, null, null)
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
    fun <T> requestByLifecycle(responseClazz: Class<T>, startCallback: () -> Unit, endCallback: () -> Unit, successCallback: T.() -> Unit, failedCallback: String.() -> Unit) {
        buildByLifecycle(responseClazz, startCallback, endCallback, successCallback, {}, failedCallback)
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
    fun <T> requestByLifecycle(responseClazz: Class<T>, startCallback: MercuryStartCallback, endCallback: MercuryEndCallback, successCallback: MercurySuccessCallback<T>, failedCallback: MercuryFailedCallback) {
        buildByLifecycle(responseClazz, startCallback, endCallback, successCallback, null, failedCallback)
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
    fun <T> requestByLifecycle(responseClazz: Class<T>, startCallback: () -> Unit, endCallback: () -> Unit, successCallback: T.() -> Unit, cacheCallback: T.() -> Unit, failedCallback: String.() -> Unit) {
        buildByLifecycle(responseClazz, startCallback, endCallback, successCallback, cacheCallback, failedCallback)
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
    fun <T> requestByLifecycle(responseClazz: Class<T>, startCallback: MercuryStartCallback, endCallback: MercuryEndCallback, successCallback: MercurySuccessCallback<T>, cacheCallback: MercuryCacheCallback<T>, failedCallback: MercuryFailedCallback) {
        buildByLifecycle(responseClazz, startCallback, endCallback, successCallback, cacheCallback, failedCallback)
    }

    private fun <T> buildByLifecycle(responseClazz: Class<T>, startCallback: () -> Unit, endCallback: () -> Unit, successCallback: T.() -> Unit, cacheCallback: T.() -> Unit, failedCallback: String.() -> Unit) {
        tag()
        build(responseClazz, startCallback, endCallback, successCallback, cacheCallback, failedCallback)
    }

    private fun tag() {
        if (TextUtils.isEmpty(tag)) {
            val sb = StringBuilder()
            sb.append(Mercury.getCurActivity()?.javaClass?.name)
            sb.append(Mercury.getCurActivity()?.hashCode())
            sb.append("requestByLifecycle")
            tag = sb.toString()
        }
    }

    private fun <T> buildByLifecycle(responseClazz: Class<T>, startCallback: MercuryStartCallback?, endCallback: MercuryEndCallback?, successCallback: MercurySuccessCallback<T>, cacheCallback: MercuryCacheCallback<T>?, failedCallback: MercuryFailedCallback?) {
        tag()
        build(responseClazz, startCallback, endCallback, successCallback, cacheCallback, failedCallback)
    }

    private fun <T> build(responseClazz: Class<T>, startCallback: () -> Unit, endCallback: () -> Unit, successCallback: T.() -> Unit, cacheCallback: T.() -> Unit, failedCallback: String.() -> Unit) {

        startCallback.invoke()

        url = dealUrl()

        /** 判断是否使用缓存 */
        getCache(responseClazz, cacheCallback = { cacheCallback() })

        ThreadUtils
                .get(ThreadUtils.Type.CACHED)
                .apply {
                    callBack {
                        it ?: return@callBack
                        Mercury.mOkHttpClient?.newCall(it as Request)?.enqueue(object : Callback {
                            override fun onResponse(call: Call?, response: Response?) {
                                Mercury.handler.post {
                                    endCallback.invoke()
                                }
                                var body = response?.body()?.string()
                                println(">>>>>onResponse: $body<<<<<")
                                /** 先走全局过滤器 */
                                val global = Mercury.globalFilter?.body(body ?: "", responseClazz)
                                if (global?.success != null && !global.success) {
                                    failedCallback.invoke("")
                                    return
                                }
                                body = global?.body ?: body
                                println(">>>>>globalFilter body: $body<<<<<")
                                /** 再走本次请求过滤器 */
                                val thisFilter = filter?.body(body ?: "", responseClazz)
                                if (thisFilter?.success != null && !thisFilter.success) {
                                    failedCallback.invoke("")
                                    return
                                }
                                body = thisFilter?.body ?: body
                                println(">>>>>filter body: $body<<<<<")
                                Mercury.handler.post {
                                    val data = Gson().fromJson(body, responseClazz)
                                    if (null == data) {
                                        failedCallback.invoke("数据解析失败")
                                    } else {
                                        successCallback.invoke(data)
                                        /** 说明此时业务数据是正常的 判断是否存储 */
                                        store(body)
                                    }
                                }
                            }

                            override fun onFailure(call: Call?, e: IOException?) {
                                Mercury.handler.post {
                                    failedCallback.invoke(e?.message ?: "exception")
                                }
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

    private fun <T> build(responseClazz: Class<T>, startCallback: MercuryStartCallback?, endCallback: MercuryEndCallback?, successCallback: MercurySuccessCallback<T>, cacheCallback: MercuryCacheCallback<T>?, failedCallback: MercuryFailedCallback?) {

        startCallback?.callback()

        url = dealUrl()

        /** 判断是否使用缓存 */
        getCache(responseClazz, cacheCallback)

        ThreadUtils
                .get(ThreadUtils.Type.CACHED)
                .apply {
                    callBack {
                        it ?: return@callBack
                        Mercury.mOkHttpClient?.newCall(it as Request)?.enqueue(object : Callback {
                            override fun onResponse(call: Call?, response: Response?) {
                                Mercury.handler.post {
                                    endCallback?.callback()
                                }
                                var body = response?.body()?.string()
                                println(">>>>>onResponse: $body<<<<<")
                                /** 先走全局过滤器 */
                                val global = Mercury.globalFilter?.body(body ?: "", responseClazz)
                                if (global != null && !global.success) {
                                    failedCallback?.callback("")
                                    return
                                }
                                body = global?.body ?: body
                                println(">>>>>globalFilter body: $body<<<<<")
                                /** 再走本次请求过滤器 */
                                val thisFilter = filter?.body(body ?: "", responseClazz)
                                if (thisFilter != null && !thisFilter.success) {
                                    failedCallback?.callback("")
                                    return
                                }
                                body = thisFilter?.body ?: body
                                println(">>>>>filter body: $body<<<<<")
                                Mercury.handler.post {
                                    val data = Gson().fromJson(body, responseClazz)
                                    if (null == data) {
                                        failedCallback?.callback("数据解析失败")
                                    } else {
                                        successCallback.callback(data)
                                        /** 说明此时业务数据是正常的 判断是否存储 */
                                        store(body)
                                    }
                                }
                            }

                            override fun onFailure(call: Call?, e: IOException?) {
                                Mercury.handler.post {
                                    failedCallback?.callback(e?.message ?: "exception")
                                }
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

        val hostClass = this.javaClass.getAnnotation(Host::class.java) ?: null
        val host: String? = hostClass?.host
        val fields = this@MercuryRequest.javaClass.declaredFields
        val get = this.javaClass.getAnnotation(Get::class.java) ?: null

        if (null != get) {
            return Request.Builder()
                    .url(StringBuilder().apply {
                        var isAppend = true
                        append(host ?: Mercury.host)
                        append(url)
                        for (i in fields.indices) {
                            val field = fields[i]
                            val h = field.getAnnotation(Header::class.java)
                            if (null != h) {
                                continue
                            }
                            val path = field.getAnnotation(Path::class.java)
                            if (null != path) {
                                continue
                            }
                            if (isAppend) {
                                append("?")
                                isAppend = false
                            }
                            field.isAccessible = true
                            append(field.name)
                            append("=")
                            val encode = field.getAnnotation(Encode::class.java)
                            val params = if (encode?.value == true) {
                                field.get(this@MercuryRequest)?.let {
                                    Uri.encode(it.toString())
                                }
                            } else {
                                field.get(this@MercuryRequest)?.toString()
                            }
                            append(params)
                            if (i != fields.size - 1) {
                                append("&")
                            }
                        }
                    }.toString())
                    .apply {
                        buildHeaders(this@MercuryRequest, this)
                    }
                    .tag(tag)
                    .get()
                    .build()
        }

        val post = this.javaClass.getAnnotation(Post::class.java) ?: null
        if (null != post) {
            val type = this.javaClass.getAnnotation(ContentType::class.java)?.type
                    ?: Mercury.contentType
            val g = GsonBuilder().addSerializationExclusionStrategy(object : ExclusionStrategy {
                override fun shouldSkipField(f: FieldAttributes?): Boolean {
                    return this@MercuryRequest.shouldSkipField(f)
                }

                override fun shouldSkipClass(clazz: Class<*>?): Boolean {
                    return false
                }
            }).create()
            val requestBody: RequestBody =
                    when (type) {
                        MercuryContentType.JSON -> RequestBody.create(MediaType.parse(type), g.toJson(this@MercuryRequest))
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
                                    val file = it.get(this@MercuryRequest) as File
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
                    .url((host ?: Mercury.host) + url)
                    .tag(tag)
                    .post(requestBody)
                    .apply {
                        buildHeaders(this@MercuryRequest, this)
                    }
                    .build()
        }

        val delete = this.javaClass.getAnnotation(Delete::class.java) ?: null
        if (null != delete) {
            return Request.Builder()
                    .url((host ?: Mercury.host) + url)
                    .tag(tag)
                    .delete()
                    .apply {
                        buildHeaders(this@MercuryRequest, this)
                    }
                    .build()
        }
        return throw IllegalArgumentException("request must not null.")
    }

    private fun shouldSkipField(f: FieldAttributes?): Boolean {
        if (f == null) {
            return false
        }
        val h = f.getAnnotation(Header::class.java)
        if (null != h) {
            return true
        }
        val path = f.getAnnotation(Path::class.java)
        if (null != path) {
            return true
        }
        val ignore = f.getAnnotation(Ignore::class.java)
        if (null != ignore) {
            return true
        }
        return false
    }

    private fun buildHeaders(request: MercuryRequest, builder: Request.Builder) {
        request.getHeadersByAnnotation(builder, this@MercuryRequest.javaClass)
        if (this@MercuryRequest is MercuryBuildHeaders) {
            mercuryBuildHeaders = this@MercuryRequest
            mercuryBuildHeaders?.buildHeaders()?.mapValues { (k, v) -> builder.addHeader(k, Base64.encodeToString(v.toByteArray(), Base64.NO_WRAP)) }
        }
    }

    private fun <T> getHeadersByAnnotation(request: Request.Builder, clazz: Class<T>) {
        if (clazz == MercuryRequest::class.java) {
            return@getHeadersByAnnotation
        }
        val fields = clazz.declaredFields
        for (i in fields.indices) {
            val it = fields[i]
            it.isAccessible = true
            val h = it.getAnnotation(Header::class.java)
            if (null != h) {
                try {
                    val headers = it.get(this@MercuryRequest) as MutableMap<String, String>
                    request.headers(Headers.Builder().apply {
                        headers.mapValues { (k, v) -> add(k, Base64.encodeToString(v.toByteArray(), Base64.NO_WRAP)) }
                    }.build())
                    return@getHeadersByAnnotation
                } catch (e: ClassCastException) {
                    throw IllegalArgumentException("Header must be a MutableMap<String,String>.")
                }
            } else {
                if (i == fields.lastIndex) {
                    getHeadersByAnnotation(request, clazz.superclass)
                }
            }
        }
    }

    private fun <T> getCache(responseClazz: Class<T>, cacheCallback: T.() -> Unit) {

        this.javaClass.getAnnotation(Cache::class.java) ?: return@getCache
        if (null == this.javaClass.getAnnotation(Get::class.java)
                && null == this.javaClass.getAnnotation(Post::class.java)) {
            throw IllegalArgumentException("Method must be get or post.")
        }
        val key = generateKey() ?: return@getCache
        println(">>>>>key：$key<<<<<")
        ThreadUtils
                .get(ThreadUtils.Type.CACHED)
                .apply {
                    callBack {
                        it ?: return@callBack
                        Mercury.handler.post {
                            cacheCallback.invoke(it as T)
                        }
                    }
                    start(object : ThreadUtils.MercuryRunnable<T>() {
                        override fun execute(): T? {
                            return MercuryCache.get(key, responseClazz)
                        }
                    })
                }
    }

    private fun <T> getCache(responseClazz: Class<T>, cacheCallback: MercuryCacheCallback<T>?) {

        this.javaClass.getAnnotation(Cache::class.java) ?: return@getCache
        if (null == this.javaClass.getAnnotation(Get::class.java)
                && null == this.javaClass.getAnnotation(Post::class.java)) {
            throw IllegalArgumentException("Method must be get or post.")
        }
        val key = generateKey() ?: return@getCache
        println(">>>>>key：$key<<<<<")
        ThreadUtils
                .get(ThreadUtils.Type.CACHED)
                .apply {
                    callBack {
                        it ?: return@callBack
                        Mercury.handler.post {
                            cacheCallback?.callback(it as T)
                        }
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
        val key = generateKey() ?: return@store
        println(">>>>>key：$key<<<<<")
        ThreadUtils
                .get(ThreadUtils.Type.CACHED)
                .start {
                    MercuryCache.put(key, body)
                }
    }

    private fun generateKey(): String? {

        val fields = this@MercuryRequest.javaClass.declaredFields

        val url = this.javaClass.getAnnotation(Get::class.java)?.url
                ?: this.javaClass.getAnnotation(Post::class.java)?.url
                ?: return null
        val hostClass = this.javaClass.getAnnotation(Host::class.java) ?: null
        val host: String? = hostClass?.host
        val key = StringBuilder().apply {
            append(host ?: Mercury.host)
            append(mapUrl(fields, url))
            fields.forEach {
                append("&")
                it.isAccessible = true
                append("${it.name}=${it.get(this@MercuryRequest)}")
            }
        }.toString()

        return Base64.encodeToString(key.toByteArray(), Base64.NO_WRAP)
    }

    private fun dealUrl(): String {

        val fields = this@MercuryRequest.javaClass.declaredFields

        val url = this.javaClass.getAnnotation(Get::class.java)?.url
                ?: this.javaClass.getAnnotation(Post::class.java)?.url
                ?: this.javaClass.getAnnotation(Patch::class.java)?.url
                ?: this.javaClass.getAnnotation(Put::class.java)?.url
                ?: ""

        return mapUrl(fields, url)
    }

    private fun mapUrl(fields: Array<out Field>, url: String): String {
        var temp = url
        for (i in fields.indices) {
            val path = fields[i].getAnnotation(Path::class.java)
            path ?: continue
            val field = fields[i]
            field.isAccessible = true
            val value = if (TextUtils.isEmpty(path.value)) field.name else path.value
            temp = temp.replace("{$value}", field.get(this@MercuryRequest).toString(), true)
        }
        return temp
    }

}