package com.example.domain.models

// Top-level Navigation Screens
enum class CyberTab(val labelHindi: String, val labelEnglish: String) {
    VOICE_AGENT("वॉयस एजेंट", "Voice Agent"),
    SAHARA_GOVT("सहारा व सरकारी", "Sahara & Govt"),
    LEGAL_VAKIL("वकील व कोर्ट", "Legal Suite"),
    PHOTO_PVC("फोटो व PVC", "Photo & PVC"),
    WPS_OCR("WPS व OCR", "WPS & OCR"),
    BILLING_PRINT("बिलिंग व प्रिंट", "Billing & Print")
}

// Sahara CRCS Refund Data
data class SaharaClaimData(
    val memberNumber: String = "SHR-98421094",
    val receiptNumber: String = "RCP-7821443",
    val certificateNumber: String = "CR-00294182",
    val societyName: String = "Saharayn Universal Multipurpose Society Ltd.",
    val depositAmount: String = "45,000",
    val claimDate: String = "12/08/2023",
    val claimantName: String = "रामेश्वर प्रसाद वर्मा (Rameshwar Prasad)",
    val aadhaarLinkedPhone: String = "98765-XXXX1",
    val claimStatus: String = "Deficiency Communicated (कमी पाई गई)",
    val deficiencyReason: String = "Member signature mismatch with deposit slip; Aadhaar mismatch on certificate.",
    val resubmissionStep: String = "Step 2: Upload Re-Affidavit & Fresh Signature"
)

// Legal Vakil Suite Models
enum class LegalDraftType(val titleHindi: String, val titleEnglish: String) {
    VAKALATNAMA("वकालतनामा", "Vakalatnama"),
    AFFIDAVIT("शपथ पत्र (Affidavit)", "Affidavit"),
    LEGAL_NOTICE("कानूनी नोटिस (Legal Notice)", "Legal Notice"),
    BAIL_APPLICATION("जमानत अर्जी (Bail Sec 437/439)", "Bail Application"),
    RENT_AGREEMENT("किरायानामा (Rent Agreement)", "Rent Agreement")
}

data class LegalDraftData(
    val draftType: LegalDraftType = LegalDraftType.VAKALATNAMA,
    val courtName: String = "माननीय जिला एवं सत्र न्यायालय, लखनऊ (UP)",
    val caseNumber: String = "Civil Suit No. 142/2026",
    val petitioner: String = "राजेश कुमार सिंह (Rajesh Kumar Singh)",
    val respondent: String = "स्टेट बैंक ऑफ इंडिया व अन्य (SBI & Ors)",
    val advocateName: String = "एडवोकेट अनुराग श्रीवास्तव (Adv. Anurag Srivastava)",
    val enrollmentNumber: String = "UP/14820/2018",
    val affidavitSubject: String = "नाम सुधार एवं मूल निवास प्रमाण पत्र सत्यापन हेतु",
    val noticeDemand: String = "15 दिनों के भीतर बकाया किराया ₹45,000 का भुगतान अथवा बेदखली",
    val generatedLegalText: String = ""
)

// e-District & Student Service Models
data class GovCertificateData(
    val certType: String = "आय प्रमाण पत्र (Income Certificate)",
    val applicantName: String = "संजय कुमार (Sanjay Kumar)",
    val fatherName: String = "श्री बृजमोहन (Brij Mohan)",
    val annualIncome: String = "₹ 48,000/- (अड़तालीस हजार रुपये वार्षिक)",
    val address: String = "ग्राम- रसूलपुर, तहसील- सदर, जनपद- वाराणसी, उत्तर प्रदेश",
    val applicationNo: String = "UP-2026-INC-893021",
    val issueDate: String = "09/10/2026",
    val verificationOfficer: String = "तहसीलदार सदर, वाराणसी"
)

// Photo & PVC Studio Models
data class PhotoStudioConfig(
    val photoCount: Int = 8, // 6, 8, 12 photos on 4x6 sheet
    val backgroundColorName: String = "Studio White",
    val studentName: String = "AMIT KUMAR",
    val dateOnPhoto: String = "09/10/2026",
    val targetFileSizeKb: Int = 35, // 20-50 KB for photo
    val isSignatureMode: Boolean = false,
    val signatureTargetKb: Int = 15 // 10-20 KB for sign
)

// Active Permission Gate Checkpoints
enum class GateType {
    NONE,
    OTP_VERIFICATION,
    CAPTCHA_CHALLENGE,
    LIVE_PREVIEW_APPROVAL
}

data class GateState(
    val activeGate: GateType = GateType.NONE,
    val gateTitle: String = "",
    val gateDescription: String = "",
    val otpTargetPhone: String = "XXXXXX9210",
    val captchaText: String = "7K9MQ",
    val pendingActionLabel: String = ""
)
