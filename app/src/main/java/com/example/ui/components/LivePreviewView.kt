package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.models.GovCertificateData
import com.example.domain.models.LegalDraftData
import com.example.domain.models.PhotoStudioConfig
import com.example.domain.models.SaharaClaimData
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyDark
import com.example.ui.theme.SaffronPrimary

enum class PreviewDocumentType {
    SAHARA_CLAIM,
    LEGAL_DRAFT,
    GOV_CERTIFICATE,
    PASSPORT_SHEET,
    PVC_CARD,
    THERMAL_RECEIPT,
    BIO_DATA
}

@Composable
fun LivePreviewContainer(
    previewType: PreviewDocumentType,
    saharaData: SaharaClaimData,
    legalData: LegalDraftData,
    certData: GovCertificateData,
    photoConfig: PhotoStudioConfig,
    onApproveVoiceOrButton: () -> Unit,
    onDirectPrint: () -> Unit,
    onClose: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .testTag("live_preview_container"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            // Live Preview Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(EmeraldSuccess)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE PRINT PREVIEW (लाइव प्रिव्यू)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldSuccess
                    )
                }

                IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close Preview")
                }
            }

            Text(
                text = "एआई संदेश: क्या सभी जानकारी सही है? पुष्टि के बाद प्रिंट निकालें।",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )

            // Preview Scroll Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .heightIn(max = 420.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF1F5F9))
                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    when (previewType) {
                        PreviewDocumentType.SAHARA_CLAIM -> SaharaClaimSheetPreview(saharaData)
                        PreviewDocumentType.LEGAL_DRAFT -> LegalDraftSheetPreview(legalData)
                        PreviewDocumentType.GOV_CERTIFICATE -> GovCertificateSheetPreview(certData)
                        PreviewDocumentType.PASSPORT_SHEET -> PassportPhotoGridPreview(photoConfig)
                        PreviewDocumentType.PVC_CARD -> PvcCardFrontBackPreview()
                        PreviewDocumentType.THERMAL_RECEIPT -> ThermalReceiptPreview()
                        PreviewDocumentType.BIO_DATA -> BioDataSheetPreview()
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onApproveVoiceOrButton,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("preview_approve_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("OK - सब सही है")
                }

                FilledTonalButton(
                    onClick = onDirectPrint,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("preview_print_button")
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("डायरेक्ट प्रिंट")
                }
            }
        }
    }
}

// 1. Sahara CRCS Claim Sheet Preview
@Composable
fun SaharaClaimSheetPreview(data: SaharaClaimData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Government / Portal Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A))
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "भारत सरकार | MINISTRY OF COOPERATION",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "CRCS SAHARA REFUND PORTAL (सहारा रिफंड पोर्टल)",
                    color = SaffronPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "DEFICIENCY RESUBMISSION & CLAIM CONFIRMATION SLIP",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "दावा सं०: ${data.receiptNumber}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                Text(text = "दिनांक: ${data.claimDate}", fontSize = 12.sp, color = Color.DarkGray)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color.LightGray)

            DetailRow("दावेदार का नाम:", data.claimantName)
            DetailRow("सोसायटी का नाम:", data.societyName)
            DetailRow("सदस्यता संख्या:", data.memberNumber)
            DetailRow("प्रमाण पत्र संख्या:", data.certificateNumber)
            DetailRow("जमा धनराशि:", "₹ ${data.depositAmount}/-")
            DetailRow("आधार लिंक फोन:", data.aadhaarLinkedPhone)

            Spacer(modifier = Modifier.height(8.dp))

            // Deficiency Status Box
            Surface(
                color = Color(0xFFFEF2F2),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(6.dp))
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "पोर्टल स्थिति: ${data.claimStatus}",
                        color = Color(0xFFDC2626),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "कमी का कारण: ${data.deficiencyReason}",
                        color = Color(0xFF7F1D1D),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Text(
                        text = "पुनः आवेदन: ${data.resubmissionStep}",
                        color = Color(0xFF1E3A8A),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "[डिजिटल सत्यापन मुहर]\nCRCS Authorized Portal\nSignature Verified",
                    fontSize = 9.sp,
                    color = Color.Gray
                )
                Text(
                    text = "हस्ताक्षर दावेदार\n(Claimant Signature)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}

// 2. Legal Draft Sheet Preview
@Composable
fun LegalDraftSheetPreview(data: LegalDraftData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFCFDFE)),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Court Stamp Paper Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, Color(0xFF94A3B8))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "COURT OF JUDICATURE | विधिक प्रारूप",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        color = NavyDark
                    )
                    Text(
                        text = "₹100 NON-JUDICIAL E-STAMP & COURT DRAFT",
                        fontSize = 10.sp,
                        color = Color(0xFF475569)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = data.generatedLegalText.ifBlank {
                    "प्रारूप: ${data.draftType.titleHindi}\nन्यायालय: ${data.courtName}\nवादी: ${data.petitioner}\nप्रतिवादी: ${data.respondent}\nअधिवक्ता: ${data.advocateName}"
                },
                fontSize = 11.sp,
                fontFamily = FontFamily.Serif,
                lineHeight = 16.sp,
                color = Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Seal of Notary / Court", fontSize = 9.sp, color = Color.Gray)
                Text(text = "हस्ताक्षर अधिवक्ता\n(${data.advocateName})", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
    }
}

