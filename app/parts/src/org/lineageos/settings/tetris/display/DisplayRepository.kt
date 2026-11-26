/* SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */
package org.lineageos.settings.tetris.display

import android.os.SystemProperties
import org.lineageos.settings.tetris.consent.PartsControlGate

internal object DisplayRepository {
    private const val LEGACY_BRIGHTNESS_KEY = "persist.tetris.display.legacy_brightness_curve"

    fun isLegacyBrightnessCurveEnabled(): Boolean =
        SystemProperties.getBoolean(LEGACY_BRIGHTNESS_KEY, false)

    /** SettingsLib caches the conversion flag; init restores it before SystemUI starts. */
    fun setLegacyBrightnessCurveEnabled(enabled: Boolean): Result<Boolean> = runCatching {
        checkNotNull(PartsControlGate.runIfAllowed {
            SystemProperties.set(LEGACY_BRIGHTNESS_KEY, enabled.toString())
            check(isLegacyBrightnessCurveEnabled() == enabled)
            enabled
        }) { "Display controls are disabled" }
    }

    /** Called on the cleanup worker after pending UI mutations have finished. */
    fun disableForConsent(): Boolean = runCatching {
        if (isLegacyBrightnessCurveEnabled()) {
            SystemProperties.set(LEGACY_BRIGHTNESS_KEY, "false")
        }
        !isLegacyBrightnessCurveEnabled()
    }.getOrDefault(false)
}
