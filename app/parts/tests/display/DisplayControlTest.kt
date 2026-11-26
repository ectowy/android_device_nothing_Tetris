/* SPDX-License-Identifier: Apache-2.0 */
package org.lineageos.settings.tetris.display

import android.os.SystemProperties
import org.lineageos.settings.tetris.consent.PartsControlGate

fun main() {
    PartsControlGate.allowed = false
    check(DisplayRepository.setLegacyBrightnessCurveEnabled(true).isFailure)
    check(SystemProperties.values.isEmpty())
    PartsControlGate.allowed = true
    SystemProperties.rejectWrite = true
    check(DisplayRepository.setLegacyBrightnessCurveEnabled(true).isFailure)
    SystemProperties.rejectWrite = false
    check(DisplayRepository.setLegacyBrightnessCurveEnabled(true).getOrThrow())
    check(SystemProperties.values["persist.tetris.display.legacy_brightness_curve"] == "true")
    check(DisplayRepository.isLegacyBrightnessCurveEnabled())
    SystemProperties.rejectWrite = true
    check(!DisplayRepository.disableForConsent())
    SystemProperties.rejectWrite = false
    PartsControlGate.allowed = false
    check(DisplayRepository.disableForConsent())
    check(!DisplayRepository.isLegacyBrightnessCurveEnabled())
    check(DisplayRepository.disableForConsent())
    println("PASS: brightness preference, permission errors, consent gate and restoration")
}
