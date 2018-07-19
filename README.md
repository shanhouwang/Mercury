# Mercury
## 简介

```
一个封装了Okhttp的library
```
## 引入项目

```
implementation 'com.devin:mercury:0.0.1-beta-2'
```
## 初始化

```
Mercury.init(object : Mercury.MercuryBuilder {
	override fun host(): String {
		return "http://www.baidu.com/"
	}

	override fun getContext(): Application {
		return this@App
	}

	override fun okHttpClient(): OkHttpClient {
		return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                        .addInterceptor(ChuckInterceptor(this@App).showNotification(true))
                        .build()
	}

	override fun defaultContentType(): String {
		return MercuryContentType.JSON
	}
})
```
## 使用方法
## 构建RequestModel
### 1、Get
```
@Get(url = "")
class BaseRequest : MercuryRequest()
```
### 2、Post
```
@Post(url = "")
@ContentType(type = MercuryContentType.FORM)
class BaseRequest : MercuryRequest()
```
### request
```
BaseRequest("10086", "Devin")
	.request(BaseResponse::class.java
	, startCallback = {
		progressBar.visibility = View.VISIBLE
			println(">>>>>start: ${Thread.currentThread().id}<<<<<")
 	}
	, endCallback = {
		progressBar.visibility = View.GONE
		println(">>>>>end: ${Thread.currentThread().id}<<<<<")
	}
	, successCallback = {
		println(">>>>>success: ${Thread.currentThread().id}<<<<<")
	}
	, cacheCallback = {
		println(">>>>>cache: ${Thread.currentThread().id}<<<<<")
	}
	, failedCallback = {
		println(">>>>>fail: ${Thread.currentThread().id}<<<<<")
	}
)
```
### requestByLifecycle：请求会根据Activity的销毁而销毁

```
MainRequest("10086", "Devin：$i")
	.requestByLifecycle(BaseResponse::class.java
		, startCallback = {
		progressBar.visibility = View.VISIBLE
		println(">>>>>start: ${Thread.currentThread().id}<<<<<")
	}
	, endCallback = {
		progressBar.visibility = View.GONE
		println(">>>>>end: ${Thread.currentThread().id}<<<<<")
	}
	, successCallback = {
		println(">>>>>success: ${Thread.currentThread().id}<<<<<")
	}
	, cacheCallback = {
		println(">>>>>cache: ${Thread.currentThread().id}<<<<<")
	}
	, failedCallback = {
		println(">>>>>fail: ${Thread.currentThread().id}<<<<<")
	}
	)
}
```


