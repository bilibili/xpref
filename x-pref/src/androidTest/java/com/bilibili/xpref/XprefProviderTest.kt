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
import android.net.Uri
import android.os.Bundle
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNull


/**
 * @author yrom
 */

@RunWith(AndroidJUnit4::class)
class XprefProviderTest {
    private lateinit var cr: ContentResolver
    private lateinit var baseuri: Uri

    @Before
    fun setup() {
        cr = InstrumentationRegistry.getContext().contentResolver
        baseuri = getBaseUri(InstrumentationRegistry.getContext())

    }

    // 'right' call has been tested in XprefTest, so here we only test the 'wrong' call
    @Test
    fun testWrongCall() {
        assertNull(cr.call(baseuri, "nullextras", null, null))
        assertNull(cr.call(baseuri, "notarget", null, Bundle.EMPTY))
        val extra = Bundle(1)
        extra.putString(KEY_NAME, "test")
        assertNull(cr.call(baseuri, "unsupportedmethod", null, extra))
    }

    @Test
    fun testStubProviderMethods() {
        assertNull(cr.query(baseuri, null, null, null, null))
        assertEquals(0, cr.update(baseuri, null, null, null))
        assertEquals(0, cr.delete(baseuri, null, null))
        assertNull(cr.insert(baseuri, null))
        assertNull(cr.getType(baseuri))
    }
}
