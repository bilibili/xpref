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

import android.content.Context
import android.content.SharedPreferences
import java.lang.ref.WeakReference

/**
 * @author yrom
 */
object Xpref {
    private val caches = mutableMapOf<String, WeakReference<Silhouette>>()

    @Synchronized private fun get(context: Context, name: String): SharedPreferences {
        var pref = caches[name]?.get()
        if (pref == null) {
            pref = Silhouette(context, name)
            caches.put(name, WeakReference(pref))
        }
        return pref
    }

    /**
     * @see Context.getSharedPreferences
     */
    @JvmStatic
    fun getSharedPreferences(context: Context, name: String): SharedPreferences {
        return get(context.applicationContext, name)
    }

    /**
     * @see android.preference.PreferenceManager.getDefaultSharedPreferences
     * @see android.preference.PreferenceManager.getDefaultSharedPreferencesName
     */
    @JvmStatic
    fun getDefaultSharedPreferences(context: Context): SharedPreferences {
        return getSharedPreferences(context, getDefaultName(context))
    }

    private fun getDefaultName(context: Context) = context.packageName + "_preferences"
}
