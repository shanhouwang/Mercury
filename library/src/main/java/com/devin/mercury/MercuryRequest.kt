package com.devin.model.mercury

import android.app.Activity
import android.net.Uri
import android.text.TextUtils
import android.util.Base64
import com.devin.mercury.Mercury
import com.devin.mercury.MercuryContentType
import com.devin.mercury.annotation.*
import com.devin.mercury.annotation.Cache
import com.devin.mercury.config.MercuryConfig
import com.devin.mercury.config.MercuryFilter
import com.devin.mercury.model.*
import com.devin.mercury.utils.MLog
import com.devin.mercury.utils.MercuryCache
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.*
import okhttp3.*
import java.io.File
import java.io.IOException
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type


/**
 * @author devin
 */
abstract class MercuryRequest<T> {

    @Ignore
    private var tag: String? = null

    @Ignore
    private var mercuryBuildHeaders: MercuryBuildHeaders? = null

    @Ignore
    private var filter: MercuryFilter? = null

    @Ignore
    private var configKey: String? = null

    @Ignore
    private val fields by lazy { this@MercuryRequest.javaClass.declaredFields }

    @Ignore
    private val getAnnotation: Get? by lazy { this@MercuryRequest.javaClass.getAnnotation(Get::class.java) }

    @Ignore
    private val postAnnotation: Post? by lazy { this@MercuryRequest.javaClass.getAnnotation(Post::class.java) }

    @Ignore
    private val patchAnnotation: Patch? by lazy { this@MercuryRequest.javaClass.getAnnotation(Patch::class.java) }

    @Ignore
    private val putAnnotation: Put? by lazy { this@MercuryRequest.javaClass.getAnnotation(Put::class.java) }

    @Ignore
    private val deleteAnnotation: Delete? by lazy { this@MercuryRequest.javaClass.getAnnotation(Delete::class.java) }

    @Ignore
    private val hostAnnotation: Host? by lazy { this@MercuryRequest.javaClass.getAnnotation(Host::class.java) }

    @Ignore
    private val cacheAnnotation: Cache? by lazy { this@MercuryRequest.javaClass.getAnnotation(Cache::class.java) }

    constructor()

    constructor(configKey: String) {
        this.configKey = configKey
    }

    /**
     * 请求会根据Activity的销毁而取消
     */
    fun lifecycle(activity: Activity): MercuryRequest<T> {
        tag = activity.javaClass.name + activity.hashCode()
        Mercury.activities.add(activity)
        return this
    }

    /** 本次请求的过滤器 */
    fun filter(filter: MercuryFilter): MercuryRequest<T> {
        this.filter = filter
        return this
    }

    /**
     * @param successCallback 成功回调方法
     */
    fun request(successCallback: T.() -> Unit) {
        build({}, {}, successCallback, {}, {})
    }

    /**
     * @param successCallback 成功回调方法
     */
    fun request(successCallback: MercurySuccessCallback<T>) {
        build(null, null, successCallback, null, null)
    }

    /**
     * @param successCallback 成功回调方法
     * @param failedCallback 失败回调方法
     */
    fun request(successCallback: T.() -> Unit, failedCallback: MercuryException.() -> Unit) {
        build({}, {}, successCallback, {}, failedCallback)
    }

    /**
     * @param successCallback 成功回调方法
     * @param failedCallback 失败回调方法
     */
    fun request(successCallback: MercurySuccessCallback<T>, failedCallback: MercuryFailedCallback) {
        build(null, null, successCallback, null, failedCallback)
    }

    /**
     * @param startCallback 调用网络接口之前的回调
     * @param endCallback 调用网络接口之后的回调
     * @param successCallback 成功回调方法
     */
    fun request(startCallback: () -> Unit, endCallback: () -> Unit, successCallback: T.() -> Unit) {
        build(startCallback, endCallback, successCallback, {}, {})
    }

    /**
     * @param startCallback 调用网络接口之前的回调
     * @param endCallback 调用网络接口之后的回调
     * @param successCallback 成功回调方法
     */
    fun request(startCallback: MercuryStartCallback, endCallback: MercuryEndCallback, successCallback: MercurySuccessCallback<T>) {
        build(startCallback, endCallback, successCallback, null, null)
    }

