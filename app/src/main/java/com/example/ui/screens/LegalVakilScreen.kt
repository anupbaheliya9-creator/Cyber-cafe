package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.models.LegalDraftData
import com.example.domain.models.LegalDraftType
import com.example.ui.CyberViewModel
import com.example.ui.components.PreviewDocumentType
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyDark
import com.example.ui.theme.SaffronPrimary

@Composable
fun LegalVakilScreen(viewModel: CyberViewModel) {
    val legalData by viewModel.legalData.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Drafting, 1: e-Courts, 2: Police & Challan

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Tab Selector
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
            ) {
                Text("विधिक प्रारूप (Drafts)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            SegmentedButton(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
            ) {
                Text("ई-कोर्ट ट्रैकिंग", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            SegmentedButton(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
            ) {
                Text("ई-FIR व चालान", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (selectedTab) {
            0 -> LegalDraftingTab(legalData, viewModel)
            1 -> ECourtsTrackingTab()
            2 -> PoliceAndChallanTab()
        }
    }
}

@Composable
private fun LegalDraftingTab(data: LegalDraftData, viewModel: CyberViewModel) {
    var petitioner by remember { mutableStateOf(data.petitioner) }
    var respondent by remember { mutableStateOf(data.respondent) }
    var advocateName by remember { mutableStateOf(data.advocateName) }
    var enrollmentNum by remember { mutableStateOf(data.enrollmentNumber) }
    var courtName by remember { mutableStateOf(data.courtName) }
    var caseNum by remember { mutableStateOf(data.caseNumber) }

    Text(
        text = "प्रारूप का प्रकार चुनें (Select Legal Format)",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Legal Document Types Chips
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        LegalDraftType.values().forEach { type ->
            FilterChip(
                selected = data.draftType == type,
                onClick = { viewModel.updateLegalDraftType(type) },
                label = {
                    Text(
                        text = "${type.titleHindi} (${type.titleEnglish})",
                        fontSize = 12.sp,
                        fontWeight = if (data.draftType == type) FontWeight.Bold else FontWeight.Normal
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (data.draftType == type) Icons.Default.Gavel else Icons.Default.Description,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (data.draftType == type) SaffronPrimary else Color.Gray
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    Text(
        text = "विधिक पक्षकार व न्यायालय विवरण (Litigant & Court)",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(6.dp))

    OutlinedTextField(
        value = petitioner,
        onValueChange = { petitioner = it },
        label = { Text("वादी / प्रार्थी / प्रथम पक्ष (Petitioner/Client)") },
        modifier = Modifier.fillMaxWidth().testTag("petitioner_input"),
        singleLine = true
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = respondent,
        onValueChange = { respondent = it },
        label = { Text("प्रतिवादी / विपक्षी / द्वितीय पक्ष (Respondent)") },
        modifier = Modifier.fillMaxWidth().testTag("respondent_input"),
        singleLine = true
    )

    Spacer(modifier = Modifier.height(8.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = advocateName,
            onValueChange = { advocateName = it },
            label = { Text("अधिवक्ता नाम") },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        OutlinedTextField(
            value = enrollmentNum,
            onValueChange = { enrollmentNum = it },
            label = { Text("बार पंजीयन सं०") },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = courtName,
            onValueChange = { courtName = it },
            label = { Text("न्यायालय") },
            modifier = Modifier.weight(1.2f),
            singleLine = true
        )
        OutlinedTextField(
            value = caseNum,
            onValueChange = { caseNum = it },
            label = { Text("वाद सं०") },
            modifier = Modifier.weight(0.8f),
            singleLine = true
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Generate & Live Preview Button
    Button(
        onClick = {
            viewModel.updateLegalData(
                data.copy(
                    petitioner = petitioner,
                    respondent = respondent,
                    advocateName = advocateName,
                    enrollmentNumber = enrollmentNum,
                    courtName = courtName,
                    caseNumber = caseNum
                )
            )
            viewModel.showPreview(PreviewDocumentType.LEGAL_DRAFT)
        },
        modifier = Modifier.fillMaxWidth().testTag("generate_legal_draft_button"),
        colors = ButtonDefaults.buttonColors(containerColor = NavyDark)
    ) {
        Icon(Icons.Default.Gavel, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("विधिक ड्राफ्ट बनाएं व लाइव प्रिव्यू देखें (Create Draft)")
    }
}

@Composable
private fun ECourtsTrackingTab() {
    var cnrNumber by remember { mutableStateOf("UPHC01-098214-2024") }
    var isSearched by remember { mutableStateOf(true) }

    Column {
        Text(
            text = "ई-कोर्ट वाद ट्रैकिंग (e-Courts Case Status)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "CNR संख्या या मुकद्दमा नंबर से अगली सुनवाई व आदेश देखें",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = cnrNumber,
                onValueChange = { cnrNumber = it },
                label = { Text("16-अंकीय CNR संख्या") },
                modifier = Modifier.weight(1f).testTag("cnr_number_input"),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { isSearched = true },
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
            ) {
                Icon(Icons.Default.Search, contentDescription = null)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (isSearched) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "वाद संख्या: Civil Suit 142/2026", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)
                        Surface(color = EmeraldSuccess.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                            Text(
                                text = "Next Date: 28/10/2026",
                                color = EmeraldSuccess,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFE2E8F0))

                    Text(text = "न्यायालय: माननीय जिला एवं सत्र न्यायाधीश, कोर्ट सं० 4", fontSize = 11.sp, color = Color(0xFF334155))
                    Text(text = "वादी: राजेश कुमार बनाम स्टेट बैंक", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                    Text(text = "वर्तमान चरण (Stage): बहस साक्ष्य (Hearing on Evidence)", fontSize = 11.sp, color = SaffronPrimary, fontWeight = FontWeight.Bold)
                    Text(text = "कॉज़ लिस्ट (Cause List): Item No. 18 (Morning Session)", fontSize = 11.sp, color = Color.DarkGray)

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { /* Print judicial order summary */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("न्यायिक आदेश (Order Sheet) डाउनलोड करें", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun PoliceAndChallanTab() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "पुलिस ई-FIR एवं ट्रैफिक चालान सेवाएं",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "ऑनलाइन e-FIR एवं खोया-पाया (Lost Article NCR)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)
                Text(text = "मोबाइल, आधार, पैन या चेक बुक गुम होने पर तत्काल डिजिटल पुलिस रसीद दर्ज करें।", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { /* Generate NCR */ },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("डिजिटल NCR रसीद जनरेट करें", fontSize = 11.sp)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "ट्रैफिक ई-चालान जांच एवं भुगतान (e-Challan)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)
                Text(text = "गाड़ी नंबर (जैसे UP32 AB 1234) से लंबित चालान देखें एवं वर्चुअल कोर्ट में निस्तारण करें।", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { /* Check challan */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("लंबित चालान चेक करें", fontSize = 11.sp)
                }
            }
        }
    }
}