// 3. e-District Gov Certificate Preview
@Composable
fun GovCertificateSheetPreview(data: GovCertificateData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "उत्तर प्रदेश शासन | GOVT OF UTTAR PRADESH", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFB45309))
                Text(text = "ई-डिस्ट्रिक्ट पोर्टल (e-District Services)", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = NavyDark)
                Text(text = data.certType, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E3A8A), modifier = Modifier.padding(vertical = 4.dp))
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color.LightGray)

            DetailRow("प्रमाण पत्र क्रमांक:", data.applicationNo)
            DetailRow("आवेदक का नाम:", data.applicantName)
            DetailRow("पिता/पति का नाम:", data.fatherName)
            DetailRow("वार्षिक आय:", data.annualIncome)
            DetailRow("निवास स्थान:", data.address)
            DetailRow("जारी दिनांक:", data.issueDate)
            DetailRow("सत्यापन अधिकारी:", data.verificationOfficer)

            Spacer(modifier = Modifier.height(10.dp))

            Surface(color = Color(0xFFECFDF5), shape = RoundedCornerShape(4.dp), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "डिजिटल रूप से हस्ताक्षरित प्रमाण पत्र। किसी भौतिक हस्ताक्षर की आवश्यकता नहीं है।",
                        fontSize = 10.sp,
                        color = Color(0xFF065F46)
                    )
                }
            }
        }
    }
}

// 4. 4x6 Passport Photo Sheet Preview
@Composable
fun PassportPhotoGridPreview(config: PhotoStudioConfig) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "4x6 INCH PHOTO PRINTING SHEET (${config.photoCount} PHOTOS)",
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = NavyDark
        )
        Text(
            text = "काटने हेतु डॉटेड लाइन दी गई है | बैकग्राउंड: ${config.backgroundColorName}",
            fontSize = 10.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Photo Grid (e.g. 2 columns x 4 rows = 8 photos)
        val rows = if (config.photoCount == 6) 3 else if (config.photoCount == 12) 4 else 4
        val cols = if (config.photoCount == 12) 3 else 2

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF94A3B8))
                .padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    repeat(cols) {
                        PassportPhotoItem(
                            modifier = Modifier.weight(1f),
                            name = config.studentName,
                            date = config.dateOnPhoto,
                            bg = if (config.backgroundColorName.contains("Blue")) Color(0xFF3B82F6) else Color(0xFFE2E8F0)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PassportPhotoItem(modifier: Modifier, name: String, date: String, bg: Color) {
    Box(
        modifier = modifier
            .aspectRatio(0.75f)
            .clip(RoundedCornerShape(2.dp))
            .background(bg)
            .border(1.dp, Color(0xFFCBD5E1)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF475569), modifier = Modifier.size(36.dp))
            Spacer(modifier = Modifier.weight(1f))

            // Name & Date on Photo footer
            Surface(
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(2.dp)) {
                    Text(text = name, fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text(text = "DOB: $date", fontSize = 6.sp, color = Color.DarkGray)
                }
            }
        }
    }
}

