package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.models.PhotoStudioConfig
import com.example.ui.CyberViewModel
import com.example.ui.components.PreviewDocumentType
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyDark
import com.example.ui.theme.SaffronPrimary

@Composable
fun PhotoPvcScreen(viewModel: CyberViewModel) {
    val config by viewModel.photoConfig.collectAsState()

    var photoCount by remember { mutableIntStateOf(config.photoCount) }
    var selectedBg by remember { mutableStateOf(config.backgroundColorName) }
    var candidateName by remember { mutableStateOf(config.studentName) }
    var photoDate by remember { mutableStateOf(config.dateOnPhoto) }
    var targetKb by remember { mutableFloatStateOf(config.targetFileSizeKb.toFloat()) }
    var isSignature by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "पासपोर्ट फोटो, PVC कार्ड व मीडिया स्टूडियो",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "4x6 शीट पर 6/8/12 फोटो, ऑटो बैकग्राउंड एवं सटीक KB साइज कंप्रेसर",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 1. Photo Sheet Configuration Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "1. 4x6 इंच फोटो शीट लेआउट",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NavyDark
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "शीट पर फोटो संख्या:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(6, 8, 12).forEach { count ->
                        FilterChip(
                            selected = photoCount == count,
                            onClick = { photoCount = count },
                            label = { Text("$count फोटो", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "बैकग्राउंड रंग (Background Removal):", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                val bgColors = listOf(
                    "Studio White" to Color.White,
                    "Studio Blue" to Color(0xFF3B82F6),
                    "Light Grey" to Color(0xFFE2E8F0),
                    "Crimson Red" to Color(0xFFDC2626)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    bgColors.forEach { (name, color) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(color)
                                .border(
                                    width = if (selectedBg == name) 3.dp else 1.dp,
                                    color = if (selectedBg == name) SaffronPrimary else Color(0xFF94A3B8),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedBg = name },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedBg == name) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = if (color == Color.White) Color.Black else Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = candidateName,
                        onValueChange = { candidateName = it },
                        label = { Text("फोटो पर नाम (Name on Photo)") },
                        modifier = Modifier.weight(1.2f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = photoDate,
                        onValueChange = { photoDate = it },
                        label = { Text("दिनांक (DOB/Date)") },
                        modifier = Modifier.weight(0.8f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        viewModel.updatePhotoConfig(
                            PhotoStudioConfig(
                                photoCount = photoCount,
                                backgroundColorName = selectedBg,
                                studentName = candidateName,
                                dateOnPhoto = photoDate,
                                targetFileSizeKb = targetKb.toInt()
                            )
                        )
                        viewModel.showPreview(PreviewDocumentType.PASSPORT_SHEET)
                    },
                    modifier = Modifier.fillMaxWidth().testTag("preview_photo_sheet_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("4x6 पासपोर्ट शीट का लाइव प्रिव्यू देखें")
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Strict File Compressor Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "2. ऑटो-क्रॉप एवं साइज कंप्रेसर",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NavyDark
                    )
                    Surface(
                        color = EmeraldSuccess.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "लक्ष्य: ${targetKb.toInt()} KB",
                            color = EmeraldSuccess,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Presets row
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalButton(
                        onClick = { targetKb = 35f; isSignature = false },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("फोटो 20-50KB", fontSize = 10.sp)
                    }
                    FilledTonalButton(
                        onClick = { targetKb = 15f; isSignature = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("साइन 10-20KB", fontSize = 10.sp)
                    }
                    FilledTonalButton(
                        onClick = { targetKb = 150f; isSignature = false },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("दस्तावेज <200KB", fontSize = 10.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Slider(
                    value = targetKb,
                    onValueChange = { targetKb = it },
                    valueRange = 10f..200f,
                    steps = 18,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "सरकारी फॉर्म (SSC, UPSC, UP Police) के मानक अनुसार इमेज का आकार बिना क्वालिटी खोए स्वतः कंप्रेस होगा।",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. PVC Card Maker
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "3. प्लास्टिक PVC कार्ड प्रिंटिंग लेआउट",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NavyDark
                )
                Text(
                    text = "आधार, पैन, ई-श्रम, आयुष्मान कार्ड को 85.60 × 53.98 मिमी मानक PVC कार्ड ट्रे पर प्रिंट करने योग्य फ्रंट व बैक अलाइनमेंट।",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                )

                Button(
                    onClick = { viewModel.showPreview(PreviewDocumentType.PVC_CARD) },
                    modifier = Modifier.fillMaxWidth().testTag("preview_pvc_card_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyDark)
                ) {
                    Icon(Icons.Default.CreditCard, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("PVC कार्ड लेआउट देखें व प्रिंट करें")
                }
            }
        }
    }
}
