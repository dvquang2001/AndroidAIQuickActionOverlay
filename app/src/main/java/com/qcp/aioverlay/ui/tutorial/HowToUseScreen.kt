package com.qcp.aioverlay.ui.tutorial

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.qcp.aioverlay.R
import com.qcp.aioverlay.ui.theme.QuickActionOverlayTheme
import kotlinx.coroutines.launch

// ── Slide data models (UI-only) ───────────────────────────────────────────────

private enum class IllustrationType {
    WELCOME, PERMISSIONS, LONG_PRESS, FLOATING_BUTTON, TRANSLATION
}

private data class TutorialPage(
    @StringRes val titleRes: Int,
    @StringRes val descRes: Int,
    val illustration: IllustrationType
)

private val tutorialPages = listOf(
    TutorialPage(R.string.tutorial_slide1_title, R.string.tutorial_slide1_desc, IllustrationType.WELCOME),
    TutorialPage(R.string.tutorial_slide2_title, R.string.tutorial_slide2_desc, IllustrationType.PERMISSIONS),
    TutorialPage(R.string.tutorial_slide3_title, R.string.tutorial_slide3_desc, IllustrationType.LONG_PRESS),
    TutorialPage(R.string.tutorial_slide4_title, R.string.tutorial_slide4_desc, IllustrationType.FLOATING_BUTTON),
    TutorialPage(R.string.tutorial_slide5_title, R.string.tutorial_slide5_desc, IllustrationType.TRANSLATION)
)

// ── Root screen ────────────────────────────────────────────────────────────────

@Composable
fun HowToUseScreen(onDismiss: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { tutorialPages.size })
    val scope = rememberCoroutineScope()
    val isLastPage by remember { derivedStateOf { pagerState.currentPage == tutorialPages.lastIndex } }

    QuickActionOverlayTheme {
        Scaffold(containerColor = MaterialTheme.colorScheme.surface) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ── Skip button row ────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    this@Column.AnimatedVisibility(
                        visible = !isLastPage,
                        modifier = Modifier.align(Alignment.CenterEnd),
                        enter = fadeIn(tween(200)),
                        exit = fadeOut(tween(200))
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text(
                                text = stringResource(R.string.tutorial_btn_skip),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // ── Horizontal pager ───────────────────────────────────────────
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { index ->
                    SlidePageContent(page = tutorialPages[index])
                }

                // ── Dot indicator + action button ──────────────────────────────
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 16.dp, bottom = 40.dp)
                ) {
                    PagerIndicator(
                        pagerState = pagerState,
                        pageCount = tutorialPages.size
                    )
                    Spacer(Modifier.height(32.dp))
                    TutorialActionButton(
                        isLastPage = isLastPage,
                        onNext = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        onDismiss = onDismiss
                    )
                }
            }
        }
    }
}

// ── Individual slide ───────────────────────────────────────────────────────────

@Composable
private fun SlidePageContent(page: TutorialPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Illustration container — clipped so glow effects stay contained
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(RoundedCornerShape(36.dp)),
            contentAlignment = Alignment.Center
        ) {
            SlideIllustration(type = page.illustration)
        }

        Spacer(Modifier.height(40.dp))

        Text(
            text = stringResource(page.titleRes),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = stringResource(page.descRes),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
        )
    }
}

// ── Illustration dispatcher ────────────────────────────────────────────────────

@Composable
private fun SlideIllustration(type: IllustrationType) {
    when (type) {
        IllustrationType.WELCOME      -> WelcomeIllustration()
        IllustrationType.PERMISSIONS  -> PermissionsIllustration()
        IllustrationType.LONG_PRESS   -> LongPressIllustration()
        IllustrationType.FLOATING_BUTTON -> FloatingButtonIllustration()
        IllustrationType.TRANSLATION  -> TranslationIllustration()
    }
}

// ── Slide 1 — Welcome ─────────────────────────────────────────────────────────

@Composable
private fun WelcomeIllustration() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.surface
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow ring
        Box(
            modifier = Modifier
                .size(170.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    shape = CircleShape
                )
        )
        // Mid ring
        Box(
            modifier = Modifier
                .size(130.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    shape = CircleShape
                )
        )
        // Main circle
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            shadowElevation = 10.dp,
            modifier = Modifier.size(96.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = "✨", style = MaterialTheme.typography.displaySmall)
            }
        }
        // Floating accent dots — offset from center of the parent Box
        Box(
            modifier = Modifier
                .size(14.dp)
                .offset(x = (-72).dp, y = (-38).dp)
                .background(MaterialTheme.colorScheme.secondary, CircleShape)
        )
        Box(
            modifier = Modifier
                .size(10.dp)
                .offset(x = 68.dp, y = (-54).dp)
                .background(MaterialTheme.colorScheme.tertiary, CircleShape)
        )
        Box(
            modifier = Modifier
                .size(12.dp)
                .offset(x = 78.dp, y = 42.dp)
                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(8.dp)
                .offset(x = (-60).dp, y = 58.dp)
                .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f), CircleShape)
        )
    }
}

// ── Slide 2 — Permissions ─────────────────────────────────────────────────────

