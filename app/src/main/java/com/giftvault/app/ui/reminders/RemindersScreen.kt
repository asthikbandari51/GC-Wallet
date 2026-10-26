package com.giftvault.app.ui.reminders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.giftvault.app.ui.components.glass.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    onNavigateToCardDetails: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: RemindersViewModel = hiltViewModel()
) {
    val tokens = currentGlassTokens()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Reminders & Alerts",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = tokens.textPrimary
                    )
                },
                navigationIcon = {
                    LiquidGlassIconButton(
                        onClick = onNavigateBack,
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        size = 38.dp,
                        iconSize = 20.dp,
                        tokens = tokens,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = tokens.primaryAccent)
            }
        } else if (uiState.activeReminders.isEmpty() && uiState.pastReminders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                    LiquidGlassSurface(
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.size(80.dp),
                        tokens = tokens
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                Icons.Rounded.Notifications,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = tokens.primaryAccent
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        "No active reminders set. Add cards with expiry dates to get automatic notifications.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = tokens.textSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (uiState.activeReminders.isNotEmpty()) {
                    item {
                        Text(
                            "Urgent / Upcoming",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = tokens.primaryAccent,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    items(uiState.activeReminders) { item ->
                        ReminderCardItem(
                            item = item,
                            onSnooze = { viewModel.snoozeReminder(it) },
                            onDismiss = { viewModel.dismissReminder(it) },
                            onViewCard = { onNavigateToCardDetails(it) },
                            tokens = tokens
                        )
                    }
                }

                if (uiState.pastReminders.isNotEmpty()) {
                    item {
                        Text(
                            "Past Alerts",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = tokens.textTertiary,
                            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                        )
                    }
                    items(uiState.pastReminders) { item ->
                        ReminderCardItem(
                            item = item,
                            onSnooze = null,
                            onDismiss = { viewModel.deleteReminder(item.reminder.id) },
                            onViewCard = { onNavigateToCardDetails(it) },
                            tokens = tokens
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReminderCardItem(
    item: ReminderUiItem,
    onSnooze: ((com.giftvault.app.domain.model.Reminder) -> Unit)?,
    onDismiss: (com.giftvault.app.domain.model.Reminder) -> Unit,
    onViewCard: (String) -> Unit,
    tokens: LiquidGlassTokens = currentGlassTokens()
) {
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        tokens = tokens
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.card?.merchantName ?: "Unknown Merchant",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = tokens.textPrimary
                )
                LiquidGlassSurface(
                    shape = RoundedCornerShape(8.dp),
                    tokens = tokens
                ) {
                    Text(
                        text = "${item.reminder.windowDays}d alert",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = tokens.primaryAccent
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Trigger Date: ${item.reminder.triggerDate}",
                style = MaterialTheme.typography.bodySmall,
                color = tokens.textSecondary
            )
            
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onSnooze != null) {
                    LiquidGlassButton(
                        onClick = { onSnooze(item.reminder) },
                        text = "Snooze (3d)",
                        style = GlassButtonStyle.SUBTLE,
                        height = 36.dp,
                        tokens = tokens
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                LiquidGlassButton(
                    onClick = { onDismiss(item.reminder) },
                    text = "Dismiss",
                    style = GlassButtonStyle.SECONDARY,
                    height = 36.dp,
                    tokens = tokens
                )
                Spacer(modifier = Modifier.width(8.dp))
                LiquidGlassButton(
                    onClick = { onViewCard(item.reminder.cardId) },
                    text = "View Card",
                    style = GlassButtonStyle.PRIMARY,
                    height = 36.dp,
                    tokens = tokens
                )
            }
        }
    }
}
