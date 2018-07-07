package com.devin.mercury

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import okhttp3.OkHttpClient
import java.util.*
import kotlin.collections.HashSet

class Mercury {

    companion object {

        lateinit var context: Application
        lateinit var mOkHttpClient: OkHttpClient
        lateinit var contentType: String
        lateinit var host: String
        var handler = Handler(Looper.getMainLooper())
        var activities = HashSet<Activity>()


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
                }

                override fun onActivityDestroyed(activity: Activity?) {
                    while (activities.iterator().hasNext()) {
                        var it = activities.iterator().next()
                        if (it == activity) {
                            cancelRequest(it)
                            activities.remove(it)
                            break
                        }
                    }
                }
            })
        }

        private fun cancelRequest(activity: Activity?) {
            doIt(activity?.javaClass?.name + activity?.hashCode())
        }

        private fun doIt(tag: String) {
            println(">>>>>doIt, tag: $tag<<<<<")
            Mercury.mOkHttpClient.dispatcher().runningCalls().forEach {
                Log.d(">>>>>doIt", ">>>>>doIt, runningCalls():  ${it.request().tag()}, ${it.isCanceled}")
                if (tag == it.request().tag() && !it.isCanceled) {
                    it.cancel()
                    Log.e(">>>>>doIt", ">>>>>doIt, runningCalls():  ${it.request().tag()}, ${it.isCanceled}")
                }
            }

            Mercury.mOkHttpClient.dispatcher().queuedCalls().forEach {
                Log.d(">>>>>doIt", ">>>>>doIt, queuedCalls():  ${it.request().tag()}, ${it.isCanceled}")
                if (tag == it.request().tag() && !it.isCanceled) {
                    it.cancel()
                    Log.e(">>>>>doIt", ">>>>>doIt, queuedCalls():  ${it.request().tag()}, ${it.isCanceled}")
                }
            }
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