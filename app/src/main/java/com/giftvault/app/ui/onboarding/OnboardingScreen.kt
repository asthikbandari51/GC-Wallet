package com.giftvault.app.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.giftvault.app.core.theme.ThemeManager
import com.giftvault.app.ui.components.glass.GlassButtonStyle
import com.giftvault.app.ui.components.glass.LiquidGlassButton
import com.giftvault.app.ui.components.glass.LiquidGlassSurface

@Composable
fun OnboardingScreen(
    themeManager: ThemeManager,
    onCompleted: () -> Unit
) {
    var nameInput by remember { mutableStateOf("") }
    var pinInput by remember { mutableStateOf("") }
    var enableBiometrics by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E1B4B),
                        Color(0xFF090D16)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shield,
                        contentDescription = null,
                        tint = Color(0xFFA5B4FC),
                        modifier = Modifier.size(38.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Welcome to GiftVault",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Set up your private, encrypted gift card vault",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.75f)
            )

            Spacer(modifier = Modifier.height(28.dp))

            LiquidGlassSurface(
                shape = RoundedCornerShape(24.dp),
                elevation = 6.dp,
                surfaceBrush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E244A).copy(alpha = 0.90f),
                        Color(0xFF141936).copy(alpha = 0.85f)
                    )
                ),
                borderBrush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.35f),
                        Color(0xFF8B80F9).copy(alpha = 0.25f),
                        Color(0xFF000000).copy(alpha = 0.40f)
                    )
                ),
                borderWidth = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Name Input
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Your Name / Alias") },
                        leadingIcon = {
                            Icon(
                                Icons.Rounded.Person,
                                contentDescription = null,
                                tint = Color(0xFFA5B4FC)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF818CF8),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedLabelColor = Color(0xFFA5B4FC),
                            unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                            cursorColor = Color(0xFFA5B4FC)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // 4-Digit App PIN Input
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = {
                            if (it.length <= 4 && it.all { char -> char.isDigit() }) pinInput = it
                        },
                        label = { Text("Create 4-Digit App PIN") },
                        leadingIcon = {
                            Icon(
                                Icons.Rounded.Lock,
                                contentDescription = null,
                                tint = Color(0xFFA5B4FC)
                            )
                        },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF818CF8),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedLabelColor = Color(0xFFA5B4FC),
                            unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                            cursorColor = Color(0xFFA5B4FC)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Enable Biometrics Toggle Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Biometric / Face Unlock",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                text = "Use fingerprint or face recognition",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                        Switch(
                            checked = enableBiometrics,
                            onCheckedChange = { enableBiometrics = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF6366F1),
                                uncheckedThumbColor = Color.White.copy(alpha = 0.7f),
                                uncheckedTrackColor = Color.White.copy(alpha = 0.15f)
                            )
                        )
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
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    LiquidGlassButton(
                        text = "Get Started",
                        style = GlassButtonStyle.PRIMARY,
                        onClick = {
                            if (nameInput.trim().isEmpty()) {
                                errorMessage = "Please enter your name"
                            } else if (pinInput.length < 4) {
                                errorMessage = "PIN must be 4 digits"
                            } else {
                                themeManager.setUserName(nameInput.trim())
                                themeManager.setAppPin(pinInput)
                                themeManager.setAppLock(enableBiometrics)
                                themeManager.setOnboardingCompleted(true)
                                onCompleted()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        height = 50.dp,
                        shape = RoundedCornerShape(16.dp),
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
