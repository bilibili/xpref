/*
 * Copyright (c) 2017. bilibili, inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bilibili.xpref.demo

import android.app.Fragment
import android.content.ContextWrapper
import android.content.SharedPreferences
import com.bilibili.xpref.Xpref

/**
 * Convenient func for [ContextWrapper]'s subclasses to call [Xpref.getDefaultSharedPreferences].
 */
fun <T: ContextWrapper> T.xpref() = Xpref.getDefaultSharedPreferences(this)

/**
 * Convenient func for [ContextWrapper]'s subclasses to call [Xpref.getSharedPreferences].
 */
fun <T: ContextWrapper> T.xpref(name: String) = Xpref.getSharedPreferences(this, name)
/**
 * Convenient func for [Fragment] to call [Xpref.getDefaultSharedPreferences].
 *
 * Note that the return value is nullable as [Fragment.getActivity] nullable
 */
fun Fragment.xpref(): SharedPreferences? = this.activity?.xpref()
