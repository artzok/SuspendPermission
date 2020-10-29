#### use suspend method request permission

```java
lifecycleScope.launch {
    val result = requestPermissions(listOf(Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION))
    Log.d(TAG, "request location permissions:$result")
}
```
