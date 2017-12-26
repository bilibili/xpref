Xpref
====

A SharedPreferences' wrapper that truly supported sharing data across multi-process

### Principle

ContentProvider is designed to provide content between multiple applications that means it supported
sharing data between multi-process. Use it to wrap the SharedPreferences can make the latter truly
cross-process sharing data

### Usage

Add dependency to your gradle script:

```
dependencies {
    implementation 'com.bilibili.lib:x-pref:1.0'
}
```

Note that this library is written with kotlin 1.2.10.

1. Gets the default SharedPreferences which is typically used in the Settings of an APP.

```kotlin
Xpref.getDefaultSharedPreferences(context)
```

2. Gets a SharedPreferences with specific named.

```kotlin
val name = "awesome"
Xpref.getSharedPreferences(context, name)
```

3. Extension in Kotlin.

You can declare extension functions in somewhere on your need for better convenience usage like following:

```kotlin
fun <T: ContextWrapper> T.xpref() = Xpref.getDefaultSharedPreferences(this)

fun <T: ContextWrapper> T.xpref(name: String) = Xpref.getSharedPreferences(this, name)
```
```kotlin
// in Activity
class AnActivity : Activity() {
    private fun getPreferences() = this.xpref("awesome")
}

// in Service
class AService : Service() {
    private fun getPreferences() = this.xpref("awesome")
}
```

The other usage is the same as normal SharedPreferences.

Have fun!
