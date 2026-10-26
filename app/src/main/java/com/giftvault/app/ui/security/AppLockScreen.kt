package com.giftvault.app.ui.security

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Backspace
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.giftvault.app.core.security.BiometricAuthManager
import com.giftvault.app.ui.components.glass.LiquidGlassSurface
import com.giftvault.app.ui.components.glass.LiquidGlassTokens

@Composable
fun AppLockScreen(
    userName: String?,
    savedPin: String?,
    biometricAuthManager: BiometricAuthManager,
    onUnlocked: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val canUseBiometrics = remember {
        biometricAuthManager.canAuthenticate(context)
    }

    LaunchedEffect(Unit) {
        if (canUseBiometrics && context is FragmentActivity) {
            biometricAuthManager.authenticate(
                activity = context,
                onSuccess = { onUnlocked() },
                onError = { /* Allow PIN entry fallback */ }
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF090C1B),
                        Color(0xFF121634),
                        Color(0xFF070812)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            // Luminous Liquid Glass Lock Badge
            LiquidGlassSurface(
                shape = RoundedCornerShape(26.dp),
                elevation = 6.dp,
                surfaceBrush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF818CF8).copy(alpha = 0.25f),
                        Color(0xFF4F46E5).copy(alpha = 0.12f)
                    )
                ),
                borderBrush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFA5B4FC).copy(alpha = 0.65f),
                        Color(0xFF6366F1).copy(alpha = 0.20f)
                    )
                ),
                modifier = Modifier.size(76.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = Color(0xFFA5B4FC),
                        modifier = Modifier.size(38.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Welcome back, ${userName ?: "Vault Holder"}!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Enter your 4-digit PIN to unlock",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.70f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 4 Luminous Dots Indicator with spring scaling
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 4) {
                    val filled = i < enteredPin.length
                    val dotScale by animateFloatAsState(
                        targetValue = if (filled) 1.25f else 1.0f,
                        animationSpec = LiquidGlassTokens.SpringTactile,
                        label = "dot_scale_$i"
                    )
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .scale(dotScale)
                            .clip(CircleShape)
                            .background(
                                if (filled) {
                                    Brush.radialGradient(
                                        colors = listOf(Color(0xFFA5B4FC), Color(0xFF6366F1))
                                    )
                                } else {
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.22f),
                                            Color.White.copy(alpha = 0.08f)
                                        )
                                    )
                                }
                            )
                            .border(
                                width = 1.dp,
                                color = if (filled) Color(0xFFC7D2FE) else Color.White.copy(alpha = 0.28f),
                                shape = CircleShape
                            )
                    )
                }
            }

            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = errorMessage ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFF87171),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(44.dp))

            // Keypad Grid (1-9, BIO/Empty, 0, Backspace)
            val keypad = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("BIO", "0", "DEL")
            )

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                keypad.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        row.forEach { key ->
                            KeypadButton(
                                key = key,
                                canUseBiometrics = canUseBiometrics,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    when (key) {
                                        "DEL" -> {
                                            if (enteredPin.isNotEmpty()) {
                                                enteredPin = enteredPin.dropLast(1)
                                                errorMessage = null
                                            }
                                        }
                                        "BIO" -> {
                                            if (canUseBiometrics && context is FragmentActivity) {
                                                biometricAuthManager.authenticate(
                                                    activity = context,
                                                    onSuccess = { onUnlocked() },
                                                    onError = { msg -> errorMessage = msg }
                                                )
                                            }
                                        }
                                        else -> {
                                            if (enteredPin.length < 4) {
                                                enteredPin += key
                                                errorMessage = null
                                                if (enteredPin.length == 4) {
                                                    if (savedPin == null || enteredPin == savedPin) {
                                                        onUnlocked()
                                                    } else {
                                                        errorMessage = "Incorrect PIN"
                                                        enteredPin = ""
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(
    key: String,
    canUseBiometrics: Boolean,
    onClick: () -> Unit
) {
    if (key == "BIO" && !canUseBiometrics) {
        Spacer(modifier = Modifier.size(72.dp))
        return
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = LiquidGlassTokens.SpringTactile,
        label = "keypad_press"
    )

    LiquidGlassSurface(
        shape = CircleShape,
        elevation = if (isPressed) 1.dp else 4.dp,
        surfaceBrush = if (isPressed) {
            Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.24f),
                    Color.White.copy(alpha = 0.14f)
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.10f),
                    Color.White.copy(alpha = 0.04f)
                )
            )
        },
        borderBrush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = if (isPressed) 0.55f else 0.32f),
                Color.White.copy(alpha = if (isPressed) 0.25f else 0.10f)
            )
        ),
        borderWidth = 1.dp,
        modifier = Modifier
            .size(72.dp)
            .scale(scale)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (key) {
                "DEL" -> Icon(
                    imageVector = Icons.Rounded.Backspace,
                    contentDescription = "Delete",
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(24.dp)
                )
                "BIO" -> Icon(
                    imageVector = Icons.Rounded.Fingerprint,
                    contentDescription = "Biometric",
                    tint = Color(0xFFA5B4FC),
                    modifier = Modifier.size(30.dp)
                )
                else -> Text(
                    text = key,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 26.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}
