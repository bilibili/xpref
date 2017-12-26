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

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View

import com.bilibili.xpref.Xpref

class MainActivity : Activity(), View.OnClickListener {
    private var listener: SharedPreferences.OnSharedPreferenceChangeListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Main-" + Math.random()
        setContentView(R.layout.activity_main)
        startService(Intent(this, XXService::class.java))
        startService(Intent(this, XXXService::class.java))
        arrayOf(R.id.read, R.id.write, R.id.clear,
            R.id.read2, R.id.write2, R.id.clear2,
            R.id.read3, R.id.write3, R.id.clear3,
            R.id.new_activity
        ).forEach { it ->
            findViewById(it).setOnClickListener(this)
        }

        listener = SharedPreferences.OnSharedPreferenceChangeListener { pref, key: String? ->
            Log.d("MainActivity",
                "received changes '$key'= '${pref.all[key]}' in activity '$title' ")
        }
        this.xpref().registerOnSharedPreferenceChangeListener(listener)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.read ->
                Log.i("Xpref", Xpref.getDefaultSharedPreferences(this).all.toString())
            R.id.write ->
                this.xpref()
                    .edit()
                    .putInt("int", (Math.random() * 100).toInt())
                    .putString("string", (Math.random() * 100).toString())
                    .putLong("long", (Math.random() * 100).toLong())
                    .putBoolean("bool", Math.random() > 0.5)
                    .putStringSet("string-set", setOf("a", "b", "c", "d"))
                    .putFloat(null, 0.5F) // null key
                    .apply()
            R.id.clear -> this.xpref().edit().clear().apply()
            R.id.read2, R.id.write2, R.id.clear2 ->
                startService(Intent(this, XXService::class.java)
                    .putExtra("cmd", v.id))
            R.id.read3, R.id.write3, R.id.clear3 ->
                startService(Intent(this, XXXService::class.java)
                    .putExtra("cmd", v.id))
            R.id.new_activity -> {
                // Test for holding instance of OnSharedPreferenceChangeListener
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        this.xpref().unregisterOnSharedPreferenceChangeListener(listener)
        listener = null
    }
}
