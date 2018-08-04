package com.devin.mercury.annotation

/**
 * 对同一URI进行多次PUT的副作用和一次PUT是相同的，因此，PUT方法具有幂等性
 *
 * @sample /zoos/id：更新某个指定动物园的信息（提供该动物园的全部信息）
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Put(val url: String = "")