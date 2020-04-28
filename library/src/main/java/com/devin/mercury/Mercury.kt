package com.devin.mercury

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.devin.mercury.config.MercuryConfig
import com.devin.mercury.config.MercuryFilter
import com.devin.mercury.utils.ThreadUtils
import okhttp3.OkHttpClient
import java.util.*

class Mercury {

    companion object {

        const val defaultMercuryConfigKey = "default_mercury_config"
        @JvmStatic
        var map = mutableMapOf<String, MercuryConfig>()
        var handler = Handler(Looper.getMainLooper())
        var activities = mutableSetOf<Activity>()
        private var stackOfActivities = Stack<Activity>()

        @JvmStatic
        fun init(builder: Builder) {
            Collections.synchronizedCollection(activities)
            map[defaultMercuryConfigKey] = builder
            registerActivityLifecycleCallbacks(defaultMercuryConfigKey)
        }

        @JvmStatic
        fun addMercuryConfig(config: MercuryConfig) {
            map[config.getConfigName()] = config
            registerActivityLifecycleCallbacks(config.getConfigName())
        }

        fun getMercuryConfig(configKey: String): MercuryConfig? {
            return map[configKey]
        }

        private fun registerActivityLifecycleCallbacks(configKey: String) {
            getMercuryConfig(configKey)?.getApplication()
                    ?: throw IllegalArgumentException("Context must be not null.")
            getMercuryConfig(configKey)?.getApplication()?.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
                override fun onActivityPaused(activity: Activity?) {}
                override fun onActivityResumed(activity: Activity?) {}
                override fun onActivityStarted(activity: Activity?) {}
                override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {}
                override fun onActivityStopped(activity: Activity?) {}
                override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
                    stackOfActivities.add(activity)
                }

                override fun onActivityDestroyed(activity: Activity?) {
                    stackOfActivities.remove(activity)
                    if (activities.contains(activity)) {
                        ThreadUtils.get(ThreadUtils.Type.CACHED).start {
                            cancelRequest(getMercuryConfig(configKey), activity?.javaClass?.name + activity?.hashCode())
                        }
                        activities.remove(activity)
                    } else {
                        /** 如果用户调用了 requestByLifecycle 方法 */
                        ThreadUtils.get(ThreadUtils.Type.CACHED).start {
                            cancelRequest(getMercuryConfig(configKey), activity?.javaClass?.name + activity?.hashCode() + "requestByLifecycle")
                        }
                    }
                }
            })
        }

        private fun cancelRequest(config: MercuryConfig?, tag: String) {
            config?.getOkClient()
                    ?: throw IllegalArgumentException("OkHttpClient must be not null.")
            config.getOkClient()?.dispatcher()?.runningCalls()?.forEach {
                println(">>>>>cancelRequest, runningCalls():  ${it.request().tag()}, ${it.isCanceled}")
                if (tag == it.request().tag() && !it.isCanceled) {
                    it.cancel()
                    println(">>>>>cancelRequest, runningCalls():  ${it.request().tag()}, ${it.isCanceled}")
                }
            }
            config.getOkClient()?.dispatcher()?.queuedCalls()?.forEach {
                println(">>>>>cancelRequest, queuedCalls():  ${it.request().tag()}, ${it.isCanceled}")
                if (tag == it.request().tag() && !it.isCanceled) {
                    it.cancel()
                    println(">>>>>cancelRequest queuedCalls():  ${it.request().tag()}, ${it.isCanceled}")
                }
            }
        }

        /**
         * 获取当前的Activity
         *
         * @return
         */
        fun getCurActivity(): Activity? {
            return stackOfActivities.lastElement()
        }

    }

    class Builder : MercuryConfig {
        override fun getApplication(): Application? {
            return context
        }

        override fun getOkClient(): OkHttpClient? {
            return okHttpClient
        }

        override fun getContentType(): String? {
            return contentType
        }

        override fun getHost(): String? {
            return host
        }

        override fun getGlobalFilter(): MercuryFilter? {
            return globalFilter
        }

        override fun getConfigName(): String {
            return defaultMercuryConfigKey
        }

        private var context: Application? = null
        private var okHttpClient: OkHttpClient? = null
        private var contentType: String? = null
        private var host: String? = null
        private var globalFilter: MercuryFilter? = null

        /** 设置全局的OkHttpClient */
        fun context(context: Application): Builder {
            this.context = context
            return this@Builder
        }

        /** 设置全局的OkHttpClient */
        fun okHttpClient(client: OkHttpClient): Builder {
            this.okHttpClient = client
            return this@Builder
        }

        /** 设置全局的ContentType */
        fun contentType(contentType: String): Builder {
            this.contentType = contentType
            return this@Builder
        }

        /** 设置全局Host */
        fun host(host: String): Builder {
            this.host = host
            return this@Builder
        }
    }
}