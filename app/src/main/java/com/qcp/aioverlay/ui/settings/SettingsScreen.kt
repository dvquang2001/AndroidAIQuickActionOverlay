package com.qcp.aioverlay.ui.settings

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.qcp.aioverlay.R
import com.qcp.aioverlay.ui.theme.QuickActionOverlayTheme

// ── Local UI-only models ───────────────────────────────────────────────────────
// These drive purely visual state. Real persistence (DataStore/ViewModel) is
// out of scope per task rules — local remember{} state is used as a placeholder.

private enum class ThemeMode { LIGHT, DARK, SYSTEM }

@get:StringRes
private val ThemeMode.labelRes: Int
    get() = when (this) {
        ThemeMode.LIGHT -> R.string.settings_theme_light
        ThemeMode.DARK -> R.string.settings_theme_dark
        ThemeMode.SYSTEM -> R.string.settings_theme_system
    }

private val ThemeMode.icon: ImageVector
    get() = when (this) {
        ThemeMode.LIGHT -> Icons.Default.LightMode
        ThemeMode.DARK -> Icons.Default.DarkMode
        ThemeMode.SYSTEM -> Icons.Default.SettingsBrightness
    }

private data class AppLanguage(
    val displayName: String,
    val nativeName: String,
    val flag: String,
    val code: String
)

private val primaryLanguages = listOf(
    AppLanguage("English",             "English",              "🇺🇸", "en"),
    AppLanguage("Portuguese (Brazil)", "Português (Brasil)",   "🇧🇷", "pt-BR"),
    AppLanguage("Hindi",               "हिन्दी",               "🇮🇳", "hi")
)

private val europeLanguages = listOf(
    AppLanguage("Spanish",  "Español",    "🇪🇸", "es"),
    AppLanguage("French",   "Français",   "🇫🇷", "fr"),
    AppLanguage("German",   "Deutsch",    "🇩🇪", "de"),
    AppLanguage("Italian",  "Italiano",   "🇮🇹", "it"),
    AppLanguage("Dutch",    "Nederlands", "🇳🇱", "nl")
)

// ── Root screen ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    // UI-only local state (placeholder — real persistence needs DataStore + ViewModel)
    var selectedTheme by remember { mutableStateOf(ThemeMode.SYSTEM) }
    var selectedLanguage by remember { mutableStateOf(primaryLanguages[0]) }
    var showLanguagePicker by remember { mutableStateOf(false) }

    val appVersion = remember {
        runCatching {
            context.packageManager
                .getPackageInfo(context.packageName, 0)
                .versionName ?: "1.0"
        }.getOrDefault("1.0")
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    QuickActionOverlayTheme {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                LargeTopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.settings_title),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = stringResource(R.string.settings_cd_back)
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.surface
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // ── Appearance ─────────────────────────────────────────────────
                item {
                    SettingsSection(
                        title = stringResource(R.string.settings_section_appearance),
                        icon = Icons.Default.Palette
                    ) {
                        ThemeSelector(
                            selected = selectedTheme,
                            onSelect = { selectedTheme = it }
                        )
                    }
                }

                // ── Language ───────────────────────────────────────────────────
                item {
                    SettingsSection(
                        title = stringResource(R.string.settings_section_language),
                        icon = Icons.Default.Language
                    ) {
                        SettingsItem(
                            icon = Icons.Default.Translate,
                            title = stringResource(R.string.settings_language_label),
                            subtitle = "${selectedLanguage.flag}  ${selectedLanguage.displayName}",
                            trailingContent = TrailingContent.Arrow,
                            onClick = { showLanguagePicker = true }
                        )
                        SettingsDivider()
                        SettingsItem(
                            icon = Icons.Default.AddCircleOutline,
                            title = stringResource(R.string.settings_more_languages),
                            trailingContent = TrailingContent.Arrow,
                            onClick = { /* placeholder — future feature */ }
                        )
                    }
                }

                // ── About ──────────────────────────────────────────────────────
                item {
                    SettingsSection(
                        title = stringResource(R.string.settings_section_about),
                        icon = Icons.Default.Info
                    ) {
                        SettingsItem(
                            icon = Icons.Default.Info,
                            title = stringResource(R.string.settings_version),
                            subtitle = appVersion,
                            trailingContent = TrailingContent.None,
                            onClick = null
                        )
                        SettingsDivider()
                        SettingsItem(
                            icon = Icons.Default.Policy,
                            title = stringResource(R.string.settings_privacy_policy),
                            trailingContent = TrailingContent.ExternalLink,
                            onClick = { uriHandler.openUri("https://example.com/privacy") }
                        )
                        SettingsDivider()
                        SettingsItem(
                            icon = Icons.Default.Gavel,
                            title = stringResource(R.string.settings_terms_of_service),
                            trailingContent = TrailingContent.ExternalLink,
                            onClick = { uriHandler.openUri("https://example.com/terms") }
                        )
                    }
                }

                item { Spacer(Modifier.height(16.dp)) }
            }
        }

        // Language picker dialog
        if (showLanguagePicker) {
            LanguagePickerDialog(
                selected = selectedLanguage,
                onSelect = { lang ->
                    selectedLanguage = lang
                    showLanguagePicker = false
                },
                onDismiss = { showLanguagePicker = false }
            )
        }
    }
}

