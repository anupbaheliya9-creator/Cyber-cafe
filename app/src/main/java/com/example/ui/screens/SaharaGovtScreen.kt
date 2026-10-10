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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.models.GovCertificateData
import com.example.domain.models.SaharaClaimData
import com.example.ui.CyberViewModel
import com.example.ui.components.PreviewDocumentType
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyDark
import com.example.ui.theme.SaffronPrimary

@Composable
fun SaharaGovtScreen(viewModel: CyberViewModel) {
    val saharaData by viewModel.saharaData.collectAsState()
    val certData by viewModel.certData.collectAsState()

    var selectedSubSection by remember { mutableIntStateOf(0) } // 0: Sahara CRCS, 1: e-District, 2: Student Exams

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Sub-Tab Switcher
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = selectedSubSection == 0,
                onClick = { selectedSubSection = 0 },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
            ) {
                Text("सहारा CRCS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            SegmentedButton(
                selected = selectedSubSection == 1,
                onClick = { selectedSubSection = 1 },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
            ) {
                Text("ई-डिस्ट्रिक्ट", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            SegmentedButton(
                selected = selectedSubSection == 2,
                onClick = { selectedSubSection = 2 },
                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
            ) {
                Text("विद्यार्थी पोर्टल", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (selectedSubSection) {
            0 -> SaharaSection(saharaData, viewModel)
            1 -> EDistrictSection(certData, viewModel)
            2 -> StudentExamsSection(viewModel)
        }
    }
}

@Composable
private fun SaharaSection(data: SaharaClaimData, viewModel: CyberViewModel) {
    var memberNum by remember { mutableStateOf(data.memberNumber) }
    var receiptNum by remember { mutableStateOf(data.receiptNumber) }
    var certNum by remember { mutableStateOf(data.certificateNumber) }
    var depositAmt by remember { mutableStateOf(data.depositAmount) }
    var claimantName by remember { mutableStateOf(data.claimantName) }
    var societyName by remember { mutableStateOf(data.societyName) }

    // Status Banner
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CRCS स्थिति: कमी पाई गई (Deficiency Notice)",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFDC2626),
                    fontSize = 13.sp
                )
            }
            Text(
                text = "कारण: ${data.deficiencyReason}",
                fontSize = 11.sp,
                color = Color(0xFF7F1D1D),
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "एआई समाधान: नया शपथ पत्र अपलोड करें एवं बायोमेट्रिक/OTP से प्रमाणित करें।",
                fontSize = 11.sp,
                color = Color(0xFF1E3A8A),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    Text(
        text = "दावा एवं सदस्यता विवरण (Claim & Member Data)",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = claimantName,
        onValueChange = { claimantName = it },
        label = { Text("दावेदार का नाम (Claimant Name)") },
        modifier = Modifier.fillMaxWidth().testTag("claimant_name_input"),
        singleLine = true
    )

    Spacer(modifier = Modifier.height(8.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = memberNum,
            onValueChange = { memberNum = it },
            label = { Text("सदस्यता सं० (Member No)") },
            modifier = Modifier.weight(1f).testTag("member_number_input"),
            singleLine = true
        )
        OutlinedTextField(
            value = receiptNum,
            onValueChange = { receiptNum = it },
            label = { Text("रसीद संख्या (Receipt No)") },
            modifier = Modifier.weight(1f).testTag("receipt_number_input"),
            singleLine = true
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = certNum,
            onValueChange = { certNum = it },
            label = { Text("प्रमाण पत्र सं० (Cert No)") },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        OutlinedTextField(
            value = depositAmt,
            onValueChange = { depositAmt = it },
            label = { Text("जमा राशि ₹ (Deposit Amt)") },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = societyName,
        onValueChange = { societyName = it },
        label = { Text("सोसायटी (Society Name)") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )

    Spacer(modifier = Modifier.height(16.dp))

    // 3 Gate Checkpoints Row
    Text(
        text = "हाइब्रिड ऑटोमेशन चेकप्वाइंट्स (Human Gates)",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = NavyDark
    )

    Spacer(modifier = Modifier.height(6.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = { viewModel.triggerOtpGate("XXXXXX9210", "सहारा OTP लॉगिन") },
            modifier = Modifier.weight(1f).testTag("sahara_otp_gate_button")
        ) {
            Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("1. OTP गेट", fontSize = 11.sp)
        }

        OutlinedButton(
            onClick = { viewModel.triggerCaptchaGate("सहारा सुरक्षा कैप्चा") },
            modifier = Modifier.weight(1f).testTag("sahara_captcha_gate_button")
        ) {
            Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("2. कैप्चा गेट", fontSize = 11.sp)
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Live Preview & Approval Button
    Button(
        onClick = {
            viewModel.updateSaharaData(
                data.copy(
                    claimantName = claimantName,
                    memberNumber = memberNum,
                    receiptNumber = receiptNum,
                    certificateNumber = certNum,
                    depositAmount = depositAmt,
                    societyName = societyName
                )
            )
            viewModel.showPreview(PreviewDocumentType.SAHARA_CLAIM)
        },
        modifier = Modifier.fillMaxWidth().testTag("sahara_live_preview_button"),
        colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
    ) {
        Icon(Icons.Default.Visibility, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("3. लाइव प्रिव्यू देखें एवं OK करें (Live Preview)")
    }
}

@Composable
private fun EDistrictSection(data: GovCertificateData, viewModel: CyberViewModel) {
    Text(
        text = "ई-डिस्ट्रिक्ट जन सेवा केंद्र (e-District Services)",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(8.dp))

    val certificates = listOf(
        "आय प्रमाण पत्र (Income Certificate)" to "₹ 48,000 वार्षिक आय",
        "जाति प्रमाण पत्र (Caste Certificate)" to "ओबीसी / एससी / एसटी सत्यापन",
        "निवास प्रमाण पत्र (Domicile/Niwas)" to "स्थायी निवास सत्यापन",
        "आयुष्मान गोल्डन कार्ड (Ayushman Card)" to "5 लाख का मुफ्त उपचार",
        "राशन कार्ड (Ration Card Slip)" to "पात्र गृहस्थी / अन्त्योदय",
        "खतौनी / भूलेख (Land Record Bhulekh)" to "खसरा / खतौनी नकल नकल"
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        certificates.forEach { (title, subtitle) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    FilledTonalButton(
                        onClick = {
                            viewModel.showPreview(PreviewDocumentType.GOV_CERTIFICATE)
                        }
                    ) {
                        Text("प्रिव्यू", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentExamsSection(viewModel: CyberViewModel) {
    Text(
        text = "विद्यार्थी एवं प्रतियोगी परीक्षा पोर्टल",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(8.dp))

    val examPortals = listOf(
        "SSC GD / CGL / CHSL 2026" to "आवेदन पत्र, एडमिट कार्ड एवं उत्तर कुंजी (Answer Key)",
        "उत्तर प्रदेश पुलिस भर्ती (UP Police)" to "कॉन्स्टेबल व एसआई फॉर्म स्टेटस व परीक्षा केंद्र",
        "रेलवे भर्ती बोर्ड (RRB ALP / Technician)" to "सीबीटी 1 प्रवेश पत्र व फोटो स्लिप",
        "UP छात्रवृत्ति (UP Scholarship Portal)" to "दशमोत्तर छात्रवृत्ति स्टेटस व आधार सीडिंग"
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        examPortals.forEach { (exam, detail) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = exam, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SaffronPrimary)
                    Text(text = detail, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                viewModel.voiceAgent.speak("$exam का प्रवेश पत्र डाउनलोड हो गया है। लाइव प्रिव्यू में जांचें।", listenAfterSpeaking = false)
                                viewModel.showPreview(PreviewDocumentType.SAHARA_CLAIM)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("एडमिट कार्ड", fontSize = 10.sp)
                        }
                        Button(
                            onClick = {
                                viewModel.voiceAgent.speak("$exam का फॉर्म प्रिंट तैयार है।", listenAfterSpeaking = true)
                                viewModel.showPreview(PreviewDocumentType.GOV_CERTIFICATE)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyDark)
                        ) {
                            Text("कन्फर्मेशन स्लिप", fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}
