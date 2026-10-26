package com.giftvault.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.giftvault.app.ui.components.glass.*

@Composable
fun EmptyVaultState(
    onAddCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    EmptyStateContainer(
        icon = Icons.Rounded.Lock,
        title = "Your Vault is Empty",
        description = "Store and organize all your physical & digital gift cards safely in one place with offline Keystore protection.",
        actionText = "Add First Card",
        onActionClick = onAddCardClick,
        modifier = modifier
    )
}

@Composable
fun EmptyReceiptsState(
    onAddReceiptClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    EmptyStateContainer(
        icon = Icons.Rounded.ReceiptLong,
        title = "No Receipts Attached",
        description = "Attach purchase bills & proof images to keep track of your card origin.",
        actionText = "Attach Receipt",
        onActionClick = onAddReceiptClick,
        modifier = modifier
    )
}

@Composable
fun EmptyRemindersState(
    onSetReminderClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    EmptyStateContainer(
        icon = Icons.Rounded.NotificationsNone,
        title = "No Reminders Active",
        description = "Never let a gift card expire. Set expiration alerts and reminders.",
        actionText = "Set Reminder",
        onActionClick = onSetReminderClick,
        modifier = modifier
    )
}

@Composable
fun EmptyStateContainer(
    icon: ImageVector,
    title: String,
    description: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    tokens: LiquidGlassTokens = currentGlassTokens()
) {
    LiquidGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(28.dp),
        elevation = 2.dp,
        tokens = tokens
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(tokens.primaryAccent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tokens.primaryAccent,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = tokens.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = tokens.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            if (actionText != null && onActionClick != null) {
                Spacer(Modifier.height(20.dp))
                LiquidGlassButton(
                    onClick = onActionClick,
                    text = actionText,
                    icon = Icons.Rounded.Add,
                    style = GlassButtonStyle.PRIMARY,
                    tokens = tokens
                )
            }
        }
    }
}