// ── Section wrapper ────────────────────────────────────────────────────────────

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        // Section header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(15.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Card body
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(content = content)
        }
    }
}

// ── Divider ────────────────────────────────────────────────────────────────────

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        // 16 horizontal padding + 38 icon + 14 gap = 68dp indent
        modifier = Modifier.padding(start = 68.dp, end = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}

// ── Generic settings row ───────────────────────────────────────────────────────

private sealed interface TrailingContent {
    data object Arrow : TrailingContent
    data object ExternalLink : TrailingContent
    data object None : TrailingContent
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    trailingContent: TrailingContent = TrailingContent.Arrow,
    onClick: (() -> Unit)?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Leading icon bubble
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Text block
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            if (subtitle != null) {
                Spacer(Modifier.height(1.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Trailing
        when (trailingContent) {
            TrailingContent.Arrow -> Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            TrailingContent.ExternalLink -> Icon(
                imageVector = Icons.Default.OpenInNew,
                contentDescription = stringResource(R.string.settings_cd_open_link),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            TrailingContent.None -> Unit
        }
    }
}

// ── Theme segmented selector ───────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeSelector(
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit
) {
    val modes = ThemeMode.entries

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = stringResource(R.string.settings_theme_label),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(12.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            modes.forEachIndexed { index, mode ->
                val isSelected = selected == mode
                SegmentedButton(
                    selected = isSelected,
                    onClick = { onSelect(mode) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = modes.size
                    ),
                    // Provide a custom icon that is always visible (not just on selection)
                    icon = { /* suppress default checkmark — icon lives inside label */ }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = mode.icon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer
                                   else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = stringResource(mode.labelRes),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

// ── Language picker dialog ─────────────────────────────────────────────────────

@Composable
private fun LanguagePickerDialog(
    selected: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp
        ) {
            Column {
                // Header
                Text(
                    text = stringResource(R.string.settings_language_picker_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(
                        start = 24.dp, top = 24.dp, end = 24.dp, bottom = 8.dp
                    )
                )

                // Language list
                LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
                    item {
                        LanguageGroupHeader(
                            stringResource(R.string.settings_language_group_primary)
                        )
                    }
                    items(primaryLanguages, key = { it.code }) { lang ->
                        LanguageRow(
                            language = lang,
                            isSelected = selected.code == lang.code,
                            onClick = { onSelect(lang) }
                        )
                    }
                    item {
                        LanguageGroupHeader(
                            stringResource(R.string.settings_language_group_europe)
                        )
                    }
                    items(europeLanguages, key = { it.code }) { lang ->
                        LanguageRow(
                            language = lang,
                            isSelected = selected.code == lang.code,
                            onClick = { onSelect(lang) }
                        )
                    }
                }

                // Footer close button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.settings_language_picker_close))
                    }
                }
            }
        }
    }
}

// ── Language group header ──────────────────────────────────────────────────────

@Composable
private fun LanguageGroupHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(
            start = 24.dp, top = 16.dp, end = 24.dp, bottom = 4.dp
        )
    )
}

// ── Language row ───────────────────────────────────────────────────────────────

@Composable
private fun LanguageRow(
    language: AppLanguage,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                else Color.Transparent
            )
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Flag emoji
        Text(
            text = language.flag,
            style = MaterialTheme.typography.titleLarge
        )

        // Name + native name
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = language.displayName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
            Text(
                text = language.nativeName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Checkmark when selected
        AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
