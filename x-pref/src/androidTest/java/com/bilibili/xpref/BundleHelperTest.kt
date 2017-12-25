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

import android.os.Bundle
import android.support.test.runner.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import java.util.HashMap
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * @author yrom
 */
@RunWith(AndroidJUnit4::class)
class BundleHelperTest {
    @Test
    fun toMap() {
        assertTrue(null.toMap().isEmpty())
        val b = Bundle()
        b.putStringArrayList("set", arrayListOf("a", "b"))
        b.putString("a", "b")
        b.putBoolean("b", true)
        b.putString(null, null)
        val map = b.toMap()
        assertNotNull(map)
        assertTrue(map["b"] is Boolean)
        assertTrue(map["b"] as Boolean)
        assertTrue(map["a"] is String)
        assertTrue(map["a"] == "b")
        assertNull(map[null])
        assertTrue(map["set"] is Set<*>) // ArrayList to Set
    }

    @Test
    fun toBundle() {
        assertTrue(null.toBundle().isEmpty)
        val map = HashMap<String?, Any>()
        map.put("set", setOf("a", "b"))
        map.put("a", "String")
        map.put("b", true)
        map.put("i", 1)
        map.put("f", 1f)
        map.put("l", Long.MAX_VALUE)
        map.put(null, "null")

        val b2 = map.toBundle()
        assertNotNull(b2)
        assertTrue(1 == b2.getInt("i"))
        assertTrue(1f == b2.getFloat("f"))
        assertTrue(Long.MAX_VALUE == b2.getLong("l"))
        assertEquals("String", b2.getString("a"))
        assertEquals("null", b2.getString(null))
        assertTrue(b2.getBoolean("b"))
        assertNotNull(b2.getStringArrayList("set")) // Set to ArrayList
    }
}