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

package com.bilibili.xpref

import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.database.ContentObserver
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.annotation.RestrictTo
import android.util.Log
import com.bilibili.xpref.BuildConfig.DEBUG
import java.lang.ref.WeakReference
import java.util.WeakHashMap

/**
 * Delegate all SharedPreferences methods to [XprefProvider]
 * @author yrom
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class Silhouette(context: Context, private val prefName: String) : SharedPreferences {

    private val resolver = context.contentResolver
    private val baseUri = getBaseUri(context)
    private val callExtras = Bundle(1)
    private val listeners = WeakHashMap<SharedPreferences.OnSharedPreferenceChangeListener, Any?>()
    private var contentObserver: ContentObserver? = null

    init {
        callExtras.putString(KEY_NAME, prefName)
    }

    override fun getAll() = call(M_GET_ALL, null, null).toMap()

    override fun getInt(key: String?, defValue: Int): Int {
        return call(M_GET_INT, key, null).ret(defValue)
    }

    override fun getLong(key: String?, defValue: Long): Long {
        return call(M_GET_LONG, key, null).ret(defValue)
    }

    override fun getFloat(key: String?, defValue: Float): Float {
        return call(M_GET_FLOAT, key, null).ret(defValue)
    }

    override fun getString(key: String?, defValue: String?): String? {
        return call(M_GET_STRING, key, null).ret(defValue)
    }

    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return call(M_GET_BOOLEAN, key, null).ret(defValue)
    }

    override fun getStringSet(key: String?, defValues: Set<String>?): Set<String>? {
        return call(M_GET_STRING_SET, key, null).retToStringSet(defValues)
    }

    override fun contains(key: String?): Boolean {
        return call(M_CONTAINS, key, null) != null
    }

    override fun edit(): SharedPreferences.Editor = Editor()

    override fun registerOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        if (DEBUG) Log.i(TAG, "register a listener " + listener)
        listeners.put(listener, null)
        registerObserverIfNeed()
    }

    override fun unregisterOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        if (DEBUG) Log.i(TAG, "unregister listener " + listener)
        listeners.remove(listener)
        unregisterObserverIfIndeed()
    }

    private class Observer(private var contentResolver: ContentResolver?,
        ref: Silhouette) : ContentObserver(Handler(Looper.getMainLooper())) {

        private val reference: WeakReference<Silhouette> = WeakReference(ref)

        override fun onChange(selfChange: Boolean, uri: Uri) {
            if (DEBUG) Log.i(TAG, "onChanged " + uri)
            val ref = reference.get()
            if (ref == null || ref.listeners.isEmpty()) {
                if (DEBUG) Log.w(TAG, "unregister leaked ContentObserver!")
                contentResolver?.unregisterContentObserver(this)
                contentResolver = null
                return
            }

            uri.lastPathSegment
                .let { if (KEY_NULL == it) null else it }
                .run {
                    for (l in ref.listeners.keys) {
                        l.onSharedPreferenceChanged(ref, this)
                    }
                }
        }
    }

    @Synchronized private fun registerObserverIfNeed() {
        if (contentObserver == null) {
            val target = baseUri.buildUpon().appendPath(prefName).build()
            val observer = Observer(resolver, this)
            resolver.registerContentObserver(target, true, observer)
            contentObserver = observer
        }
    }

    @Synchronized private fun unregisterObserverIfIndeed() {
        if (listeners.isEmpty()) {
            contentObserver?.let {
                resolver.unregisterContentObserver(it)
            }
            contentObserver = null
        }
    }

    internal fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        return try {
            // ContentProvider cannot handle uri in call(), so put name in extras
            val ext = extras?.apply { putString(KEY_NAME, prefName) } ?: callExtras
            if (DEBUG) {
                Log.d(TAG, "call $method($arg, $ext)")
            }
            resolver.call(baseUri, method, arg, ext)
        } catch (e: Exception) {
            Log.w(TAG, e)
            null
        }
    }

    private inner class Editor : SharedPreferences.Editor {
        internal var changes = Bundle()

        override fun putString(key: String?, value: String?): SharedPreferences.Editor {
            changes.putString(key, value)
            return this
        }

        override fun putStringSet(key: String?, values: Set<String>?): SharedPreferences.Editor {
            changes.putStringArrayList(key, values?.toStringArrayList())
            return this
        }

        override fun putInt(key: String?, value: Int): SharedPreferences.Editor {
            changes.putInt(key, value)
            return this
        }

        override fun putLong(key: String?, value: Long): SharedPreferences.Editor {
            changes.putLong(key, value)
            return this
        }

        override fun putFloat(key: String?, value: Float): SharedPreferences.Editor {
            changes.putFloat(key, value)
            return this
        }

        override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor {
            changes.putBoolean(key, value)
            return this
        }

        override fun remove(key: String?): SharedPreferences.Editor {
            changes.remove(key)
            changes.putString(key, null)
            return this
        }

        override fun clear(): SharedPreferences.Editor {
            changes.clear()
            changes.putBoolean(KEY_CLEAR, true)
            return this
        }

        override fun commit(): Boolean {
            call(M_EDITOR_COMMIT, null, changes)
            return true
        }

        override fun apply() {
            commit()
        }
    }

    companion object {
        private const val TAG = "Silhouette"
    }
}
