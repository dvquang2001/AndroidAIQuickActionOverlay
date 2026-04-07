package com.qcp.aioverlay.ui.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.qcp.aioverlay.R
import com.qcp.aioverlay.ui.theme.QuickActionOverlayTheme

@Composable
fun FloatingButtonContent(
    onActionClick: () -> Unit,
    onDismiss: () -> Unit
) {
    QuickActionOverlayTheme {

        val glowColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)

        Box(modifier = Modifier.size(72.dp)) {

            // ✅ Glow (NO blur → NO square)
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.BottomStart)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    glowColor,
                                    glowColor.copy(alpha = 0.2f),
                                    Color.Transparent
                                ),
                                radius = size.maxDimension / 1.2f
                            )
                        )
                    }
            )

            // ✅ Main floating button (NO Surface)
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.BottomStart)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f)
                    )
                    .border(
                        width = 1.5.dp,
                        color = Color.White.copy(alpha = 0.4f),
                        shape = CircleShape
                    )
                    .clickable { onActionClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.floating_btn_label),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // ✅ Dismiss button (NO Surface)
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .align(Alignment.TopEnd)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant,
                        CircleShape
                    )
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.floating_btn_cd_close),
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}