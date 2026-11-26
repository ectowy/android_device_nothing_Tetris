/* SPDX-License-Identifier: Apache-2.0 */
package android.os

object SystemProperties {
    val values = mutableMapOf<String, String>()
    var rejectWrite = false
    fun getBoolean(key: String, defaultValue: Boolean) = values[key]?.toBoolean() ?: defaultValue
    fun set(key: String, value: String) {
        check(!rejectWrite) { "Property write denied" }
        values[key] = value
    }
}
