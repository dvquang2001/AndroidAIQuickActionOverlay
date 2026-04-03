package com.qcp.aioverlay.ui.overlay

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.qcp.aioverlay.R
import com.qcp.aioverlay.domain.model.ActionType
import com.qcp.aioverlay.domain.model.ProcessResult
import com.qcp.aioverlay.ui.ext.labelRes
import com.qcp.aioverlay.ui.theme.QuickActionOverlayTheme

@Composable
fun OverlayScreen(
    selectedText: String,
    onDismiss: () -> Unit,
    viewModel: OverlayViewModel,
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(selectedText) {
        viewModel.onIntent(OverlayIntent.SetText(selectedText))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is OverlayEffect.CopyToClipboard -> {
                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText(context.getString(R.string.overlay_clipboard_label), effect.text))
                }
                is OverlayEffect.ShowError -> { /* show snack bar */ }
                OverlayEffect.Dismiss -> onDismiss()
            }
        }
    }

    QuickActionOverlayTheme {
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(tween(300)) { it } + fadeIn(),
            exit = slideOutVertically(tween(300)) { it } + fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // --------- Header ---------
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.overlay_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row {
                            IconButton(onClick = { viewModel.onIntent(OverlayIntent.ToggleExpanded) }) {
                                Icon(
                                    if (state.isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null
                                )
                            }
                            IconButton(onClick = { viewModel.onIntent(OverlayIntent.Dismiss) }) {
                                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.overlay_cd_close))
                            }
                        }
                    }

                    // --------- Selected text preview ---------
                    AnimatedVisibility(visible = state.isExpanded) {
                        Column {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.overlay_selected_text_label),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = state.selectedText,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // --------- Action chips ---------
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ActionType.entries
                            .filter { it != ActionType.CUSTOM }
                            .forEach { action ->
                                FilterChip(
                                    selected = state.selectedAction == action,
                                    onClick = { viewModel.onIntent(OverlayIntent.SelectAction(action)) },
                                    label = { Text(stringResource(action.labelRes), style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                    }

                    // --------- Custom prompt ---------
                    AnimatedVisibility(visible = state.selectedAction == ActionType.CUSTOM) {
                        Column {
                            Spacer(Modifier.height(8.dp))
                            TextField(
                                value = state.customPrompt,
                                onValueChange = { viewModel.onIntent(OverlayIntent.SetCustomPrompt(it)) },
                                placeholder = { Text(stringResource(R.string.overlay_custom_prompt_hint)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = false,
                                maxLines = 3
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // --------- Run button ---------
                    Button(
                        onClick = { viewModel.onIntent(OverlayIntent.RunAction) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.result !is ProcessResult.Loading,
                    ) {
                        Text(stringResource(R.string.overlay_run_action, stringResource(state.selectedAction.labelRes)))
                    }

                    Spacer(Modifier.height(12.dp))

                    // --------- Result ---------
                    state.result?.let { result ->
                        Spacer(Modifier.height(12.dp))
                        AnimatedContent(
                            targetState = result,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "result"
                        ) { targetResult ->
                            when (targetResult) {
                                ProcessResult.Loading -> {
                                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                                    }
                                }

                                is ProcessResult.Success -> {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                stringResource(R.string.overlay_result_label),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            IconButton(
                                                onClick = { viewModel.onIntent(OverlayIntent.CopyResult) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.ContentCopy,
                                                    contentDescription = stringResource(R.string.overlay_cd_copy),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.primaryContainer)
                                                .padding(12.dp)
                                        ) {
                                            Text(text = targetResult.output, style = MaterialTheme.typography.bodyMedium)
                                        }
                                    }
                                }

                                is ProcessResult.Error -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.errorContainer)
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.overlay_error_format, targetResult.message),
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
