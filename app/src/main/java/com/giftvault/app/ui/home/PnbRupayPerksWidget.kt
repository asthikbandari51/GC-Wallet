package com.giftvault.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.giftvault.app.core.rupay.PnbCardBenefitSummary
import com.giftvault.app.ui.components.glass.*

@Composable
fun PnbRupayPerksWidget(
    summaries: List<PnbCardBenefitSummary>,
    onOpenFullPerksModal: () -> Unit,
    modifier: Modifier = Modifier,
    tokens: LiquidGlassTokens = currentGlassTokens()
) {
    if (summaries.isEmpty()) return

    var selectedCardIndex by remember { mutableStateOf(0) }
    val currentSummary = summaries.getOrNull(selectedCardIndex) ?: summaries.first()

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.WorkspacePremium,
                    contentDescription = null,
                    tint = Color(0xFFFFB800),
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "RuPay Quarterly Perks",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = tokens.textPrimary
                )
            }

            Text(
                text = "View All",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = tokens.primaryAccent,
                modifier = Modifier.clickable { onOpenFullPerksModal() }
            )
        }

        Spacer(Modifier.height(10.dp))

        LiquidGlassCard(
            shape = RoundedCornerShape(24.dp),
            elevation = 3.dp,
            modifier = Modifier.fillMaxWidth(),
            tokens = tokens
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card Switcher Chips (Imperial 1, Imperial 2, Imperial 3)
                if (summaries.size > 1) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(summaries.indices.toList()) { idx ->
                            val item = summaries[idx]
                            val isSelected = idx == selectedCardIndex
                            LiquidGlassChip(
                                selected = isSelected,
                                onClick = { selectedCardIndex = idx },
                                label = "${item.card.cardNickname} (••${item.card.lastFourDigits})",
                                tokens = tokens
                            )
                        }
                    }
                }

                // Status Banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${currentSummary.card.cardNickname} • ${currentSummary.quarterLabel}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = tokens.textPrimary
                        )
                        Text(
                            text = "${currentSummary.claimedCount} of ${currentSummary.totalCount} benefits claimed",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0x2210B981)
                    ) {
                        Text(
                            text = "₹${String.format("%,.0f", currentSummary.totalClaimedValue)} / ₹${String.format("%,.0f", currentSummary.totalPotentialValue)}",
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Progress Bar
                val progress = if (currentSummary.totalCount > 0) currentSummary.claimedCount.toFloat() / currentSummary.totalCount else 0f
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = Color(0xFF10B981),
                    trackColor = if (tokens.isDark) Color(0x332A2D5C) else Color(0xFFE2E8F0)
                )

                // Visual Benefit Grid
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    currentSummary.benefits.chunked(2).forEach { rowPair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowPair.forEach { benefit ->
                                LiquidGlassSurface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onOpenFullPerksModal() },
                                    shape = RoundedCornerShape(12.dp),
                                    surfaceBrush = if (benefit.isClaimed) tokens.elevatedGradient else tokens.surfaceGradient,
                                    borderBrush = if (benefit.isClaimed) tokens.activeBorderGradient else tokens.borderGradient,
                                    borderWidth = 1.dp,
                                    tokens = tokens
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (benefit.isClaimed) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                            contentDescription = null,
                                            tint = if (benefit.isClaimed) Color(0xFF10B981) else Color(0xFFF59E0B),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = benefit.category.shortName,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = tokens.textPrimary,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = if (benefit.isClaimed) "Claimed (₹${String.format("%,.0f", benefit.category.quarterlyValue)})" else "Available",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (benefit.isClaimed) Color(0xFF10B981) else tokens.textTertiary,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }
                            if (rowPair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}
