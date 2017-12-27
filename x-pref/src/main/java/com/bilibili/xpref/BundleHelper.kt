/*
 * Copyright (c) 2017. bilibili, inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

@file:JvmName("Internal")
@file:JvmMultifileClass

package com.bilibili.xpref

import android.os.Bundle
import android.support.annotation.RestrictTo
import java.util.ArrayList
import java.util.HashMap

/**
 * Convert [Bundle] to [Map]
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal fun Bundle?.toMap(): Map<String?, Any?> {
    if (this == null) return emptyMap<String?, Any>()
    val map = HashMap<String?, Any?>(this.size())
    for (key in this.keySet()) {
        val v = this.get(key)
        when (v) {
            is ArrayList<*> -> map.put(key, v.toStringSet())
            else -> map.put(key, v)
        }
    }
    return map
}

/**
 * StringArrayList to StringSet for Bundle [toMap]
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal fun ArrayList<*>.toStringSet(): HashSet<String?> {
    return this.mapTo(HashSet(), { it as? String })
} //

/**
 *  StringSet to StringArrayList for Map [toBundle]
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal fun Set<*>.toStringArrayList(): ArrayList<String?> {
    return this.mapTo(ArrayList(), { it as? String })
}

/**
 * Convert [Map] to [Bundle]
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal fun Map<String?, *>?.toBundle(): Bundle {
    return this?.let { Bundle(it.size) }?.also { it.putAll(this) } ?: Bundle.EMPTY
}

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal fun Bundle.putAll(map: Map<String?, Any?>) {
    for ((k, v) in map) {
        when (v) {
            is Int -> this.putInt(k, v)
            is Long -> this.putLong(k, v)
            is Float -> this.putFloat(k, v)
            is String -> this.putString(k, v)
            is Boolean -> this.putBoolean(k, v)
            is Set<*> -> { //StringSet to StringArrayList
                this.putStringArrayList(k, v.toStringArrayList())
            }
            null -> this.putString(k, null)
        }
    }
}

/**
 * Result key
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal const val KEY_RET = "\$xpref.ret"

/**
 * Construct Bundle instance with key [KEY_RET] int value
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal fun retBundleOf(v: Int?) = bundle(v) { putInt(KEY_RET, it) }

/**
 * Construct Bundle instance with key [KEY_RET] Long value
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal fun retBundleOf(v: Long?) = bundle(v) { putLong(KEY_RET, it) }

/**
 * Construct Bundle instance with key [KEY_RET] Float value
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal fun retBundleOf(v: Float?) = bundle(v) { putFloat(KEY_RET, it) }

/**
 * Construct Bundle instance with key [KEY_RET] String value
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal fun retBundleOf(v: String?) = bundle(v) { putString(KEY_RET, it) }

/**
 * Construct Bundle instance with key [KEY_RET] Boolean value
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal fun retBundleOf(v: Boolean?) = bundle(v) { putBoolean(KEY_RET, it) }

/**
 * Construct Bundle instance with key [KEY_RET] StringSet value
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal fun retBundleOf(v: Set<*>?): Bundle? {
    return bundle(v) {
        putStringArrayList(KEY_RET, it.toStringArrayList()) // StringSet to StringArrayList
    }
}

private inline fun <reified T> bundle(v: T?, putter: Bundle.(T) -> Unit): Bundle? {
    return v?.let { Bundle(1) }?.apply { putter(v) }
}

/**
 * Cast result to StringSet which get from Bundle with key [KEY_RET]
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal fun Bundle?.retToStringSet(defValues: Set<String>?): Set<String>? {
    // StringArrayList to StringSet
    return this?.getStringArrayList(KEY_RET)?.toHashSet() ?: defValues
}

/**
 * Cast result which get from Bundle with key [KEY_RET] to [defValue]'s type [T]
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal inline fun <reified T> Bundle?.ret(defValue: T): T {
    return this?.get(KEY_RET) as? T ?: defValue
}
