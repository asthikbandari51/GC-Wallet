package com.giftvault.app.ui.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.giftvault.app.ui.components.glass.LiquidGlassSurface
import com.giftvault.app.ui.components.glass.LiquidGlassTokens
import com.giftvault.app.ui.components.glass.currentGlassTokens

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun GiftVaultBottomNavBar(
    navController: NavHostController,
    tokens: LiquidGlassTokens = currentGlassTokens()
) {
    val items = listOf(
        BottomNavItem(Screen.HOME.route, "Home", Icons.Rounded.Home),
        BottomNavItem(Screen.VAULT.route, "Vault", Icons.Rounded.AccountBalanceWallet),
        BottomNavItem(Screen.MANAGE_BANK_CARDS.route, "Cards", Icons.Rounded.CreditCard),
        BottomNavItem(Screen.SETTINGS.route, "Settings", Icons.Rounded.Settings)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = items.any { it.route == currentDestination?.route }

    if (showBottomBar) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 4.dp)
        ) {
            LiquidGlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(28.dp),
                elevation = 8.dp,
                surfaceBrush = Brush.verticalGradient(
                    colors = listOf(
                        if (tokens.isDark) Color(0xF0181D3E) else Color(0xFAF8FAFC),
                        if (tokens.isDark) Color(0xF010142D) else Color(0xF2F1F5F9)
                    )
                ),
                borderBrush = tokens.borderGradient,
                borderWidth = 1.2.dp,
                tokens = tokens
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEach { item ->
                        val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true

                        val scale by animateFloatAsState(
                            targetValue = if (isSelected) 1.05f else 1.0f,
                            animationSpec = LiquidGlassTokens.SpringTactile,
                            label = "nav_item_scale"
                        )

                        val activeBgColor = if (isSelected) {
                            if (tokens.isDark) Color(0x338B80F9) else Color(0x26584BF6)
                        } else Color.Transparent

                        val activeBorderBrush = if (isSelected) tokens.activeBorderGradient else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .scale(scale)
                                .clip(RoundedCornerShape(20.dp))
                                .background(activeBgColor)
                                .then(
                                    if (isSelected) {
                                        Modifier.border(1.dp, activeBorderBrush, RoundedCornerShape(20.dp))
                                    } else Modifier
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    tint = if (isSelected) tokens.primaryAccent else tokens.textTertiary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = item.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) tokens.primaryAccent else tokens.textTertiary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
