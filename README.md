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
        .filter(object : MercuryFilter {                                                                                         
            override fun body(body: String, clazz: Class<*>): MercuryFilterModel {
                // 比如：返回的JSON Body {"success":true,"code":0,"data":"7mAFuvXiU1"} 其中data字段是加密的，我们可以解密，然后在传给下一层
                // 还可以根据code做一些业务判断，比如code是-100的时候需要重新登录，如果model.success = false，会走Fail回调，如果model.success = true，会走Success回调，可以做一些拦截                                               
                val model = MercuryFilterModel()                                                                                 
                val parser = JsonParser()                                                                                        
                val jsonObject = parser.parse(body).asJsonObject                                                                 
                val data = jsonObject.get("data").asString                                                                       
                jsonObject.add("data", parser.parse(AES.INSTANCE.decryptAES(CommonBusinessStaticValue.AES_KEY, data)))           
                model.body = jsonObject.toString()                                                                               
                return model                                                                                                     
            }                                                                                                                    
        })                                                                                                                       
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
## Request
### 一般情况下只需要一行代码
```
LastContactsRequest(shopId).requestByLifecycle(LastContactsResponse::class.java,{})
```
### 可重载
```
BaseRequest("10086", "Devin")
	.request(BaseResponse::class.java
	, startCallback = {
		progressBar.visibility = View.VISIBLE
			println(">>>>>start: $请求开始的回调<<<<<")
 	}
	, endCallback = {
		progressBar.visibility = View.GONE
		println(">>>>>end: $请求结束的回调<<<<<")
	}
	, successCallback = {
		println(">>>>>success: $成功解析成实体后的回调<<<<<")
	}
	, cacheCallback = {
		println(">>>>>cache: $如果设置了Cache注解，Cache回调会首先返回<<<<<")
	}
	, failedCallback = {
		println(">>>>>fail: $失败的回调<<<<<")
	}
)
```
### requestByLifecycle：请求会根据Activity的销毁而销毁

```
MainRequest("10086", "Devin：$i")
	.requestByLifecycle(BaseResponse::class.java
		, startCallback = {
		progressBar.visibility = View.VISIBLE
		println(">>>>>start: $请求开始的回调<<<<<")
	}
	, endCallback = {
		progressBar.visibility = View.GONE
		println(">>>>>end: $请求结束的回调<<<<<")
	}
	, successCallback = {
		println(">>>>>success: $成功解析成实体后的回调<<<<<")
	}
	, cacheCallback = {
		println(">>>>>cache: $如果设置了Cache注解，Cache回调会首先返回<<<<<")
	}
	, failedCallback = {
		println(">>>>>fail: $失败的回调<<<<<")
	}
	)
}
```


