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

import android.content.SharedPreferences
import android.support.test.runner.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import java.util.ArrayList
import kotlin.test.assertTrue

/**
 * @author yrom
 */
@RunWith(AndroidJUnit4::class)
class SharedPreferencesWrapperTest {
    @Test
    fun testWrapper() {
        val mock = MockSharedPreferences()
        val wrapper = XprefProvider.SharedPreferencesWrapper(mock,
            SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> })
        wrapper.all
        wrapper.getString("", "")
        wrapper.getInt("", 0)
        wrapper.getLong("", 0)
        wrapper.getFloat("", 0f)
        wrapper.getStringSet("", null)
        wrapper.contains("")
        wrapper.edit().putString("", "").apply()
        wrapper.edit().putString("1", "").commit()
        assertTrue(
            mock.testedMethods.containsAll(arrayListOf("getAll", "getString",
                "getInt", "getLong", "getFloat", "getStringSet",
                "contains", "edit", "apply", "commit",
                "registerOnSharedPreferenceChangeListener"))
        )
    }

    internal class MockSharedPreferences : SharedPreferences {
        var testedMethods: MutableList<String> = ArrayList()

        override fun getAll(): Map<String, *> {
            testedMethods.add("getAll")
            return emptyMap<String, Any>()
        }

        override fun getString(key: String, defValue: String?): String? {
            testedMethods.add("getString")
            return defValue
        }

        override fun getStringSet(key: String, defValues: Set<String>?): Set<String>? {
            testedMethods.add("getStringSet")
            return defValues
        }

        override fun getInt(key: String, defValue: Int): Int {
            testedMethods.add("getInt")
            return defValue
        }

        override fun getLong(key: String, defValue: Long): Long {
            testedMethods.add("getLong")
            return defValue
        }

        override fun getFloat(key: String, defValue: Float): Float {
            testedMethods.add("getFloat")
            return defValue
        }

        override fun getBoolean(key: String, defValue: Boolean): Boolean {
            testedMethods.add("getBoolean")
            return defValue
        }

        override fun contains(key: String): Boolean {
            testedMethods.add("contains")
            return false
        }

        override fun edit(): SharedPreferences.Editor {
            testedMethods.add("edit")
            return object : SharedPreferences.Editor {
                override fun putString(key: String, value: String?): SharedPreferences.Editor {
                    return this
                }

                override fun putStringSet(key: String, values: Set<String>?): SharedPreferences.Editor {
                    return this
                }

                override fun putInt(key: String, value: Int): SharedPreferences.Editor {
                    return this
                }

                override fun putLong(key: String, value: Long): SharedPreferences.Editor {
                    return this
                }

                override fun putFloat(key: String, value: Float): SharedPreferences.Editor {
                    return this
                }

                override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor {
                    return this
                }

                override fun remove(key: String): SharedPreferences.Editor {
                    return this
                }

                override fun clear(): SharedPreferences.Editor {
                    return this
                }

                override fun commit(): Boolean {
                    testedMethods.add("commit")
                    return false
                }

                override fun apply() {
                    testedMethods.add("apply")
                }
            }
        }

        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
            testedMethods.add("registerOnSharedPreferenceChangeListener")
        }

        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
            testedMethods.add("unregisterOnSharedPreferenceChangeListener")
        }
    }
}