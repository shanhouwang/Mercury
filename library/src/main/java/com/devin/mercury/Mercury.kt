package com.devin.mercury

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.devin.mercury.config.MercuryFilter
import com.devin.mercury.utils.ThreadUtils
import okhttp3.OkHttpClient
import java.util.*

class Mercury {

    companion object {

        var context: Application? = null
        var mOkHttpClient: OkHttpClient? = null
        var contentType: String? = null
        var host: String? = null
        /** 全局过滤器 */
        var globalFilter: MercuryFilter? = null
        var handler = Handler(Looper.getMainLooper())
        var activities = mutableSetOf<Activity>()
        private var stackOfActivities = Stack<Activity>()


        @JvmStatic
        fun init(builder: Builder) {
            Collections.synchronizedCollection(activities)
            mOkHttpClient = builder.getOkClient()
            contentType = builder.getContentType()
            context = builder.getContext()
            host = builder.getHost()
            globalFilter = builder.getGlobalFilter()
            registerActivityLifecycleCallbacks()
        }

        private fun registerActivityLifecycleCallbacks() {
            context ?: throw IllegalArgumentException("Context must be not null.")
            context?.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
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
                    stackOfActivities.add(activity)
                }
                override fun onActivityDestroyed(activity: Activity?) {
                    stackOfActivities.remove(activity)
                    if (activities.contains(activity)) {
                        ThreadUtils.get(ThreadUtils.Type.CACHED).start {
                            cancelRequest(activity?.javaClass?.name + activity?.hashCode())
                        }
                        activities.remove(activity)
                    } else {
                        /** 如果用户调用了 requestByLifecycle 方法 */
                        ThreadUtils.get(ThreadUtils.Type.CACHED).start {
                            cancelRequest(activity?.javaClass?.name + activity?.hashCode() + "requestByLifecycle")
                        }
                    }
                }
            })
        }

        private fun cancelRequest(tag: String) {
            mOkHttpClient ?: throw IllegalArgumentException("OkHttpClient must be not null.")
            mOkHttpClient?.dispatcher()?.runningCalls()?.forEach {
                Log.d("cancelRequest", ">>>>>cancelRequest, runningCalls():  ${it.request().tag()}, ${it.isCanceled}")
                if (tag == it.request().tag() && !it.isCanceled) {
                    it.cancel()
                    Log.e("cancelRequest", ">>>>>cancelRequest, runningCalls():  ${it.request().tag()}, ${it.isCanceled}")
                }
            }

            mOkHttpClient?.dispatcher()?.queuedCalls()?.forEach {
                Log.d("cancelRequest", ">>>>>cancelRequest, queuedCalls():  ${it.request().tag()}, ${it.isCanceled}")
                if (tag == it.request().tag() && !it.isCanceled) {
                    it.cancel()
                    Log.e("cancelRequest", ">>>>>cancelRequest queuedCalls():  ${it.request().tag()}, ${it.isCanceled}")
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

    class Builder {

        private var context: Application? = null
        private var okHttpClient: OkHttpClient? = null
        private var contentType: String? = null
        private var host: String? = null
        private var globalFilter: MercuryFilter? = null

        fun getContext(): Application? {
            return context
        }

        fun getOkClient(): OkHttpClient? {
            return okHttpClient
        }

        fun getContentType(): String? {
            return contentType
        }

        fun getHost(): String? {
            return host
        }

        fun getGlobalFilter(): MercuryFilter? {
            return globalFilter
        }

        /** 设置全局的Context */
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

        /** 设置全局过滤器 */
        fun filter(filter: MercuryFilter): Builder {
            this.globalFilter = filter
            return this@Builder
        }
    }
}