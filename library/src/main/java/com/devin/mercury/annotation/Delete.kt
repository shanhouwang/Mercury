package com.devin.mercury.annotation

/**
 *
 * 调用一次和N次对系统产生的副作用是相同的，因此符合幂等性的
 *
 * @sample 1、/zoos/id：删除某个动物园
 *         2、/zoos/id/animals/id：删除某个指定动物园的指定动物
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Delete(val url: String = "")