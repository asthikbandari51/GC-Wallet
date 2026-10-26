package com.giftvault.app.ui.components

import android.os.Build
import android.view.WindowManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import kotlin.math.abs

@Composable
fun BarcodeDialog(
    merchantName: String,
    cardNumber: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect {
            dialogWindow?.let { win ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    win.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                    win.attributes = win.attributes.apply {
                        blurBehindRadius = 60
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xCC050814)),
            contentAlignment = Alignment.Center
        ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp)),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = merchantName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = "Scan at checkout",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close barcode",
                            tint = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // High-Contrast White Surface for Easy Scanner Reading
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    color = Color.White,
                    tonalElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 1D Code128 Barcode Representation
                        Code128BarcodeCanvas(
                            code = cardNumber,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Clean Spaced Monospace Card Number
                        Text(
                            text = cardNumber.chunked(4).joinToString(" "),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            ),
                            color = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                TextButton(onClick = onDismiss) {
                    Text("Done", fontWeight = FontWeight.Bold, color = Color(0xFF0D7377))
                }
            }
        }
    }
}
}

@Composable
fun Code128BarcodeCanvas(
    code: String,
    modifier: Modifier = Modifier
) {
    val pattern = remember(code) { generateBarcodePattern(code) }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val totalModules = pattern.size
        val moduleWidth = width / totalModules.toFloat()

        for (i in pattern.indices) {
            if (pattern[i]) {
                drawRect(
                    color = Color.Black,
                    topLeft = Offset(x = i * moduleWidth, y = 0f),
                    size = Size(width = moduleWidth, height = height)
                )
            }
        }
    }
}

private fun generateBarcodePattern(input: String): BooleanArray {
    val cleanInput = input.filter { it.isLetterOrDigit() }.ifEmpty { "1234567890" }
    val modulesList = mutableListOf<Boolean>()

    // Start Quiet Zone
    repeat(10) { modulesList.add(false) }

    // Pseudo-Code128 pattern generator based on character hash values
    cleanInput.forEach { char ->
        val hash = abs(char.hashCode())
        val bitPattern = hash % 64
        for (bit in 5 downTo 0) {
            val isBar = (bitPattern and (1 shl bit)) != 0
            modulesList.add(isBar)
            modulesList.add(!isBar)
        }
    }

    // End Quiet Zone
    repeat(10) { modulesList.add(false) }

    return modulesList.toBooleanArray()
}
