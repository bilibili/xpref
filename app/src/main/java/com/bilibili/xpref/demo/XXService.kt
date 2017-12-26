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

package com.bilibili.xpref.demo

import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import android.util.Log

/**
 * @author yrom
 */
class XXService : Service(), SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var preferences: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        preferences = this.xpref()
        preferences.registerOnSharedPreferenceChangeListener(this)
        preferences.edit().putString("random", Math.random().toString()).apply()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val cmd = intent.getIntExtra("cmd", 0)
        if (cmd == 0) return Service.START_NOT_STICKY
        when (cmd) {
            R.id.read2 -> Log.i("xx", preferences.all.toString())
            R.id.write2 -> preferences.edit()
                    .putInt("int", (Math.random() * 100).toInt())
                    .putStringSet("string-set",
                            setOf("aa", "bb", "cc", "dd", Math.random().toString()))
                    .apply()
            R.id.clear2 -> preferences.edit().clear().apply()
        }

        return Service.START_NOT_STICKY
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        Log.i("XX", "On changed key=$key, value=${sharedPreferences.all[key]}")
    }

    override fun onDestroy() {
        super.onDestroy()
        preferences.unregisterOnSharedPreferenceChangeListener(this)
    }
}
