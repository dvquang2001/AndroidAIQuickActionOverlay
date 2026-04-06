package com.qcp.aioverlay.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.qcp.aioverlay.R
import com.qcp.aioverlay.domain.model.ActionType
import com.qcp.aioverlay.domain.model.HistoryItem
import com.qcp.aioverlay.ui.components.ConfirmDialog
import com.qcp.aioverlay.ui.ext.labelRes
import com.qcp.aioverlay.ui.theme.QuickActionOverlayTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onSignOut: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToHowToUse: () -> Unit = {},
    viewModel: MainViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    // ── Dialog visibility state (UI-only) ─────────────────────────────────────
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is MainEffect.NavigateTo -> context.startActivity(effect.intent)
                MainEffect.NavigateToLogin -> onSignOut()
            }
        }
    }

    QuickActionOverlayTheme {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                LargeTopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.main_title),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        // Clear all history — guarded by confirmation dialog
                        AnimatedVisibility(
                            visible = state.history.isNotEmpty(),
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            IconButton(onClick = { showClearHistoryDialog = true }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.main_cd_delete_history),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(onClick = onNavigateToHowToUse) {
                            Icon(
                                Icons.Default.Help,
                                contentDescription = stringResource(R.string.tutorial_cd_how_to_use),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = stringResource(R.string.settings_cd_navigate_settings),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        // Logout — guarded by confirmation dialog
                        IconButton(onClick = { showLogoutDialog = true }) {
                            Icon(
                                Icons.Default.Logout,
                                contentDescription = stringResource(R.string.main_cd_sign_out),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // ── Permissions section header ──
                item {
                    SectionHeader(
                        title = "Permissions",
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                // Accessibility card
                item {
                    PermissionCard(
                        icon = Icons.Default.Accessibility,
                        title = stringResource(R.string.main_permission_accessibility_title),
                        description = stringResource(R.string.main_permission_accessibility_desc),
                        isEnabled = state.isServiceEnabled,
                        onClick = { viewModel.onIntent(MainIntent.OpenAccessibilitySettings) }
                    )
                    Spacer(Modifier.height(8.dp))
                }

                // Overlay card
                item {
                    PermissionCard(
                        icon = Icons.Default.Layers,
                        title = stringResource(R.string.main_permission_overlay_title),
                        description = stringResource(R.string.main_permission_overlay_desc),
                        isEnabled = state.hasOverlayPermission,
                        onClick = { viewModel.onIntent(MainIntent.OpenOverlaySettings) }
                    )
                    Spacer(Modifier.height(24.dp))
                }

                // ── History section header ──
                item {
                    SectionHeader(
                        title = stringResource(R.string.main_history_title),
                        badge = if (state.history.isNotEmpty()) state.history.size else null,
                        icon = Icons.Default.History,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                // Empty state
                if (state.history.isEmpty()) {
                    item {
                        EmptyHistoryState()
                    }
                } else {
                    items(state.history, key = { it.id }) { item ->
                        HistoryItem(
                            item = item,
                            onDelete = { viewModel.onIntent(MainIntent.DeleteHistory(item.id)) }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }

                item { Spacer(Modifier.height(16.dp)) }
            }
        }

        // ── Logout confirmation dialog ─────────────────────────────────────────
        if (showLogoutDialog) {
            ConfirmDialog(
                title = stringResource(R.string.dialog_logout_title),
                message = stringResource(R.string.dialog_logout_message),
                confirmText = stringResource(R.string.dialog_logout_confirm),
                onConfirm = { viewModel.onIntent(MainIntent.SignOut) },
                onDismiss = { showLogoutDialog = false },
                isDestructive = false
            )
        }

        // ── Clear all history confirmation dialog ─────────────────────────────
        if (showClearHistoryDialog) {
            ConfirmDialog(
                title = stringResource(R.string.dialog_clear_history_title),
                message = stringResource(R.string.dialog_clear_history_message),
                confirmText = stringResource(R.string.dialog_clear_history_confirm),
                onConfirm = { viewModel.onIntent(MainIntent.ClearHistory) },
                onDismiss = { showClearHistoryDialog = false },
                isDestructive = true
            )
        }
    }
}

// ── Section header ───────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    badge: Int? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (badge != null) {
            Spacer(Modifier.width(8.dp))
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = badge.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                )
            }
        }
    }
}

// ── Permission card ───────────────────────────────────────────────────────────

@Composable
fun PermissionCard(
    icon: ImageVector,
    title: String,
    description: String,
    isEnabled: Boolean,
    onClick: () -> Unit,
) {
    val activeColor = MaterialTheme.colorScheme.primaryContainer
    val inactiveColor = MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) activeColor.copy(alpha = 0.4f)
                             else MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Leading icon bubble
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isEnabled) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isEnabled) MaterialTheme.colorScheme.onPrimary
                               else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Text
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Switch
            Switch(
                checked = isEnabled,
                onCheckedChange = null,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyHistoryState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(64.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Text(
            text = stringResource(R.string.main_history_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── History item ──────────────────────────────────────────────────────────────

@Composable
fun HistoryItem(
    item: HistoryItem,
    onDelete: () -> Unit
) {
    val date = SimpleDateFormat("dd MMM · HH:mm", Locale.getDefault()).format(Date(item.createdAt))

    // Dialog state local to this item — shown when user taps the delete icon
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp)) {

            // Top row: action chip + date + delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Action type pill
                ActionTypePill(actionType = item.actionType)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = date,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    // Delete — guarded by confirmation dialog
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.main_cd_delete_item),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            // Input text (muted label)
            Text(
                text = item.inputText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(4.dp))

            // Output text
            Text(
                text = item.outputText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    // ── Delete single item confirmation dialog ────────────────────────────────
    if (showDeleteDialog) {
        ConfirmDialog(
            title = stringResource(R.string.dialog_delete_title),
            message = stringResource(R.string.dialog_delete_message),
            confirmText = stringResource(R.string.dialog_delete_confirm),
            onConfirm = onDelete,   // calls the existing lambda passed from parent
            onDismiss = { showDeleteDialog = false },
            isDestructive = true
        )
    }
}

// ── Action type pill ──────────────────────────────────────────────────────────

@Composable
private fun ActionTypePill(actionType: ActionType) {
    val (bgColor, textColor) = when (actionType) {
        ActionType.TRANSLATE -> MaterialTheme.colorScheme.tertiaryContainer to
                                MaterialTheme.colorScheme.onTertiaryContainer
        ActionType.SUMMARIZE -> MaterialTheme.colorScheme.primaryContainer to
                                MaterialTheme.colorScheme.onPrimaryContainer
        ActionType.EXPLAIN   -> MaterialTheme.colorScheme.secondaryContainer to
                                MaterialTheme.colorScheme.onSecondaryContainer
        ActionType.CUSTOM    -> MaterialTheme.colorScheme.surfaceVariant to
                                MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = bgColor
    ) {
        Text(
            text = stringResource(actionType.labelRes),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
