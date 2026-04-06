package com.qcp.aioverlay.ui.overlay

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
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
        // 72dp outer box = 64dp circle + 8dp head-room for the dismiss badge
        Box(modifier = Modifier.size(72.dp)) {

            // ── Glow / bloom layer ─────────────────────────────────────────────
            // A blurred filled circle drawn behind the surface creates a soft
            // frosted-glass halo effect without touching WindowManager params.
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.BottomStart)
                    .blur(radius = 22.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                        shape = CircleShape
                    )
            )

            // ── Main circle button ─────────────────────────────────────────────
            Surface(
                onClick = onActionClick,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f),
                border = BorderStroke(
                    width = 1.5.dp,
                    color = Color.White.copy(alpha = 0.40f)
                ),
                shadowElevation = 10.dp,
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.BottomStart)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.floating_btn_label),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // ── Dismiss badge ──────────────────────────────────────────────────
            // Small circle sits at the top-right corner of the 72dp box,
            // visually overlapping the main circle's edge.
            Surface(
                onClick = onDismiss,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                ),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .size(22.dp)
                    .align(Alignment.TopEnd)
            ) {
                Box(contentAlignment = Alignment.Center) {
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
}