    /**
     * @param startCallback 调用网络接口之前的回调
     * @param endCallback 调用网络接口之后的回调
     * @param successCallback 成功回调方法
     * @param failedCallback 失败回调方法
     */
    fun request(startCallback: () -> Unit, endCallback: () -> Unit, successCallback: T.() -> Unit, failedCallback: MercuryException.() -> Unit) {
        build(startCallback, endCallback, successCallback, {}, failedCallback)
    }

    /**
     * @param startCallback 调用网络接口之前的回调
     * @param endCallback 调用网络接口之后的回调
     * @param successCallback 成功回调方法
     * @param failedCallback 失败回调方法
     */
    fun request(startCallback: MercuryStartCallback, endCallback: MercuryEndCallback, successCallback: MercurySuccessCallback<T>, failedCallback: MercuryFailedCallback) {
        build(startCallback, endCallback, successCallback, null, failedCallback)
    }

    /**
     * @param startCallback 调用网络接口之前的回调
     * @param endCallback 调用网络接口之后的回调
     * @param successCallback 成功回调方法
     * @param cacheCallback 本地缓存回调
     * @param failedCallback 失败回调方法
     */
    fun request(startCallback: () -> Unit, endCallback: () -> Unit, successCallback: T.() -> Unit, cacheCallback: T.() -> Unit, failedCallback: MercuryException.() -> Unit) {
        build(startCallback, endCallback, successCallback, cacheCallback, failedCallback)
    }

    /**
     * @param startCallback 调用网络接口之前的回调
     * @param endCallback 调用网络接口之后的回调
     * @param successCallback 成功回调方法
     * @param cacheCallback 本地缓存回调
     * @param failedCallback 失败回调方法
     */
    fun request(startCallback: MercuryStartCallback, endCallback: MercuryEndCallback, successCallback: MercurySuccessCallback<T>, cacheCallback: MercuryCacheCallback<T>, failedCallback: MercuryFailedCallback) {
        build(startCallback, endCallback, successCallback, cacheCallback, failedCallback)
    }

    /**
     * 请求会根据Activity的销毁而销毁
     *
     * @param successCallback 成功回调方法
     */
    fun requestByLifecycle(successCallback: T.() -> Unit) {
        buildByLifecycle({}, {}, successCallback, {}, {})
    }

    /**
     * 请求会根据Activity的销毁而销毁
     *
     * @param successCallback 成功回调方法
     */
    fun requestByLifecycle(successCallback: MercurySuccessCallback<T>) {
        buildByLifecycle(null, null, successCallback, null, null)
    }

    /**
     * 请求会根据Activity的销毁而销毁
     *
     * @param successCallback 成功回调方法
     * @param failedCallback 失败回调方法
     */
    fun requestByLifecycle(successCallback: T.() -> Unit, failedCallback: MercuryException.() -> Unit) {
        buildByLifecycle({}, {}, successCallback, {}, failedCallback)
    }

    /**
     * 请求会根据Activity的销毁而销毁
     *
     * @param successCallback 成功回调方法
     * @param failedCallback 失败回调方法
     */
    fun requestByLifecycle(successCallback: MercurySuccessCallback<T>, failedCallback: MercuryFailedCallback) {
        buildByLifecycle(null, null, successCallback, null, failedCallback)
    }

    /**
     * 请求会根据Activity的销毁而销毁
     *
     * @param startCallback 调用网络接口之前的回调
     * @param endCallback 调用网络接口之后的回调
     * @param successCallback 成功回调方法
     */
    fun requestByLifecycle(startCallback: () -> Unit, endCallback: () -> Unit, successCallback: T.() -> Unit) {
        buildByLifecycle(startCallback, endCallback, successCallback, {}, {})
    }

    /**
     * 请求会根据Activity的销毁而销毁
     *
     * @param startCallback 调用网络接口之前的回调
     * @param endCallback 调用网络接口之后的回调
     * @param successCallback 成功回调方法
     */
    fun requestByLifecycle(startCallback: MercuryStartCallback, endCallback: MercuryEndCallback, successCallback: MercurySuccessCallback<T>) {
        buildByLifecycle(startCallback, endCallback, successCallback, null, null)
    }

    /**
     * 请求会根据Activity的销毁而销毁
     *
     * @param startCallback 调用网络接口之前的回调
     * @param endCallback 调用网络接口之后的回调
     * @param successCallback 成功回调方法
     * @param failedCallback 失败回调方法
     */
    fun requestByLifecycle(startCallback: () -> Unit, endCallback: () -> Unit, successCallback: T.() -> Unit, failedCallback: MercuryException.() -> Unit) {
        buildByLifecycle(startCallback, endCallback, successCallback, {}, failedCallback)
    }

