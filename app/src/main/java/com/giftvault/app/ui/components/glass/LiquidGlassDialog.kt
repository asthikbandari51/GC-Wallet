package com.giftvault.app.ui.components.glass

import android.os.Build
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider

@Composable
fun LiquidGlassDialog(
    onDismissRequest: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    properties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false),
    tokens: LiquidGlassTokens = currentGlassTokens(),
    confirmButton: (@Composable () -> Unit)? = null,
    dismissButton: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        val view = LocalView.current
        DisposableEffect(view) {
            val window = (view.parent as? DialogWindowProvider)?.window
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                window?.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                window?.attributes = window?.attributes?.apply {
                    blurBehindRadius = 60
                }
            }
            onDispose { }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (tokens.isDark) Color(0xCC050814) else Color(0x880F172A)
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismissRequest
                ),
            contentAlignment = Alignment.Center
        ) {
            LiquidGlassSurface(
                modifier = modifier
                    .fillMaxWidth(0.92f)
                    .heightIn(max = 620.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* prevent dismiss on clicking dialog body */ },
                shape = LiquidGlassTokens.ShapeModal,
                elevation = 16.dp,
                surfaceBrush = if (tokens.isDark) tokens.elevatedGradient else tokens.surfaceGradient,
                borderBrush = tokens.activeBorderGradient,
                borderWidth = 1.2.dp,
                tokens = tokens
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 20.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = tokens.textPrimary
                    )

                    Spacer(Modifier.height(14.dp))

                    // Bounded scrollable content area
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                    ) {
                        content()
                    }

                    // Pinned Action Buttons Row
                    if (confirmButton != null || dismissButton != null) {
                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (dismissButton != null && confirmButton != null) {
                                Box(modifier = Modifier.weight(1f), propagateMinConstraints = true) {
                                    dismissButton()
                                }
                                Box(modifier = Modifier.weight(1.2f), propagateMinConstraints = true) {
                                    confirmButton()
                                }
                            } else if (confirmButton != null) {
                                Box(modifier = Modifier.fillMaxWidth(), propagateMinConstraints = true) {
                                    confirmButton()
                                }
                            } else if (dismissButton != null) {
                                Box(modifier = Modifier.fillMaxWidth(), propagateMinConstraints = true) {
                                    dismissButton()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
