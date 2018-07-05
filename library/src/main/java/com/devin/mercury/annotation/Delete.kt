package com.devin.mercury.annotation

/**
 * 删除某一个资源
 */
@Target(AnnotationTarget.CLASS)
annotation class Delete(val url: String = "")