    /**
     * 请求会根据Activity的销毁而销毁
     *
     * @param startCallback 调用网络接口之前的回调
     * @param endCallback 调用网络接口之后的回调
     * @param successCallback 成功回调方法
     * @param failedCallback 失败回调方法
     */
    fun requestByLifecycle(startCallback: MercuryStartCallback, endCallback: MercuryEndCallback, successCallback: MercurySuccessCallback<T>, failedCallback: MercuryFailedCallback) {
        buildByLifecycle(startCallback, endCallback, successCallback, null, failedCallback)
    }

    /**
     * 请求会根据Activity的销毁而销毁
     *
     * @param startCallback 调用网络接口之前的回调
     * @param endCallback 调用网络接口之后的回调
     * @param successCallback 成功回调方法
     * @param cacheCallback 本地缓存回调
     * @param failedCallback 失败回调方法
     */
    fun requestByLifecycle(startCallback: () -> Unit, endCallback: () -> Unit, successCallback: T.() -> Unit, cacheCallback: T.() -> Unit, failedCallback: MercuryException.() -> Unit) {
        buildByLifecycle(startCallback, endCallback, successCallback, cacheCallback, failedCallback)
    }

    /**
     * 请求会根据Activity的销毁而销毁
     *
     * @param startCallback 调用网络接口之前的回调
     * @param endCallback 调用网络接口之后的回调
     * @param successCallback 成功回调方法
     * @param cacheCallback 本地缓存回调
     * @param failedCallback 失败回调方法
     */
    fun requestByLifecycle(startCallback: MercuryStartCallback, endCallback: MercuryEndCallback, successCallback: MercurySuccessCallback<T>, cacheCallback: MercuryCacheCallback<T>, failedCallback: MercuryFailedCallback) {
        buildByLifecycle(startCallback, endCallback, successCallback, cacheCallback, failedCallback)
    }

