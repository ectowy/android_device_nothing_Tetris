/* SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */
package org.lineageos.settings.tetris.display

import android.os.PowerManager
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.lineageos.settings.tetris.R
import org.lineageos.settings.tetris.ui.PartsInfoCard
import org.lineageos.settings.tetris.ui.PartsPreference
import org.lineageos.settings.tetris.ui.PartsPreferenceGroup
import org.lineageos.settings.tetris.ui.PartsScaffold

@Composable
fun DisplayScreen(onBack: () -> Unit) {
    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()
    var legacyCurve by remember {
        mutableStateOf(runCatching { DisplayRepository.isLegacyBrightnessCurveEnabled() }.getOrDefault(false))
    }
    var busy by remember { mutableStateOf(false) }
    var applyFailed by remember { mutableStateOf(false) }
    var rebootFailed by remember { mutableStateOf(false) }
    var showRebootDialog by rememberSaveable { mutableStateOf(false) }

    PartsScaffold(title = stringResource(R.string.display_title), onBack = onBack) { insets ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = insets.calculateTopPadding() + 8.dp,
                bottom = insets.calculateBottomPadding() + 24.dp,
            ),
        ) {
            item("display_brightness") {
                PartsPreferenceGroup(title = stringResource(R.string.display_brightness_section)) {
                    PartsPreference(
                        title = stringResource(R.string.display_legacy_slider_title),
                        summary = stringResource(R.string.display_legacy_slider_summary),
                        icon = Icons.Filled.Brightness6,
                        trailing = {
                            Switch(
                                checked = legacyCurve,
                                enabled = !busy,
                                onCheckedChange = { enabled ->
                                    if (enabled == legacyCurve) return@Switch
                                    busy = true
                                    scope.launch {
                                        val result = withContext(Dispatchers.IO) {
                                            DisplayRepository.setLegacyBrightnessCurveEnabled(enabled)
                                        }
                                        applyFailed = result.isFailure
                                        result.getOrNull()?.let {
                                            legacyCurve = it
                                            rebootFailed = false
                                            showRebootDialog = true
                                        }
                                        busy = false
                                    }
                                },
                            )
                        },
                    )
                }
            }
            if (applyFailed || rebootFailed) {
                item("display_error") {
                    PartsInfoCard(
                        title = stringResource(R.string.display_title),
                        body = stringResource(
                            if (rebootFailed) R.string.display_reboot_failed
                            else R.string.display_control_apply_failed,
                        ),
                    )
                }
            }
            item("display_about") {
                PartsInfoCard(
                    title = stringResource(R.string.display_about_title),
                    body = stringResource(R.string.display_controls_summary),
                )
            }
        }
    }

    if (showRebootDialog) {
        AlertDialog(
            onDismissRequest = { showRebootDialog = false },
            title = { Text(stringResource(R.string.display_reboot_title)) },
            text = { Text(stringResource(R.string.display_reboot_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showRebootDialog = false
                    busy = true
                    scope.launch {
                        val result = withContext(Dispatchers.IO) {
                            runCatching {
                                checkNotNull(context.getSystemService(PowerManager::class.java)).reboot(null)
                            }
                        }
                        rebootFailed = result.isFailure
                        busy = false
                    }
                }) { Text(stringResource(R.string.display_reboot_now)) }
            },
            dismissButton = {
                TextButton(onClick = { showRebootDialog = false }) {
                    Text(stringResource(R.string.display_reboot_later))
                }
            },
        )
    }
}
