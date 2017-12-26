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

import com.bilibili.xpref.Xpref

/**
 * @author yrom
 */
class XXXService : Service(), SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var preferences: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        preferences = Xpref.getDefaultSharedPreferences(applicationContext)
        preferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val cmd = intent.getIntExtra("cmd", 0)
        if (cmd == 0) return Service.START_NOT_STICKY
        when (cmd) {
            R.id.read3 -> Log.i("xxx", preferences.all.toString())
            R.id.write3 -> preferences.edit()
                    .putInt("int", (Math.random() * 100).toInt())
                    .putBoolean("bool", startId and 1 == 1)
                    .apply()
            R.id.clear3 -> preferences.edit().clear().apply()
        }
        return Service.START_NOT_STICKY
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        Log.i("XXX", "On changed key=$key, value=${sharedPreferences.all[key]}")
    }

    override fun onDestroy() {
        super.onDestroy()
        preferences.unregisterOnSharedPreferenceChangeListener(this)
    }
}
