package com.giftvault.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.giftvault.app.core.sharing.ShareMode
import com.giftvault.app.domain.model.GiftCard
import com.giftvault.app.ui.components.glass.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickShareSheet(
    card: GiftCard,
    onDismiss: () -> Unit,
    onSelectShareMode: (ShareMode) -> Unit,
    tokens: LiquidGlassTokens = currentGlassTokens()
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.Transparent,
        scrimColor = if (tokens.isDark) Color(0xAA050814) else Color(0x660F172A),
        dragHandle = null
    ) {
        LiquidGlassSurface(
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            elevation = 16.dp,
            borderBrush = tokens.activeBorderGradient,
            tokens = tokens,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 22.dp)
                    .padding(top = 12.dp, bottom = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // iOS Frosted Drag Handle
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(if (tokens.isDark) Color(0x44FFFFFF) else Color(0x33000000))
                )

                // Title & Description
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Choose Share Format",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = tokens.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Select how you would like to share '${card.merchantName}'",
                        style = MaterialTheme.typography.bodyMedium,
                        color = tokens.textSecondary
                    )
                }

                // Option 1: Visual Branded Voucher (Image)
                ShareGlassOptionCard(
                    title = "Visual Branded Voucher (Image)",
                    subtitle = "Generates high-definition branded card graphic with code, PIN & balance",
                    icon = Icons.Rounded.Share,
                    iconGradient = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)),
                    tokens = tokens,
                    onClick = {
                        onSelectShareMode(ShareMode.VISUAL_CARD)
                        onDismiss()
                    }
                )

                // Option 2: Full Unmasked Details (Text)
                ShareGlassOptionCard(
                    title = "Full Unmasked Details (Text)",
                    subtitle = "Copies complete card code, PIN, balance & expiry date as ready-to-redeem text",
                    icon = Icons.Rounded.LockOpen,
                    iconGradient = listOf(Color(0xFF10B981), Color(0xFF06B6D4)),
                    tokens = tokens,
                    onClick = {
                        onSelectShareMode(ShareMode.TRUSTED_UNMASKED)
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
private fun ShareGlassOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconGradient: List<Color>,
    tokens: LiquidGlassTokens,
    onClick: () -> Unit
) {
    LiquidGlassCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        tokens = tokens
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Luminous Rounded Badge Icon
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(iconGradient)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = tokens.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.textSecondary,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = tokens.textTertiary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
