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

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.annotation.RestrictTo
import android.support.annotation.VisibleForTesting
import android.util.ArrayMap
import android.util.Log
import java.lang.IllegalStateException
import java.util.HashMap

/**
 * @see SharedPreferences.getAll
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal const val M_GET_ALL = "$1"
/**
 * @see SharedPreferences.getString
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal const val M_GET_STRING = "$2"
/**
 * @see SharedPreferences.getStringSet
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal const val M_GET_STRING_SET = "$3"
/**
 * @see SharedPreferences.getInt
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal const val M_GET_INT = "$4"
/**
 * @see SharedPreferences.getLong
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal const val M_GET_LONG = "$5"
/**
 * @see SharedPreferences.getFloat
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal const val M_GET_FLOAT = "$6"
/**
 * @see SharedPreferences.getBoolean
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal const val M_GET_BOOLEAN = "$7"
/**
 * @see SharedPreferences.contains
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal const val M_CONTAINS = "$8"
/**
 * Use in Editor of SharedPreferences
 * @see SharedPreferences.Editor.commit
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal const val M_EDITOR_COMMIT = "$9"

/**
 * Key for name of SharedPreferences
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal const val KEY_NAME = "\$xpref.name"

internal const val KEY_NULL = "\$xpref.NULL"

/**
 * Magic key for Editor commit with [SharedPreferences.Editor.clear]
 * @see SharedPreferences.Editor.commit
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal const val KEY_CLEAR = "\$xpref.clear"

private var baseUri: Uri? = null

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal fun getBaseUri(context: Context): Uri {
    if (baseUri == null) {
        synchronized(XprefProvider::class) {
            val packageName = context.packageName
            baseUri = Uri.parse(StringBuilder(packageName.length + 26)
                .append("content://")
                .append(packageName)
                .append(".provider.xpref").toString())
        }
    }
    return baseUri!!
}

/**
 * Shares same SharedPreferences instance between application's
 * processes via this ContentProvider.
 *
 * Authority: "${applicationId}.provider.xpref" <br></br>
 *
 * Notify uri : "content://${applicationId}.provider.xpref/{pref-name}/{pref-key}"
 *
 * @author yrom
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
class XprefProvider : ContentProvider() {

    private val caches = createMap()

    override fun onCreate() = true

    override fun query(uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri,
        selection: String?,
        selectionArgs: Array<String>?): Int = 0

    override fun update(uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?): Int = 0

    override fun shutdown() {
        caches.clear()
    }

    /**
     * Call methods of underlying SharedPreferences.
     *
     * @param method The supported methods, [M_GET_ALL], [M_GET_INT], etc.
     *         See `SharedPreferences.getXxx`
     * @param arg The key of SharedPreferences method.
     *         See `SharedPreferences.getXxx`
     * @param extras Method arguments Bundle.
     *         should contain name of SharedPreferences by key [KEY_NAME]
     * @return Returns result as Bundle with key [KEY_RET] or null
     */
    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        // extras should not be null here
        val targetPref = extras?.getString(KEY_NAME) ?: return null.also {
            Log.w("XprefProvider", "What work?")
        }
        val pref = this.get(targetPref)
        return when (method) {
            M_GET_ALL -> pref.all.toBundle()
            M_GET_INT -> retBundleOf(pref.getInt(arg))
            M_GET_LONG -> retBundleOf(pref.getLong(arg))
            M_GET_FLOAT -> retBundleOf(pref.getFloat(arg))
            M_GET_STRING -> retBundleOf(pref.getString(arg))
            M_GET_BOOLEAN -> retBundleOf(pref.getBoolean(arg))
            M_GET_STRING_SET -> retBundleOf(pref.getStringSet(arg, null))
            M_CONTAINS -> if (pref.contains(arg)) Bundle.EMPTY else null
            M_EDITOR_COMMIT -> extras.also { it.remove(KEY_NAME) }.applyTo(pref)
            else -> null
        }
    }

    private fun Bundle.applyTo(pref: SharedPreferences): Bundle? {
        val editor = pref.edit()
        if (getBoolean(KEY_CLEAR, false)) {
            editor.clear()
        } else {
            for (k in keySet()) {
                val v = this[k]
                when (v) {
                    null -> editor.remove(k)
                    is Int -> editor.putInt(k, v)
                    is Long -> editor.putLong(k, v)
                    is Float -> editor.putFloat(k, v)
                    is String -> editor.putString(k, v)
                    is Boolean -> editor.putBoolean(k, v)
                    is ArrayList<*> -> editor.putStringSet(k, v.toStringSet())
                }
            }
        }
        editor.apply()
        return null
    }

    private fun createMap(): MutableMap<String, SharedPreferencesWrapper> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            createArrayMap()
        } else {
            HashMap()
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private fun createArrayMap(): MutableMap<String, SharedPreferencesWrapper> = ArrayMap()

    private fun get(name: String): SharedPreferencesWrapper {
        val context = context ?: throw IllegalStateException()
        synchronized(this) {
            return caches.getOrPut(name, {
                SharedPreferencesWrapper(
                    context.getSharedPreferences(name, Context.MODE_PRIVATE),
                    OnSharedPreferenceChangeListener { _, key: String? ->
                        notifyChanged(name, key)
                    })
            })
        }
    }

    /**
     * ${baseuri}/target/key
     */
    private fun notifyChanged(targetPref: String, key: String?) {
        val context = context ?: return
        val uri = getBaseUri(context)
            .buildUpon()
            .appendPath(targetPref)
            .appendPath(key ?: KEY_NULL)
            .build()
        context.contentResolver.notifyChange(uri, null, false)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    internal class SharedPreferencesWrapper(
        private val delegate: SharedPreferences,
        private val listener: OnSharedPreferenceChangeListener
    ) : SharedPreferences, OnSharedPreferenceChangeListener {
        private var cache: Map<String?, *>? = null

        init {
            this.delegate.registerOnSharedPreferenceChangeListener(this)
        }

        @Synchronized override fun getAll(): Map<String?, *> {
            val c = cache ?: delegate.all
            cache = c
            return c
        }

        override fun getString(key: String?, defValue: String?): String? {
            return delegate.getString(key, defValue)
        }

        internal fun getString(key: String?): String? {
            return try {
                delegate.getString(key, null)
            } catch (e: ClassCastException) {
                all[key]?.toString()
            }
        }

        override fun getStringSet(key: String?, defValues: Set<String>?): Set<String>? {
            return try {
                delegate.getStringSet(key, defValues)
            } catch (e: ClassCastException) {
                defValues
            }
        }

        private inline fun <reified T> get(key: String?, stringCaster: (String) -> T?): T? {
            return all[key].let {
                when (it) {
                    is T -> it
                    is String -> stringCaster(it)
                    else -> null
                }
            }
        }

        internal fun getInt(key: String?) = get(key) { it.toIntOrNull() }

        override fun getInt(key: String?, defValue: Int) = delegate.getInt(key, defValue)

        internal fun getLong(key: String?) = get(key) { it.toLongOrNull() }

        override fun getLong(key: String?, defValue: Long) = delegate.getLong(key, defValue)

        internal fun getFloat(key: String?) = get(key) { it.toFloatOrNull() }

        override fun getFloat(key: String?, defValue: Float) = delegate.getFloat(key, defValue)

        internal fun getBoolean(key: String?) = get(key) { it.toBoolean() }

        override fun getBoolean(key: String?, defValue: Boolean): Boolean {
            return delegate.getBoolean(key, defValue)
        }

        override fun contains(key: String?): Boolean = delegate.contains(key)

        @SuppressLint("CommitPrefEdits")
        override fun edit(): SharedPreferences.Editor = Editor(delegate.edit())

        override fun registerOnSharedPreferenceChangeListener(
            listener: OnSharedPreferenceChangeListener?) {
            //no op
        }

        override fun unregisterOnSharedPreferenceChangeListener(
            listener: OnSharedPreferenceChangeListener?) {
            // no op
        }

        override fun onSharedPreferenceChanged(ignored: SharedPreferences, key: String?) {
            reset()
            listener.onSharedPreferenceChanged(this, key)
        }

        private inner class Editor internal constructor(
            private val delegate: SharedPreferences.Editor
        ) : SharedPreferences.Editor {

            override fun putString(key: String?, value: String?): SharedPreferences.Editor {
                delegate.putString(key, value)
                return this
            }

            override fun putStringSet(key: String?,
                values: Set<String>?
            ): SharedPreferences.Editor {
                delegate.putStringSet(key, values)
                return this
            }

            override fun putInt(key: String?, value: Int): SharedPreferences.Editor {
                delegate.putInt(key, value)
                return this
            }

            override fun putLong(key: String?, value: Long): SharedPreferences.Editor {
                delegate.putLong(key, value)
                return this
            }

            override fun putFloat(key: String?, value: Float): SharedPreferences.Editor {
                delegate.putFloat(key, value)
                return this
            }

            override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor {
                delegate.putBoolean(key, value)
                return this
            }

            override fun remove(key: String?): SharedPreferences.Editor {
                delegate.remove(key)
                return this
            }

            override fun clear(): SharedPreferences.Editor {
                delegate.clear()
                return this
            }

            override fun commit(): Boolean {
                try {
                    return delegate.commit()
                } finally {
                    this@SharedPreferencesWrapper.reset()
                }
            }

            override fun apply() {
                delegate.apply()
                this@SharedPreferencesWrapper.reset()
            }
        }

        @Synchronized private fun reset() {
            cache = null
        }
    }
}
