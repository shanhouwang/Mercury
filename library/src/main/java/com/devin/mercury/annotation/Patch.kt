package com.devin.mercury.annotation

/**
 * @sample /zoos/id：更新某个指定动物园的信息（提供该动物园的部分信息）
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Patch(val url: String)