# Mercury
## 简介

```
一个封装了Okhttp的library
```
## 引入项目

```
implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.2.50"
implementation 'com.devin:mercury:0.0.6'
implementation 'com.squareup.okhttp3:okhttp:3.10.0'
implementation 'com.google.code.gson:gson:2.8.0'
```
## 初始化
### 主工程里面的请求
```
Mercury.init(Mercury.Builder()                                                                        
        .context(this@App)                                                                            
        .host("http://www.baidu.com/")                                                                
        .okHttpClient(OkHttpClient.Builder()                                                          
                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)) 
                .addInterceptor(ChuckInterceptor(this@App).showNotification(true))                    
                .build())                                                                             
        .contentType(MercuryContentType.JSON))                                                        
```
### 项目中可能还需要单独配置一套OkhttpClient
```
Mercury.addMercuryConfig(object : MercuryConfig {                                                     
    override fun getApplication(): Application? {                                                     
        return this@App                                                                               
    }                                                                                                 
                                                                                                      
    override fun getOkClient(): OkHttpClient? {                                                       
        return OkHttpClient.Builder()                                                                 
                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)) 
                .addInterceptor(ChuckInterceptor(this@App).showNotification(true))                    
                .build()                                                                              
    }                                                                                                 
                                                                                                      
    override fun getContentType(): String? {                                                          
        return MercuryContentType.JSON                                                                
    }                                                                                                 
                                                                                                      
    override fun getHost(): String? {                                                                 
        return "https://www.im.com/"                                                               
    }                                                                                                 
                                                                                                      
    override fun getGlobalFilter(): MercuryFilter? {
        // 可以做加密解密等操作                                                  
        return null                                                                                   
    }                                                                                                 
                                                                                                      
    override fun getConfigName(): String {                                                            
        return "IM"                                                                                   
    }                                                                                                 
})                                                                                                    
```
## 使用方法
## 构建RequestModel
### 1、Get
```
@Get(url = "")
class BaseRequest : MercuryRequest() // 此次请求会被默认的MercuryConfig调用
```
```
@Get(url = "")
class IMBaseRequest : MercuryRequest(“IM”) // 此次请求会被叫“IM”的MercuryConfig调用
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


