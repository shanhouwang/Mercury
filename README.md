# Mercury
## 使用方法
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

