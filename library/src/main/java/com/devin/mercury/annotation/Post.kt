package com.devin.mercury.annotation

/**
 * 一个非幂等方法，调用多次，都将产生新的资源
 *
 * @sample /zoos：新建一个动物园
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Post(val url: String = "")