    private fun buildByLifecycle(startCallback: () -> Unit, endCallback: () -> Unit, successCallback: T.() -> Unit, cacheCallback: T.() -> Unit, failedCallback: MercuryException.() -> Unit) {
        tag()
        build(startCallback, endCallback, successCallback, cacheCallback, failedCallback)
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

    private fun buildByLifecycle(startCallback: MercuryStartCallback?, endCallback: MercuryEndCallback?, successCallback: MercurySuccessCallback<T>, cacheCallback: MercuryCacheCallback<T>?, failedCallback: MercuryFailedCallback?) {
        tag()
        build(startCallback, endCallback, successCallback, cacheCallback, failedCallback)
    }

    private fun build(startCallback: () -> Unit, endCallback: () -> Unit, successCallback: T.() -> Unit, cacheCallback: T.() -> Unit, failedCallback: MercuryException.() -> Unit) = runBlocking<Unit> {
        "build".getThreadName()
        startCallback.invoke()
        /** 判断是否使用缓存 */
        getCache(cacheCallback)

        getMercuryConfig()?.getOkClient()?.newCall(buildRequest(dealUrl()))?.enqueue(object : Callback {
            override fun onResponse(call: Call?, response: Response?) {
                doResponse(endCallback, response, failedCallback, successCallback)
            }

            override fun onFailure(call: Call?, e: IOException?) {
                Mercury.handler.post {
                    failedCallback.invoke(MercuryException(IO_EXCEPTION, e?.message ?: ""))
                }
            }
        })
    }

    private fun CoroutineScope.doResponse(endCallback: () -> Unit, response: Response?, failedCallback: MercuryException.() -> Unit, successCallback: T.() -> Unit) {
        Mercury.handler.post {
            endCallback.invoke()
        }
        var body = response?.body()?.string()
        MLog.d(">>>>>onResponse: $body<<<<<")
        /** 先走全局过滤器 */
        val global = getMercuryConfig()?.getGlobalFilter()?.body(body ?: "", getType())
        if (global?.success != null && !global.success) {
            failedCallback.invoke(global.exception ?: MercuryException(OTHER_EXCEPTION, ""))
            return
        }
        body = global?.body ?: body
        MLog.d(">>>>>globalFilter body: $body<<<<<")
        /** 再走本次请求过滤器 */
        val thisFilter = filter?.body(body ?: "", getType())
        if (thisFilter?.success != null && !thisFilter.success) {
            failedCallback.invoke(thisFilter.exception
                    ?: MercuryException(OTHER_EXCEPTION, ""))
            return
        }
        body = thisFilter?.body ?: body
        MLog.d(">>>>>filter body: $body<<<<<")
        Mercury.handler.post {
            try {
                val data = Gson().fromJson(body, getType()) as T
                successCallback.invoke(data)
                /** 说明此时业务数据是正常的 判断是否存储 */
                async {
                    store(body)
                }
            } catch (e: Exception) {
                failedCallback.invoke(MercuryException(DATA_PARSER_EXCEPTION, "数据解析失败"))
            }
        }
    }

    private fun getMercuryConfig(): MercuryConfig? {
        return Mercury.map[configKey ?: Mercury.defaultMercuryConfigKey]
    }

    private fun build(startCallback: MercuryStartCallback?, endCallback: MercuryEndCallback?, successCallback: MercurySuccessCallback<T>, cacheCallback: MercuryCacheCallback<T>?, failedCallback: MercuryFailedCallback?) = runBlocking<Unit> {
        build({ startCallback?.callback() }, { endCallback?.callback() }, { successCallback?.callback(this) }, { cacheCallback?.callback(this) }, { failedCallback?.callback(this) })
    }

    private suspend fun buildRequest(url: String): Request = withContext(Dispatchers.Default) {
        "buildRequest".getThreadName()
        val host: String? = hostAnnotation?.host
        if (null != getAnnotation) {
            return@withContext Request.Builder()
                    .url(appendGetMethodUrl(host, url, fields))
                    .apply {
                        buildHeaders(this@MercuryRequest, this)
                    }
                    .tag(tag)
                    .get()
                    .build()
        }
        if (null != postAnnotation) {
            val type = this@MercuryRequest.javaClass.getAnnotation(ContentType::class.java)?.type
                    ?: getMercuryConfig()?.getContentType()
            val g = GsonBuilder().addSerializationExclusionStrategy(object : ExclusionStrategy {
                override fun shouldSkipField(attributes: FieldAttributes?): Boolean {
                    return this@MercuryRequest.shouldSkipField(attributes)
                }

                override fun shouldSkipClass(clazz: Class<*>?): Boolean {
                    return false
                }
            }).create()
            val requestBody: RequestBody = when (type) {
                MercuryContentType.JSON -> RequestBody.create(MediaType.parse(type), g.toJson(this@MercuryRequest))
                MercuryContentType.FORM -> FormBody.Builder().apply {
                    fields?.forEach {
                        it.isAccessible = true
                        MLog.d(">>>>>${it.name}, ${it.type}, ${it.get(this@MercuryRequest)}<<<<<")
                        addEncoded(it.name, it.get(this@MercuryRequest)?.toString())
                    }
                }.build()
                MercuryContentType.FORM_DATA -> MultipartBody.Builder().apply {
                    fields?.forEach {
                        if (it.type == File::class.java) {
                            val file = it.get(this@MercuryRequest) as File
                            addFormDataPart(it.name, file.name, RequestBody.create(MediaType.parse("application/octet-stream"), file))
                        } else {
                            addFormDataPart(it.name, it.get(this@MercuryRequest).toString())
                        }
                    }
                }.build()
                else -> throw IllegalArgumentException("not find content type $type.")
            }
            return@withContext Request.Builder()
                    .url((host ?: getMercuryConfig()?.getHost()) + url)
                    .tag(tag)
                    .post(requestBody)
                    .apply {
                        buildHeaders(this@MercuryRequest, this)
                    }
                    .build()
        }

        if (null != deleteAnnotation) {
            return@withContext Request.Builder()
                    .url((host ?: getMercuryConfig()?.getHost()) + url)
                    .tag(tag)
                    .delete()
                    .apply {
                        buildHeaders(this@MercuryRequest, this)
                    }
                    .build()
        }
        throw IllegalArgumentException("request must not null.")
    }

    private suspend fun appendGetMethodUrl(host: String?, url: String, fields: Array<out Field>): String = withContext(Dispatchers.Default) {
        "appendGetMethodUrl".getThreadName()
        StringBuilder().apply {
            var isAppend = true
            append(host ?: getMercuryConfig()?.getHost())
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
        }.toString()
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

    private suspend fun buildHeaders(request: MercuryRequest<T>, builder: Request.Builder) = withContext(Dispatchers.Default) {
        "buildHeaders".getThreadName()
        request.getHeadersByAnnotation(builder, this@MercuryRequest.javaClass)
        if (this@MercuryRequest is MercuryBuildHeaders) {
            mercuryBuildHeaders = this@MercuryRequest
            mercuryBuildHeaders?.buildHeaders()?.mapValues { (k, v) -> builder.addHeader(k, Base64.encodeToString(v.toByteArray(), Base64.NO_WRAP)) }
        }
    }

    private suspend fun <T> getHeadersByAnnotation(request: Request.Builder, clazz: Class<T>): Unit = withContext(Dispatchers.Default) {
        "getHeadersByAnnotation".getThreadName()
        if (clazz == MercuryRequest::class.java) {
            return@withContext
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
                    return@withContext
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

    private suspend fun getCache(cacheCallback: T.() -> Unit) = withContext(Dispatchers.Default) {
        doCache(cacheCallback)
    }

    private suspend fun MercuryRequest<T>.doCache(cacheCallback: T.() -> Unit) {
        "getCache".getThreadName()
        cacheAnnotation ?: return
        if (null == getAnnotation && null == postAnnotation) {
            throw IllegalArgumentException("Method must be get or post.")
        }
        val key = generateKey() ?: return
        MLog.d(">>>>>key：$key<<<<<")
        Mercury.handler.post {
            val data = getMercuryConfig()?.let { MercuryCache.get(it, key, getType() as Class<T>) }
            data?.let {
                cacheCallback.invoke(it as T)
            }
        }
    }

    private suspend fun getCache(cacheCallback: MercuryCacheCallback<T>?) = withContext(Dispatchers.Default) {
        doCache {
            cacheCallback?.callback(this)
        }
    }

    private suspend fun store(body: String?) = withContext(Dispatchers.Default) {
        "store".getThreadName()
        body ?: return@withContext
        cacheAnnotation ?: return@withContext
        val key = generateKey() ?: return@withContext
        MLog.d(">>>>>key：$key<<<<<")
        getMercuryConfig()?.let { MercuryCache.put(it, key, body) }
    }

    private suspend fun generateKey(): String? = withContext(Dispatchers.Default) {
        "generateKey".getThreadName()
        val url = getAnnotation?.url ?: postAnnotation?.url ?: return@withContext null
        val host: String? = hostAnnotation?.host
        val key = StringBuilder().apply {
            append(host ?: getMercuryConfig()?.getHost())
            append(mapUrl(fields, url))
            fields.forEach {
                append("&")
                it.isAccessible = true
                append("${it.name}=${it.get(this@MercuryRequest)}")
            }
        }.toString()

        return@withContext Base64.encodeToString(key.toByteArray(), Base64.NO_WRAP)
    }

    private suspend fun dealUrl() = withContext(Dispatchers.Default) {
        "dealUrl".getThreadName()
        val url = getAnnotation?.url ?: postAnnotation?.url ?: patchAnnotation?.url
        ?: putAnnotation?.url ?: deleteAnnotation?.url ?: ""
        return@withContext mapUrl(fields, url)
    }

    private suspend fun mapUrl(fields: Array<out Field>, url: String): String = withContext(Dispatchers.Default) {
        "mapUrl, $url".getThreadName()
        var temp = url
        for (i in fields.indices) {
            val path = fields[i].getAnnotation(Path::class.java)
            path ?: continue
            val field = fields[i]
            field.isAccessible = true
            val value = if (TextUtils.isEmpty(path.value)) field.name else path.value
            temp = temp.replace("{$value}", field.get(this@MercuryRequest).toString(), true)
        }
        return@withContext temp
    }

    private fun String.getThreadName() {
        MLog.d(">>>>>coroutines: $this, ${Thread.currentThread().name}")
    }

    private fun getType(): Type {
        val gs = this.javaClass.genericSuperclass
        if (gs is ParameterizedType && gs.actualTypeArguments.isNotEmpty()) {
            return gs.actualTypeArguments[0]
        }
        throw IllegalArgumentException("MercuryRequest 必须加返回值类型（泛型）")
    }

}