@Composable
private fun PermissionsIllustration() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    listOf(
                        MaterialTheme.colorScheme.secondaryContainer,
                        MaterialTheme.colorScheme.surface
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Shield icon
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 6.dp,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            // Fake permission toggle rows
            FakePermissionRow(label = "Accessibility", enabled = true)
            FakePermissionRow(label = "Overlay", enabled = true)
        }
    }
}

@Composable
private fun FakePermissionRow(label: String, enabled: Boolean) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.width(180.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(
                            if (enabled) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline,
                            CircleShape
                        )
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
            // Fake toggle pill
            Row(
                modifier = Modifier
                    .size(width = 32.dp, height = 18.dp)
                    .clip(CircleShape)
                    .background(
                        if (enabled) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                horizontalArrangement = if (enabled) Arrangement.End else Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .size(14.dp)
                        .background(Color.White, CircleShape)
                )
            }
        }
    }
}

// ── Slide 3 — Long Press ──────────────────────────────────────────────────────

@Composable
private fun LongPressIllustration() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    listOf(
                        MaterialTheme.colorScheme.tertiaryContainer,
                        MaterialTheme.colorScheme.surface
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Phone screen mockup
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shadowElevation = 6.dp,
            modifier = Modifier.size(width = 148.dp, height = 192.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Fake text lines (some highlighted = selected)
                FakeTextLine(widthFraction = 0.9f, highlighted = false)
                FakeTextLine(widthFraction = 0.75f, highlighted = false)
                // Selection highlight block
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                        .padding(vertical = 6.dp, horizontal = 4.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        FakeTextLine(widthFraction = 1f, highlighted = true)
                        FakeTextLine(widthFraction = 0.8f, highlighted = true)
                    }
                }
                FakeTextLine(widthFraction = 0.65f, highlighted = false)
                FakeTextLine(widthFraction = 0.8f, highlighted = false)
                FakeTextLine(widthFraction = 0.55f, highlighted = false)
            }
        }

        // Touch indicator circle at bottom-right of mockup
        Box(
            modifier = Modifier
                .size(28.dp)
                .offset(x = 50.dp, y = 20.dp)
                .background(
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(16.dp)
                .offset(x = 50.dp, y = 20.dp)
                .background(MaterialTheme.colorScheme.tertiary, CircleShape)
        )
    }
}

@Composable
private fun FakeTextLine(widthFraction: Float, highlighted: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .height(7.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(
                if (highlighted) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.14f)
            )
    )
}

// ── Slide 4 — Floating Button ─────────────────────────────────────────────────

@Composable
private fun FloatingButtonIllustration() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Phone mockup
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shadowElevation = 6.dp,
            modifier = Modifier.size(width = 148.dp, height = 192.dp)
        ) {
            Box {
                // Status bar hint
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                )
                // Content lines suggestion
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(4) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(if (it == 3) 0.6f else 0.9f)
                                .height(7.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f))
                        )
                    }
                }

                // Floating AI button at center-right edge
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 6.dp)
                ) {
                    // Glow bloom
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .blur(14.dp, BlurredEdgeTreatment.Unbounded)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                                CircleShape
                            )
                    )
                    // Frosted circle button
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.93f),
                        border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.4f)),
                        shadowElevation = 8.dp,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "✨",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Slide 5 — Translation ─────────────────────────────────────────────────────

@Composable
private fun TranslationIllustration() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    listOf(
                        MaterialTheme.colorScheme.secondaryContainer,
                        MaterialTheme.colorScheme.surface
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Source card
            TranslationCard(
                flag = "🇺🇸",
                word = "Hello",
                lang = "English",
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                textColor = MaterialTheme.colorScheme.onSurface
            )

            // Arrow circle
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Target card
            TranslationCard(
                flag = "🇧🇷",
                word = "Olá",
                lang = "Português",
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                textColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun TranslationCard(
    flag: String,
    word: String,
    lang: String,
    containerColor: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        shadowElevation = 3.dp,
        modifier = Modifier.width(86.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(flag, style = MaterialTheme.typography.headlineSmall)
            Text(
                text = word,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
            Text(
                text = lang,
                style = MaterialTheme.typography.labelSmall,
                color = textColor.copy(alpha = 0.65f)
            )
        }
    }
}

// ── Pager indicator dots ───────────────────────────────────────────────────────

@Composable
private fun PagerIndicator(
    pagerState: PagerState,
    pageCount: Int
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val isSelected = pagerState.currentPage == index

            val dotWidth: Dp by animateDpAsState(
                targetValue = if (isSelected) 24.dp else 8.dp,
                animationSpec = tween(durationMillis = 280),
                label = "dot_width_$index"
            )
            val dotColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                              else MaterialTheme.colorScheme.outlineVariant,
                animationSpec = tween(durationMillis = 280),
                label = "dot_color_$index"
            )

            Box(
                modifier = Modifier
                    .size(width = dotWidth, height = 8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }
    }
}

// ── Bottom action button ───────────────────────────────────────────────────────

@Composable
private fun TutorialActionButton(
    isLastPage: Boolean,
    onNext: () -> Unit,
    onDismiss: () -> Unit
) {
    Button(
        onClick = if (isLastPage) onDismiss else onNext,
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        AnimatedContent(
            targetState = isLastPage,
            transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(250)) },
            label = "action_btn_text"
        ) { lastPage ->
            Text(
                text = if (lastPage) stringResource(R.string.tutorial_btn_get_started)
                       else stringResource(R.string.tutorial_btn_next),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
