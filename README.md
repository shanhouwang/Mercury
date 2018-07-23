# Mercury
## 简介

```
一个封装了Okhttp的library
```
## 引入项目

```
implementation 'com.devin:mercury:0.0.1'
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
### 3、Cache
* Request Model实体类注解方法必须是Get或者Post
* 第一次请求后，如果返回，会缓存下来，第二次同样的请求（url 和 请求参数一致）首先会走缓存的数据，然后会回调

```
@Cache
class MainRequest(var id: String, var name: String) : MercuryRequest()
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