// 5. PVC Card Layout Preview
@Composable
fun PvcCardFrontBackPreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(12.dp)
    ) {
        Text(
            text = "PVC PLASTIC CARD PRINTING LAYOUT (CR-80 STANDARD)",
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = NavyDark,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Front Side
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.58f),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "GOVERNMENT OF INDIA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A))
                    Text(text = "भारत सरकार", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A))
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(50.dp, 60.dp)
                            .background(Color(0xFFCBD5E1), RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AccountBox, contentDescription = null, tint = Color(0xFF475569), modifier = Modifier.size(32.dp))
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(text = "नाम: अमित कुमार / AMIT KUMAR", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(text = "जन्म तिथि / DOB: 14/05/1998", fontSize = 9.sp, color = Color.DarkGray)
                        Text(text = "लिंग / Gender: MALE / पुरुष", fontSize = 9.sp, color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "XXXX XXXX 8912", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp, color = Color(0xFFB45309))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Back Side
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.58f),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(text = "Address: H.No 18, Gandhi Nagar, Lucknow, UP - 226001", fontSize = 9.sp, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(45.dp)
                            .background(Color(0xFF0F172A), RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.QrCode2, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                    }
                    Text(text = "1947 (Toll Free)\nhelp@uidai.gov.in", fontSize = 8.sp, color = Color.Gray, textAlign = TextAlign.End)
                }
            }
        }
    }
}

// 6. Thermal Receipt Preview
@Composable
fun ThermalReceiptPreview() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEFCE8)),
        shape = RoundedCornerShape(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "*** DIGITAL CYBER CAFE & VAKIL SUITE ***", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Black)
            Text(text = "MAIN ROAD, NEAR CIVIL COURT", fontSize = 9.sp, color = Color.DarkGray)
            Text(text = "RECEIPT NO: CYB-2026-9812", fontSize = 9.sp, color = Color.DarkGray)

            DottedLine()

            DetailRow("1. सहारा फॉर्म री-सबमिशन", "₹ 50.00")
            DetailRow("2. शपथ पत्र ड्राफ्टिंग (WPS)", "₹ 150.00")
            DetailRow("3. 8 पासपोर्ट फोटो शीट", "₹ 40.00")
            DetailRow("4. लेमिनेशन व PVC कार्ड", "₹ 70.00")

            DottedLine()

            DetailRow("कुल देय धनराशि (TOTAL):", "₹ 310.00", isBold = true)
            DetailRow("भुगतान माध्यम (Mode):", "UPI (PhonePe/GPay)")

            DottedLine()

            Text(text = "धन्यवाद! पुनः पधारें।", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Color.Black)
        }
    }
}

// 7. Bio Data Preview
@Composable
fun BioDataSheetPreview() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "CURRICULUM VITAE / बायो-डाटा",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                color = NavyDark,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color.LightGray)

            DetailRow("पूरा नाम:", "रवि कुमार (Ravi Kumar)")
            DetailRow("पद / Job Profile:", "Data Entry Operator / Computer Assistant")
            DetailRow("सम्पर्क संख्या:", "+91 98391-XXXXX")
            DetailRow("ईमेल:", "ravi.kumar.job2026@gmail.com")
            DetailRow("योग्यता:", "B.Sc (Computer Science) | CCC Certified")
            DetailRow("अनुभव:", "2 Years at Cyber Cafe & Legal Documentation")
            DetailRow("कौशल:", "Hindi/English Typing (40 WPM), MS Office, Photoshop")
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = if (isBold) Color.Black else Color(0xFF334155), fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
        Text(text = value, fontSize = 11.sp, color = Color.Black, fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.SemiBold)
    }
}

@Composable
private fun DottedLine() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .padding(vertical = 4.dp)
    ) {
        drawLine(
            color = Color.Gray,
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )
    }
}
