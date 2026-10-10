package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CyberViewModel
import com.example.ui.components.PreviewDocumentType
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyDark
import com.example.ui.theme.SaffronPrimary

@Composable
fun WpsOcrScreen(viewModel: CyberViewModel) {
    var activeSubTab by remember { mutableIntStateOf(0) } // 0: OCR Scan-to-Text, 1: WPS Bio-Data / Application

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = activeSubTab == 0,
                onClick = { activeSubTab = 0 },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) {
                Text("OCR स्कैन-टू-टेक्स्ट", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            SegmentedButton(
                selected = activeSubTab == 1,
                onClick = { activeSubTab = 1 },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) {
                Text("1-क्लिक बायो-डाटा / Resume", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (activeSubTab == 0) {
            OcrScanTab(viewModel)
        } else {
            BioDataTab(viewModel)
        }
    }
}

@Composable
private fun OcrScanTab(viewModel: CyberViewModel) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val sampleDocs = listOf(
        "हस्तलिखित शपथ पत्र" to "समक्ष: श्रीमान नोटरी पब्लिक, लखनऊ।\nमैं रमेश कुमार, पुत्र श्री जगदीश प्रसाद, शपथपूर्वक बयान करता हूँ कि मेरा सहारा खाता सं० 49201 और आधार सं० में पिता का नाम समान है। लिपिकीय त्रुटि के कारण उपनाम में विसंगति हुई है।",
        "पुलिस e-FIR प्रतिलिपि" to "थाना: हजरतगंज, लखनऊ। प्रथम सूचना रिपोर्ट सं०: 114/2026।\nवादी का नाम: संजय कुमार। घटना का विवरण: दिनांक 08/10/2026 को गोमती नगर से रेडमी नोट मोबाइल व पर्स गुम हो गया। जांच अधिकारी: उ०नि० सत्येंद्र सिंह।",
        "सहारा रिफंड जमा रसीद" to "सोसायटी: हमारा इंडिया क्रेडिट को-ऑपरेटिव सोसायटी लिमिटेड।\nरसीद सं०: 893120, खाता सं०: 0492190012, प्रमाणपत्र सं०: 00921094।\nजमा धनराशि: ₹35,000 (पैंतीस हजार रुपये मात्र)। परिपक्वता तिथि: 14/05/2024।"
    )

    var currentOcrText by remember { mutableStateOf(sampleDocs[0].second) }
    var selectedPresetIndex by remember { mutableIntStateOf(0) }

    Text(
        text = "दस्तावेज स्कैन एवं OCR टेक्स्ट निष्कर्षण",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = "स्कैन की गई कॉपी, FIR या हस्तलिखित फॉर्म को एडिटेबल Word/Text में बदलें",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(10.dp))

    Text(text = "नमूना दस्तावेज चुनें (Sample Documents):", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        sampleDocs.forEachIndexed { index, (label, text) ->
            FilterChip(
                selected = selectedPresetIndex == index,
                onClick = {
                    selectedPresetIndex = index
                    currentOcrText = text
                    viewModel.voiceAgent.speak("$label का OCR स्कैन पूर्ण हुआ। टेक्स्ट एडिटेबल बॉक्स में आ गया है।", listenAfterSpeaking = false)
                },
                label = { Text(label, fontSize = 10.sp) },
                modifier = Modifier.weight(1f)
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Editable WPS Editor Box
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "WPS टेक्स्ट एडिटर (Editable Text)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = NavyDark
                )

                Row {
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(currentOcrText))
                            viewModel.voiceAgent.speak("टेक्स्ट क्लिपबोर्ड पर कॉपी हो गया।", listenAfterSpeaking = false)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = SaffronPrimary, modifier = Modifier.size(18.dp))
                    }

                    IconButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, currentOcrText)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Document Text"))
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = NavyDark, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = currentOcrText,
                onValueChange = { currentOcrText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .testTag("ocr_editable_text_area"),
                textStyle = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        viewModel.voiceAgent.speak("WPS Word प्रारूप (.docx) तैयार हो गया है।", listenAfterSpeaking = false)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Icon(Icons.AutoMirrored.Filled.Article, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Word (.docx)", fontSize = 11.sp)
                }

                Button(
                    onClick = {
                        viewModel.voiceAgent.speak("Excel स्प्रेडशीट (.xlsx) तैयार हो गई है।", listenAfterSpeaking = false)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                ) {
                    Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Excel (.xlsx)", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun BioDataTab(viewModel: CyberViewModel) {
    Text(
        text = "1-क्लिक प्रिंट-रेडी बायो-डाटा एवं आवेदन पत्र",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = "नौकरी, बैंक, पुलिस वेरिफिकेशन एवं विवाह हेतु प्रोफेशनल रिज्यूम",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(12.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = "उम्मीदवार विवरण (Candidate Profile)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = "रवि कुमार (Ravi Kumar)",
                onValueChange = {},
                label = { Text("पूरा नाम") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = "Data Entry Operator / Computer Assistant",
                onValueChange = {},
                label = { Text("वांछित पद (Job Profile)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = "B.Sc (Computer Science) | CCC Certified | Hindi Typing 40 WPM",
                onValueChange = {},
                label = { Text("शैक्षिक योग्यता व कौशल") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = { viewModel.showPreview(PreviewDocumentType.BIO_DATA) },
                modifier = Modifier.fillMaxWidth().testTag("preview_bio_data_button"),
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
            ) {
                Icon(Icons.Default.Badge, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("बायो-डाटा का लाइव प्रिव्यू देखें व प्रिंट करें")
            }
        }
    }
}
