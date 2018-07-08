package com.devin.mercury

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.devin.mercury.utils.ThreadUtils
import okhttp3.OkHttpClient
import java.util.*

class Mercury {

    companion object {

        lateinit var context: Application
        lateinit var mOkHttpClient: OkHttpClient
        lateinit var contentType: String
        lateinit var host: String
        var handler = Handler(Looper.getMainLooper())
        var activities = mutableSetOf<Activity>()
        private var stackOfActivities = Stack<Activity>()


        fun init(builder: MercuryBuilder) {
            Collections.synchronizedCollection(activities)
            mOkHttpClient = builder.okHttpClient()
            contentType = builder.contentType()
            context = builder.getContext()
            host = builder.host()
            registerActivityLifecycleCallbacks()
        }

        private fun registerActivityLifecycleCallbacks(){
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
                            cancelRequest()
                        })
                    }
                }
            })
        }

        private fun cancelRequest(tag: String) {
            Mercury.mOkHttpClient.dispatcher().runningCalls().forEach {
                Log.d("cancelRequest", ">>>>>cancelRequest, runningCalls():  ${it.request().tag()}, ${it.isCanceled}")
                if (tag == it.request().tag() && !it.isCanceled) {
                    it.cancel()
                    Log.e("cancelRequest", ">>>>>cancelRequest, runningCalls():  ${it.request().tag()}, ${it.isCanceled}")
                }
            }

            Mercury.mOkHttpClient.dispatcher().queuedCalls().forEach {
                Log.d("cancelRequest", ">>>>>cancelRequest, queuedCalls():  ${it.request().tag()}, ${it.isCanceled}")
                if (tag == it.request().tag() && !it.isCanceled) {
                    it.cancel()
                    Log.e("cancelRequest", ">>>>>cancelRequest queuedCalls():  ${it.request().tag()}, ${it.isCanceled}")
                }
            }
        }

        private fun cancelRequest() {
            Mercury.mOkHttpClient.dispatcher().runningCalls().forEach {
                Log.d("cancelRequest", ">>>>>cancelRequest, thread：${Thread.currentThread().id}, runningCalls():  ${it.request().tag()}, ${it.isCanceled}")
                if (it.request().tag()?.toString()?.contains("requestByLifecycle") == true && !it.isCanceled) {
                    it.cancel()
                    Log.e("cancelRequest", ">>>>>cancelRequest, runningCalls():  ${it.request().tag()}, ${it.isCanceled}")
                }
            }

            Mercury.mOkHttpClient.dispatcher().queuedCalls().forEach {
                Log.d("cancelRequest", ">>>>>cancelRequest, thread：${Thread.currentThread().id}, queuedCalls():  ${it.request().tag()}, ${it.isCanceled}")
                if (it.request().tag()?.toString()?.contains("requestByLifecycle") == true && !it.isCanceled) {
                    it.cancel()
                    Log.e("cancelRequest", ">>>>>cancelRequest, queuedCalls():  ${it.request().tag()}, ${it.isCanceled}")
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

    interface MercuryBuilder {

        fun getContext(): Application

        fun okHttpClient(): OkHttpClient

        fun contentType(): String

        /** http://www.baidu.com/ */
        fun host(): String

    }
}