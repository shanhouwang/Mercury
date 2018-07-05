package com.devin.mercury.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Devin on 17/3/15.
 */
public class ThreadUtils {

    private static ExecutorService mCachedThreadPool;
    private static ScheduledExecutorService mScheduledThreadPool;
    private static ExecutorService mSingleThreadPool;
    private static Handler mHandler = new Handler(Looper.getMainLooper());

    private Type type;
    private CallBack callBack;

    private ThreadUtils() {
    }

    public static ThreadUtils get(Type type) {
        ThreadUtils util = new ThreadUtils();
        util.type = type;
        return util;
    }

    /**
     * 关闭所有定时及周期性任务
     *
     * @return
     */
    public static boolean shut() {
        if (mScheduledThreadPool == null) {
            return false;
        }
        mScheduledThreadPool.shutdown();
        return mScheduledThreadPool.isShutdown();
    }

    private ExecutorService build() {
        ExecutorService service = null;
        switch (type) {
            case CACHED:
                if (mCachedThreadPool == null || mCachedThreadPool.isShutdown()) {
                    mCachedThreadPool = Executors.newCachedThreadPool();
                }
                service = mCachedThreadPool;
                break;
            case CHAIN:
                if (mSingleThreadPool == null || mSingleThreadPool.isShutdown()) {
                    mSingleThreadPool = Executors.newSingleThreadExecutor();
                }
                service = mSingleThreadPool;
                break;
            case SCHEDULED:
                if (mScheduledThreadPool == null || mScheduledThreadPool.isShutdown()) {
                    mScheduledThreadPool = Executors.newScheduledThreadPool(10);
                }
                service = mScheduledThreadPool;
                break;
            default:
                break;
        }
        return service;
    }

    public void start(MercuryRunnable runnable) {
        runnable.setCallBack(callBack);
        build().execute(runnable);
    }

    public void start(Runnable runnable) {
        build().execute(runnable);
    }

    /**
     * 延迟 initialDelay 后 每 period 执行一次
     */
    public void scheduleWithFixedDelay(MercuryRunnable runnable, long initialDelay, long period, TimeUnit unit) {
        runnable.setCallBack(callBack);
        build();
        if (mScheduledThreadPool == null) {
            return;
        }
        mScheduledThreadPool.scheduleWithFixedDelay(runnable, initialDelay, period, unit);
    }

    /**
     * 延迟 delay 后执行
     */
    public void schedule(MercuryRunnable runnable, long delay, TimeUnit unit) {
        runnable.setCallBack(callBack);
        build();
        if (mScheduledThreadPool == null) {
            return;
        }
        mScheduledThreadPool.schedule(runnable, delay, unit);
    }

    public ThreadUtils callBack(CallBack callBack) {
        this.callBack = callBack;
        return this;
    }

    public enum Type {
        /**
         * 可灵活回收空闲线程，若无可回收，则新建线程
         */
        CACHED,

        /**
         * 它只会用唯一的工作线程来执行任务 保证所有任务按照指定顺序(FIFO, LIFO, 优先级)执行
         */
        CHAIN,

        /**
         * 支持定时及周期性任务执行
         */
        SCHEDULED
    }

    public interface CallBack<T> {

        void onResponse(T obj);
    }

    public abstract static class MercuryRunnable<T> implements Runnable {

        private CallBack callBack;

        public void setCallBack(CallBack callBack) {
            this.callBack = callBack;
        }

        public abstract T execute();

        @Override
        public void run() {
            final T obj = execute();
            if (callBack == null) {
                return;
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    callBack.onResponse(obj);
                }
            });
        }
    }
}
