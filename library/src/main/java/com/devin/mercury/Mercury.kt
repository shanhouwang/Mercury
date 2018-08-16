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

        /** 初始化开关 */
        var init: Boolean = false
        var context: Application? = null
        var mOkHttpClient: OkHttpClient? = null
        var contentType: String? = null
        var host: String? = null
        /** 全局过滤器 */
        var globalFilter: MercuryFilter? = null
        var handler = Handler(Looper.getMainLooper())
        var activities = mutableSetOf<Activity>()
        private var stackOfActivities = Stack<Activity>()


        fun init(builder: Builder) {
            if (init) {
                return@init
            }
            Collections.synchronizedCollection(activities)
            mOkHttpClient = builder.okHttpClient
            contentType = builder.contentType
            context = builder.context
            host = builder.host
            globalFilter = builder.globalFilter
            registerActivityLifecycleCallbacks()
            init = true
        }

        private fun registerActivityLifecycleCallbacks() {
            Mercury.context ?: throw IllegalArgumentException("Context must be not null.")
            Mercury.context?.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
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
                        ThreadUtils.get(ThreadUtils.Type.CACHED).start({
                            cancelRequest(activity?.javaClass?.name + activity?.hashCode())
                        })
                        activities.remove(activity)
                    } else {
                        /** 如果用户调用了 requestByLifecycle 方法 */
                        ThreadUtils.get(ThreadUtils.Type.CACHED).start({
                            cancelRequest(activity?.javaClass?.name + activity?.hashCode() + "requestByLifecycle")
                        })
                    }
                }
            })
        }

        private fun cancelRequest(tag: String) {
            Mercury.mOkHttpClient
                    ?: throw IllegalArgumentException("OkHttpClient must be not null.")
            Mercury.mOkHttpClient?.dispatcher()?.runningCalls()?.forEach {
                Log.d("cancelRequest", ">>>>>cancelRequest, runningCalls():  ${it.request().tag()}, ${it.isCanceled}")
                if (tag == it.request().tag() && !it.isCanceled) {
                    it.cancel()
                    Log.e("cancelRequest", ">>>>>cancelRequest, runningCalls():  ${it.request().tag()}, ${it.isCanceled}")
                }
            }

            Mercury.mOkHttpClient?.dispatcher()?.queuedCalls()?.forEach {
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

        var context: Application? = null
        var okHttpClient: OkHttpClient? = null
        var contentType: String? = null
        var host: String? = null
        var globalFilter: MercuryFilter? = null

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