package com.segnities007.setting.presentation.component.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.segnities007.designsystem.atom.chip.NofyChoiceChip
import com.segnities007.designsystem.atom.slider.NofySlider
import com.segnities007.designsystem.atom.surface.NofyCardSurface
import com.segnities007.designsystem.molecule.layout.NofyCardSectionHeader
import com.segnities007.designsystem.atom.text.NofyTitleLargeText
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.designsystem.theme.NofySpacing
import com.segnities007.setting.R
import com.segnities007.setting.presentation.component.layout.SettingsSectionList
import com.segnities007.setting.presentation.contract.SettingIntent
import com.segnities007.setting.presentation.contract.SettingsSection
import com.segnities007.setting.presentation.preview.previewSettingState
import com.segnities007.settings.FontScalePresets
import com.segnities007.settings.ThemeMode
import com.segnities007.settings.fontScalePresetIndex
import kotlin.math.roundToInt

@Composable
internal fun AppearanceSection(
    themeMode: ThemeMode,
    fontScale: Float,
    onIntent: (SettingIntent) -> Unit
) {
    val selectedFontScaleIndex = fontScalePresetIndex(fontScale)

    SettingsSectionList {
        item {
            NofyCardSurface {
                NofyTitleLargeText(text = stringResource(R.string.settings_theme_heading))
                Column(verticalArrangement = Arrangement.spacedBy(NofySpacing.md)) {
                    ThemeModeRow(
                        leftThemeMode = ThemeMode.Light,
                        rightThemeMode = ThemeMode.Dark,
                        selectedThemeMode = themeMode,
                        onThemeSelected = { onIntent(SettingIntent.SelectThemeMode(it)) }
                    )
                    ThemeModeRow(
                        leftThemeMode = ThemeMode.GreenLight,
                        rightThemeMode = ThemeMode.GreenDark,
                        selectedThemeMode = themeMode,
                        onThemeSelected = { onIntent(SettingIntent.SelectThemeMode(it)) }
                    )
                }
            }
        }

        item {
            NofyCardSurface {
                NofyCardSectionHeader(
                    title = stringResource(R.string.settings_font_size_heading),
                    supporting = stringResource(
                        R.string.settings_font_size_value,
                        (fontScale * 100).toInt()
                    ),
                )
                NofySlider(
                    value = selectedFontScaleIndex.toFloat(),
                    onValueChange = { sliderValue ->
                        val presetIndex = sliderValue.roundToInt().coerceIn(0, FontScalePresets.lastIndex)
                        onIntent(SettingIntent.ChangeFontScale(FontScalePresets[presetIndex]))
                    },
                    valueRange = 0f..FontScalePresets.lastIndex.toFloat(),
                    steps = (FontScalePresets.size - 2).coerceAtLeast(0)
                )
            }
        }

        item {
            NofyCardSurface {
                NofyCardSectionHeader(
                    title = stringResource(R.string.settings_language_heading),
                    supporting = stringResource(R.string.settings_language_body),
                )
            }
        }
    }
}

@NofyPreview
@Composable
private fun AppearanceSectionPreview() {
    NofyPreviewSurface {
        AppearanceSection(
            themeMode = previewSettingState().themeMode,
            fontScale = previewSettingState().fontScale,
            onIntent = {}
        )
    }
}

@Composable
private fun ThemeModeRow(
    leftThemeMode: ThemeMode,
    rightThemeMode: ThemeMode,
    selectedThemeMode: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(NofySpacing.md)
    ) {
        NofyChoiceChip(
            label = leftThemeMode.label(),
            selected = selectedThemeMode == leftThemeMode,
            onClick = { onThemeSelected(leftThemeMode) },
            modifier = Modifier.weight(1f)
        )
        NofyChoiceChip(
            label = rightThemeMode.label(),
            selected = selectedThemeMode == rightThemeMode,
            onClick = { onThemeSelected(rightThemeMode) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ThemeMode.label(): String {
    return when (this) {
        ThemeMode.Light -> stringResource(R.string.settings_theme_light)
        ThemeMode.Dark -> stringResource(R.string.settings_theme_dark)
        ThemeMode.GreenLight -> stringResource(R.string.settings_theme_green_light)
        ThemeMode.GreenDark -> stringResource(R.string.settings_theme_green_dark)
    }
}
