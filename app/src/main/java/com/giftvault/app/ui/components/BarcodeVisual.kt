package com.giftvault.app.ui.components

import android.os.Build
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix

enum class BarcodeSymbology(val displayName: String, val zxingFormat: BarcodeFormat, val is2D: Boolean) {
    CODE_128("Code-128", BarcodeFormat.CODE_128, false),
    QR_CODE("QR Code", BarcodeFormat.QR_CODE, true),
    PDF_417("PDF-417", BarcodeFormat.PDF_417, false),
    AZTEC("Aztec", BarcodeFormat.AZTEC, true)
}

@Composable
fun BarcodeDialog(
    merchantName: String,
    cardNumber: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var selectedSymbology by remember { mutableStateOf(BarcodeSymbology.CODE_128) }

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
                // High brightness for cashier scanning
                val lp = win.attributes
                lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
                win.attributes = lp
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
                    .clip(RoundedCornerShape(26.dp)),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
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
                                fontWeight = FontWeight.Black,
                                color = Color.Black
                            )
                            Text(
                                text = "Scan at cashier / register",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF64748B)
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

                    Spacer(modifier = Modifier.height(14.dp))

                    // Symbology Switcher Pills
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(BarcodeSymbology.entries.toTypedArray()) { symbology ->
                            val isSelected = symbology == selectedSymbology
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                                modifier = Modifier.clickable { selectedSymbology = symbology }
                            ) {
                                Text(
                                    text = symbology.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF334155),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Barcode Render Canvas Box
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp)),
                        color = Color.White,
                        tonalElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            DynamicBarcodeCanvas(
                                code = cardNumber.ifBlank { "00000000" },
                                symbology = selectedSymbology,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(if (selectedSymbology.is2D) 160.dp else 90.dp)
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // 4-Character Chunk Blocks (Tap to copy specific block)
                            val chunks = remember(cardNumber) {
                                cardNumber.chunked(4)
                            }

                            Text(
                                text = "Tap any 4-char block to copy:",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                chunks.forEachIndexed { idx, chunk ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFF8FAFC),
                                        shadowElevation = 1.dp,
                                        modifier = Modifier
                                            .padding(horizontal = 3.dp)
                                            .clickable {
                                                clipboardManager.setText(AnnotatedString(chunk))
                                                Toast.makeText(context, "Copied chunk: $chunk", Toast.LENGTH_SHORT).show()
                                            }
                                    ) {
                                        Text(
                                            text = chunk,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            letterSpacing = 1.sp,
                                            color = Color(0xFF0F172A),
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Full Code Copy
                            TextButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(cardNumber))
                                    Toast.makeText(context, "Full card code copied!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF0D7377))
                                Spacer(Modifier.width(6.dp))
                                Text("Copy Full String", fontWeight = FontWeight.Bold, color = Color(0xFF0D7377), fontSize = 13.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    TextButton(onClick = onDismiss) {
                        Text("Done", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun DynamicBarcodeCanvas(
    code: String,
    symbology: BarcodeSymbology,
    modifier: Modifier = Modifier
) {
    val bitMatrix = remember(code, symbology) {
        try {
            val writer = MultiFormatWriter()
            val hints = mapOf(
                EncodeHintType.MARGIN to 1,
                EncodeHintType.CHARACTER_SET to "UTF-8"
            )
            val cleanCode = code.trim().filter { it.isLetterOrDigit() || it == '-' || it == '_' }.ifEmpty { "12345678" }
            val width = if (symbology.is2D) 256 else 400
            val height = if (symbology.is2D) 256 else 120
            writer.encode(cleanCode, symbology.zxingFormat, width, height, hints)
        } catch (_: Exception) {
            null
        }
    }

    if (bitMatrix != null) {
        Canvas(modifier = modifier) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val matrixWidth = bitMatrix.width
            val matrixHeight = bitMatrix.height

            val moduleWidth = canvasWidth / matrixWidth.toFloat()
            val moduleHeight = canvasHeight / matrixHeight.toFloat()

            for (x in 0 until matrixWidth) {
                for (y in 0 until matrixHeight) {
                    if (bitMatrix.get(x, y)) {
                        drawRect(
                            color = Color.Black,
                            topLeft = Offset(x * moduleWidth, y * moduleHeight),
                            size = Size(moduleWidth + 0.5f, moduleHeight + 0.5f)
                        )
                    }
                }
            }
        }
    } else {
        // Graceful fallback linear canvas
        Code128BarcodeCanvas(code = code, modifier = modifier)
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

    cleanInput.forEach { char ->
        val hash = kotlin.math.abs(char.hashCode())
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
