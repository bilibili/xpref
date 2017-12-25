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

```
Xpref.getDefaultSharedPreferences(context)
```

2. Gets a SharedPreferences with specific named.

```
val name = "awesome"
Xpref.getSharedPreferences(context, name)
```

The other usage is the same as normal SharedPreferences.

Have fun!
