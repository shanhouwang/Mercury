package com.devin.mercury.annotation

/**
 * 仅仅获取Http头信息
 */
@Target(AnnotationTarget.CLASS)
annotation class Head(val url: String = "")