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
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Arrays
import java.util.HashSet
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class XprefTest {
    /**
     * clear all data after every testcase
     */
    @After
    fun afterTest() {
        val preferences = Xpref.getDefaultSharedPreferences(InstrumentationRegistry.getContext())
        preferences.edit().clear().apply()
    }

    @Test
    fun testGetDefault() {
        val appContext = InstrumentationRegistry.getContext()
        val preferences = Xpref.getDefaultSharedPreferences(appContext)
        assertNotNull(preferences)
        assertTrue(preferences is Silhouette)
        assertEquals(preferences, Xpref.getDefaultSharedPreferences(appContext))
    }

    @Test
    fun testGetInstance() {
        val appContext = InstrumentationRegistry.getContext()
        val name = "xxxxxx"
        val preferences = Xpref.getSharedPreferences(appContext, name)
        assertNotNull(preferences)
        assertTrue(preferences is Silhouette)
        assertEquals(preferences, Xpref.getSharedPreferences(appContext, name))
        assertEquals(preferences, Xpref.getSharedPreferences(appContext, name))
    }

    @Test
    fun testGetString() {
        val v = "astring"
        val key = "akey"
        val preferences = Xpref.getDefaultSharedPreferences(InstrumentationRegistry.getContext())
        assertNull(preferences.getString("notexsits", null))
        assertEquals(v, preferences.getString("notexsits", v))

        preferences.edit()
                .putString(key, v)
                .apply()
        assertEquals(v, preferences.getString(key, null))
    }

    @Test
    fun testGetStringSet() {
        val v = HashSet(Arrays.asList("adfadf", "bcvcv", "bmuer"))
        val key = "stringset"
        val preferences = Xpref.getDefaultSharedPreferences(InstrumentationRegistry.getContext())
        assertNull(preferences.getStringSet("notexsits", null))
        assertEquals(v, preferences.getStringSet("notexsits", v))

        preferences.edit()
                .putStringSet(key, v)
                .apply()
        assertEquals(v, preferences.getStringSet(key, null))
    }


    @Test
    fun testGetFloat() {
        val v = 0.123456789f
        val key = "afloat"
        val preferences = Xpref.getDefaultSharedPreferences(InstrumentationRegistry.getContext())
        assertEquals(Float.NaN, preferences.getFloat("notexsits", Float.NaN))

        preferences.edit()
                .putFloat(key, v)
                .apply()
        assertEquals(v, preferences.getFloat(key, Float.NaN))
    }

    @Test
    fun testGetLong() {
        val v = java.lang.Double.doubleToLongBits(Math.random())
        val key = "along"
        val preferences = Xpref.getDefaultSharedPreferences(InstrumentationRegistry.getContext())
        assertTrue(v == preferences.getLong("notexsits", v))
        assertTrue(Long.MAX_VALUE == preferences.getLong("notexsits", Long.MAX_VALUE))
        assertTrue(Long.MIN_VALUE == preferences.getLong("notexsits", Long.MIN_VALUE))

        preferences.edit()
                .putLong(key, v)
                .apply()
        assertEquals(v, preferences.getLong(key, v.inv()))
    }

    @Test
    fun testGetInt() {
        val v = java.lang.Double.doubleToLongBits(Math.random()).toInt()
        val key = "aint"
        val preferences = Xpref.getDefaultSharedPreferences(InstrumentationRegistry.getContext())
        assertTrue(v == preferences.getInt("notexsits", v))
        assertTrue(Int.MAX_VALUE == preferences.getInt("notexsits", Int.MAX_VALUE))
        assertTrue(Int.MIN_VALUE == preferences.getInt("notexsits", Int.MIN_VALUE))

        preferences.edit()
                .putInt(key, v)
                .apply()
        assertEquals(v.toLong(), preferences.getInt(key, v.inv()).toLong())
    }

    @Test
    fun testGetBoolean() {
        val v = java.lang.Double.doubleToLongBits(Math.random()).toInt() and 1 == 1
        val key = "aboolean"
        val preferences = Xpref.getDefaultSharedPreferences(InstrumentationRegistry.getContext())
        assertEquals(v, preferences.getBoolean("notexsits", v))
        assertFalse(preferences.getBoolean("notexsits", false))

        preferences.edit()
                .putBoolean(key, v)
                .apply()
        assertEquals(v, preferences.getBoolean(key, !v))
    }

    @Test
    fun testEditAndGetAll() {
        val preferences = Xpref.getDefaultSharedPreferences(InstrumentationRegistry.getContext())
        assertTrue(preferences.all.isEmpty())
        preferences.edit()
                .putInt("a", 1)
                .putBoolean("b", true)
                .putStringSet("s", HashSet(Arrays.asList("a", "b", "c")))
                .putFloat("d", 0.1f)
                .putLong("l", Long.MAX_VALUE)
                .apply()
        assertEquals(5, preferences.all.size.toLong())
    }

    @Test
    fun testContainsAndRemoveKey() {
        val preferences = Xpref.getDefaultSharedPreferences(InstrumentationRegistry.getContext())
        assertTrue(preferences.all.isEmpty())
        preferences.edit().putLong("l", Long.MIN_VALUE).apply()
        assertTrue(preferences.contains("l"))
        assertEquals(Long.MIN_VALUE, preferences.getLong("l", 0))
        preferences.edit().remove("l").apply()
        assertFalse(preferences.contains("l"))
        assertEquals(-1, preferences.getLong("l", -1))
    }

    @Test
    fun testClear() {
        val preferences = Xpref.getDefaultSharedPreferences(InstrumentationRegistry.getContext())
        assertTrue(preferences.all.isEmpty())
        preferences.edit().putLong("l", java.lang.Long.MIN_VALUE).putFloat("f", 0.1f).apply()
        assertFalse(preferences.all.isEmpty())
        preferences.edit().clear().commit()
        assertTrue(preferences.all.isEmpty())
    }

    @Test
    fun testListener() {
        val preferences = Xpref.getDefaultSharedPreferences(InstrumentationRegistry.getContext())
        val listener = TestOnSharedPreferenceChangeListener("key")
        preferences.registerOnSharedPreferenceChangeListener(listener)
        preferences.edit().putString("key", "kkkk").apply()
        try {
            Thread.sleep(500)
        } catch (e: InterruptedException) {
            // wait listener be notified
        }

        assertTrue(1 == listener.testedState)
        listener.testedState = 2
        preferences.unregisterOnSharedPreferenceChangeListener(listener)
        preferences.edit().putString("key", "a").apply()
        try {
            Thread.sleep(500)
        } catch (e: InterruptedException) {
            // wait listener be notified
        }

        assertEquals(2, listener.testedState.toLong())
    }
    @Test
    fun testNullKeyListener() {
        val preferences = Xpref.getDefaultSharedPreferences(InstrumentationRegistry.getContext())
        val listener = TestOnSharedPreferenceChangeListener(null)
        preferences.registerOnSharedPreferenceChangeListener(listener)
        preferences.edit().putString(null, "null key").apply()
        try {
            Thread.sleep(500)
        } catch (e: InterruptedException) {
            // wait listener be notified
        }

        assertTrue(1 == listener.testedState)
        listener.testedState = 2
        preferences.unregisterOnSharedPreferenceChangeListener(listener)
        preferences.edit().putString(null, "null key").apply()
        try {
            Thread.sleep(500)
        } catch (e: InterruptedException) {
            // wait listener be notified
        }

        assertEquals(2, listener.testedState.toLong())
    }
    internal inner class TestOnSharedPreferenceChangeListener(
            private val testTargetKey: String?
    ) : SharedPreferences.OnSharedPreferenceChangeListener {
        var testedState = 0

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
            assertTrue(sharedPreferences is Silhouette)
            assertEquals(testTargetKey, key)
            testedState = 1
        }
    }

    @Test
    fun testWrongClassCast() {
        val preferences = Xpref.getDefaultSharedPreferences(InstrumentationRegistry.getContext())
        preferences.edit()
                .putString("aint", "11111")   // put string
                .putLong("along", Long.MAX_VALUE)
                .putBoolean("aboolean", true)
                .putString("astring", "false")
                .putStringSet(null, setOf("a","b"))
                .apply()
        assertEquals("11111", preferences.getString("aint", null))

        // cast long to string
        assertEquals(Long.MAX_VALUE.toString(), preferences.getString("along", null))
        // cast string to int
        assertTrue(11111 == preferences.getInt("aint", 0))
        assertTrue(0 == preferences.getInt("astring", 0))
        // cast string to long
        assertTrue(11111L == preferences.getLong("aint", 0))
        assertTrue(0L == preferences.getLong("astring", 0))

        // cast boolean to string
        assertEquals("true", preferences.getString("aboolean", null))
        // cast string to boolean
        assertEquals("false", preferences.getString("astring", null))
        assertFalse(preferences.getBoolean("astring", true))
        // can only cast string to boolean
        assertTrue(preferences.getBoolean("along", true))
        assertTrue(preferences.getBoolean(null, true))

        assertNotNull(preferences.getStringSet(null, null))
        // cast stringset to string
        assertNotNull(preferences.getString(null, null))

    }
}
