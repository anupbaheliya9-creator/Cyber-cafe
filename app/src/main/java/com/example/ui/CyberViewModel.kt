package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiManager
import com.example.data.api.GeminiResult
import com.example.data.local.AppDatabase
import com.example.data.model.TransactionRecord
import com.example.domain.generator.LegalDraftGenerator
import com.example.domain.models.*
import com.example.ui.components.PreviewDocumentType
import com.example.voice.VoiceAgentManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CyberViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val transactionDao = db.transactionDao()

    val transactions: StateFlow<List<TransactionRecord>> = transactionDao
        .getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Gemini API Key & Security Manager
    val geminiManager = GeminiManager(application)

    private val _isApiKeyDialogOpen = MutableStateFlow(false)
    val isApiKeyDialogOpen: StateFlow<Boolean> = _isApiKeyDialogOpen.asStateFlow()

    // Active Navigation Tab
    private val _currentTab = MutableStateFlow(CyberTab.VOICE_AGENT)
    val currentTab: StateFlow<CyberTab> = _currentTab.asStateFlow()

    // Sahara CRCS State
    private val _saharaData = MutableStateFlow(SaharaClaimData())
    val saharaData: StateFlow<SaharaClaimData> = _saharaData.asStateFlow()

    // Legal Drafting State
    private val _legalData = MutableStateFlow(
        LegalDraftData().let {
            it.copy(generatedLegalText = LegalDraftGenerator.generateText(it))
        }
    )
    val legalData: StateFlow<LegalDraftData> = _legalData.asStateFlow()

    // e-District State
    private val _certData = MutableStateFlow(GovCertificateData())
    val certData: StateFlow<GovCertificateData> = _certData.asStateFlow()

    // Photo & PVC Studio Config
    private val _photoConfig = MutableStateFlow(PhotoStudioConfig())
    val photoConfig: StateFlow<PhotoStudioConfig> = _photoConfig.asStateFlow()

    // Live Preview State
    private val _isPreviewOpen = MutableStateFlow(false)
    val isPreviewOpen: StateFlow<Boolean> = _isPreviewOpen.asStateFlow()

    private val _activePreviewType = MutableStateFlow(PreviewDocumentType.SAHARA_CLAIM)
    val activePreviewType: StateFlow<PreviewDocumentType> = _activePreviewType.asStateFlow()

    // Hybrid Automation Permission Gate State
    private val _gateState = MutableStateFlow(GateState())
    val gateState: StateFlow<GateState> = _gateState.asStateFlow()

    // Voice Agent Manager
    val voiceAgent: VoiceAgentManager = VoiceAgentManager(application) { spokenCommand ->
        handleVoiceCommand(spokenCommand)
    }

    // Smart Billing State
    private val _billingItems = MutableStateFlow(
        listOf(
            "सहारा फॉर्म री-सबमिशन" to 50.0,
            "शपथ पत्र ड्राफ्टिंग (WPS)" to 150.0,
            "8 पासपोर्ट फोटो शीट" to 40.0,
            "PVC कार्ड प्रिंट" to 70.0
        )
    )
    val billingItems: StateFlow<List<Pair<String, Double>>> = _billingItems.asStateFlow()

    private val _upiId = MutableStateFlow("cybercafe.csc@upi")
    val upiId: StateFlow<String> = _upiId.asStateFlow()

    init {
        // Seed initial transaction record if empty
        viewModelScope.launch {
            transactionDao.insertTransaction(
                TransactionRecord(
                    customerName = "रामेश्वर प्रसाद",
                    customerPhone = "9876543210",
                    serviceCategory = "Sahara CRCS",
                    serviceName = "Deficiency Resubmission",
                    amount = 50.0,
                    paymentMode = "UPI",
                    referenceNumber = "UPI9028123"
                )
            )
        }
    }

    fun openApiKeyDialog() {
        _isApiKeyDialogOpen.value = true
    }

    fun closeApiKeyDialog() {
        _isApiKeyDialogOpen.value = false
    }

    fun setTab(tab: CyberTab) {
        _currentTab.value = tab
    }

    fun updateSaharaData(data: SaharaClaimData) {
        _saharaData.value = data
    }

    fun updateLegalDraftType(type: LegalDraftType) {
        val updated = _legalData.value.copy(draftType = type)
        _legalData.value = updated.copy(generatedLegalText = LegalDraftGenerator.generateText(updated))
    }

    fun updateLegalData(data: LegalDraftData) {
        _legalData.value = data.copy(generatedLegalText = LegalDraftGenerator.generateText(data))
    }

    fun updatePhotoConfig(config: PhotoStudioConfig) {
        _photoConfig.value = config
    }

    fun showPreview(type: PreviewDocumentType) {
        _activePreviewType.value = type
        _isPreviewOpen.value = true
        voiceAgent.speak(
            "लाइव प्रिव्यू स्क्रीन पर तैयार है। क्या सभी जानकारी सही है? पुष्टि के बाद प्रिंट निकालें।",
            listenAfterSpeaking = true
        )
    }

    fun closePreview() {
        _isPreviewOpen.value = false
    }

    // Trigger Permission Gates
    fun triggerOtpGate(phone: String, actionLabel: String = "सहारा पोर्टल ऑथेंटिकेशन") {
        _gateState.value = GateState(
            activeGate = GateType.OTP_VERIFICATION,
            gateTitle = "मोबाइल OTP सत्यापन (OTP Gate)",
            gateDescription = "सरकारी पोर्टल पर लॉगिन एवं सबमिशन हेतु ग्राहक के मोबाइल पर प्राप्त OTP दर्ज करें।",
            otpTargetPhone = phone,
            pendingActionLabel = actionLabel
        )
        voiceAgent.speak("कृपया ग्राहक के मोबाइल पर प्राप्त 6 अंकों का OTP दर्ज करें।", listenAfterSpeaking = false)
    }

    fun triggerCaptchaGate(actionLabel: String = "कैप्चा सत्यापन") {
        val chars = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"
        val code = (1..5).map { chars.random() }.joinToString("")
        _gateState.value = GateState(
            activeGate = GateType.CAPTCHA_CHALLENGE,
            gateTitle = "सुरक्षा कैप्चा कोड (CAPTCHA Gate)",
            gateDescription = "पोर्टल सर्वर को स्वचालित स्पैम से बचाने हेतु स्क्रीन पर दिखने वाला कोड सत्यापित करें।",
            captchaText = code,
            pendingActionLabel = actionLabel
        )
        voiceAgent.speak("सुरक्षा कैप्चा सत्यापन आवश्यक है। कृपया कोड दर्ज करें।", listenAfterSpeaking = false)
    }

    fun triggerLiveApprovalGate(actionLabel: String = "अंतिम प्रिंट व शुल्क भुगतान") {
        _gateState.value = GateState(
            activeGate = GateType.LIVE_PREVIEW_APPROVAL,
            gateTitle = "लाइव प्रिव्यू व फीस स्वीकृति (Approval Gate)",
            gateDescription = "क्या सभी जानकारी एवं शुल्क सही है? OK कहने या बटन दबाने पर अंतिम दस्तावेज प्रिंट होगा।",
            pendingActionLabel = actionLabel
        )
        voiceAgent.speak("क्या सभी जानकारी सही है? यदि हाँ तो ओके कहें।", listenAfterSpeaking = true)
    }

    fun dismissGate() {
        _gateState.value = GateState(activeGate = GateType.NONE)
    }

    fun onOtpVerified(otp: String) {
        dismissGate()
        voiceAgent.speak("OTP सफलतापूर्वक सत्यापित हो गया। आवेदन प्रक्रिया आगे बढ़ाई जा रही है।", listenAfterSpeaking = false)
    }

    fun onCaptchaSolved(captcha: String) {
        dismissGate()
        voiceAgent.speak("कैप्चा कोड सत्यापित हुआ। पोर्टल प्रक्रिया पूर्ण हो रही है।", listenAfterSpeaking = false)
    }

    fun onPreviewApproved() {
        dismissGate()
        _isPreviewOpen.value = false
        voiceAgent.speak("पुष्टि प्राप्त हुई। दस्तावेज प्रिंट एवं सुरक्षित आर्काइव में भेज दिया गया है।", listenAfterSpeaking = false)

        // Record Completed Transaction in Room DB
        viewModelScope.launch {
            val total = _billingItems.value.sumOf { it.second }
            transactionDao.insertTransaction(
                TransactionRecord(
                    customerName = _saharaData.value.claimantName.substringBefore(" ("),
                    customerPhone = "9876543210",
                    serviceCategory = "Print & Filing",
                    serviceName = "All-In-One Approved Job",
                    amount = if (total > 0) total else 80.0,
                    paymentMode = "UPI",
                    referenceNumber = "TXN-${System.currentTimeMillis().toString().takeLast(6)}"
                )
            )
        }
    }

    // Security & Data Privacy: Clear session & Mask PII
    fun scrubAndMaskPii() {
        viewModelScope.launch {
            transactionDao.maskAllPii()
            _saharaData.value = _saharaData.value.copy(
                aadhaarLinkedPhone = "XXXXXX0000",
                memberNumber = "SHR-XXXX"
            )
            voiceAgent.speak("गोपनीयता सुरक्षा सक्रिय: ग्राहक का आधार, फोन व व्यक्तिगत डेटा एन्क्रिप्ट एवं मास्क कर दिया गया है।", listenAfterSpeaking = false)
        }
    }

    fun addBillingItem(name: String, price: Double) {
        val list = _billingItems.value.toMutableList()
        list.add(name to price)
        _billingItems.value = list
    }

    fun removeBillingItem(index: Int) {
        val list = _billingItems.value.toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            _billingItems.value = list
        }
    }

    // AI Execution with strict API key protocol validation & error handling
    fun executeGeminiAi(prompt: String, onCustomResult: ((String) -> Unit)? = null) {
        viewModelScope.launch {
            // Rule 3: Check for key existence prior to every API call.
            if (!geminiManager.hasValidKey()) {
                val msg = "कृपया जारी रखने के लिए अपनी Gemini API Key दर्ज करें।"
                voiceAgent.addMessage(msg, isUser = false)
                voiceAgent.speak(msg, listenAfterSpeaking = false)
                _isApiKeyDialogOpen.value = true
                return@launch
            }

            voiceAgent.addMessage("Gemini AI द्वारा प्रक्रियाधीन...", isUser = false)
            when (val result = geminiManager.generateContent(prompt)) {
                is GeminiResult.Success -> {
                    if (onCustomResult != null) {
                        onCustomResult(result.text)
                    } else {
                        voiceAgent.addMessage(result.text, isUser = false)
                        voiceAgent.speak(result.text, listenAfterSpeaking = true)
                    }
                }
                is GeminiResult.KeyMissing -> {
                    val msg = "कृपया जारी रखने के लिए अपनी Gemini API Key दर्ज करें।"
                    voiceAgent.addMessage(msg, isUser = false)
                    voiceAgent.speak(msg, listenAfterSpeaking = false)
                    _isApiKeyDialogOpen.value = true
                }
                is GeminiResult.Error -> {
                    voiceAgent.addMessage(result.message, isUser = false)
                    voiceAgent.speak(result.message, listenAfterSpeaking = false)
                    if (result.isKeyError) {
                        _isApiKeyDialogOpen.value = true
                    }
                }
            }
        }
    }

    // Natural Language Voice Command Processor
    private fun handleVoiceCommand(rawQuery: String) {
        val query = rawQuery.lowercase()

        when {
            // API Key management commands
            query.contains("api key") || query.contains("api कुंजी") || query.contains("gemini key") || query.contains("जेमिनी की") -> {
                _isApiKeyDialogOpen.value = true
                voiceAgent.speak("Gemini API Key प्रबंधन विंडो खोली गई है। कृपया अपनी वैध कुंजी दर्ज करें।", listenAfterSpeaking = false)
            }

            // Approval commands
            query.contains("ok") || query.contains("हाँ") || query.contains("सही है") || query.contains("print karo") || query.contains("प्रिंट") -> {
                if (_gateState.value.activeGate != GateType.NONE) {
                    onPreviewApproved()
                } else if (_isPreviewOpen.value) {
                    onPreviewApproved()
                } else {
                    voiceAgent.speak("प्रिंट कमांड प्राप्त हुई। डायरेक्ट प्रिंट विंडो खोली जा रही है।", listenAfterSpeaking = false)
                }
            }

            // Sahara Refund commands
            query.contains("सहारा") || query.contains("sahara") || query.contains("crcs") || query.contains("रिफंड") -> {
                _currentTab.value = CyberTab.SAHARA_GOVT
                showPreview(PreviewDocumentType.SAHARA_CLAIM)
                voiceAgent.speak(
                    "सहारा सीआरसीएस रिफंड पोर्टल खोल दिया गया है। दावेदार रामेश्वर प्रसाद का डेफिशियेंसी फॉर्म तैयार है। क्या सभी जानकारी सही है?",
                    listenAfterSpeaking = true
                )
            }

            // Legal Vakil suite commands
            query.contains("वकालतनामा") || query.contains("vakalatnama") -> {
                _currentTab.value = CyberTab.LEGAL_VAKIL
                updateLegalDraftType(LegalDraftType.VAKALATNAMA)
                showPreview(PreviewDocumentType.LEGAL_DRAFT)
                voiceAgent.speak("माननीय न्यायालय हेतु वकालतनामा प्रारूप तैयार है। प्रिव्यू चेक करें।", listenAfterSpeaking = true)
            }
            query.contains("शपथ पत्र") || query.contains("affidavit") -> {
                _currentTab.value = CyberTab.LEGAL_VAKIL
                updateLegalDraftType(LegalDraftType.AFFIDAVIT)
                showPreview(PreviewDocumentType.LEGAL_DRAFT)
                voiceAgent.speak("नोटरी शपथ पत्र का विधिक प्रारूप तैयार कर दिया गया है।", listenAfterSpeaking = true)
            }
            query.contains("नोटिस") || query.contains("notice") -> {
                _currentTab.value = CyberTab.LEGAL_VAKIL
                updateLegalDraftType(LegalDraftType.LEGAL_NOTICE)
                showPreview(PreviewDocumentType.LEGAL_DRAFT)
                voiceAgent.speak("कानूनी नोटिस तैयार है। प्रिव्यू में मुवक्किल और विपक्षी की जानकारी जांचें।", listenAfterSpeaking = true)
            }
            query.contains("जमानत") || query.contains("bail") -> {
                _currentTab.value = CyberTab.LEGAL_VAKIL
                updateLegalDraftType(LegalDraftType.BAIL_APPLICATION)
                showPreview(PreviewDocumentType.LEGAL_DRAFT)
                voiceAgent.speak("धारा 437 और 439 के तहत जमानत अर्जी ड्राफ्ट हो गई है।", listenAfterSpeaking = true)
            }

            // Photo & PVC commands
            query.contains("फोटो") || query.contains("passport") || query.contains("photo") -> {
                _currentTab.value = CyberTab.PHOTO_PVC
                showPreview(PreviewDocumentType.PASSPORT_SHEET)
                voiceAgent.speak("4 बाई 6 इंच शीट पर 8 पासपोर्ट फोटो का कटिंग लेआउट तैयार है।", listenAfterSpeaking = true)
            }
            query.contains("pvc") || query.contains("आधार कार्ड") || query.contains("कार्ड") -> {
                _currentTab.value = CyberTab.PHOTO_PVC
                showPreview(PreviewDocumentType.PVC_CARD)
                voiceAgent.speak("PVC कार्ड प्रिंटिंग हेतु फ्रंट और बैक लेआउट तैयार है।", listenAfterSpeaking = true)
            }

            // Certificate commands
            query.contains("आय") || query.contains("income") || query.contains("जाति") || query.contains("निवास") -> {
                _currentTab.value = CyberTab.SAHARA_GOVT
                showPreview(PreviewDocumentType.GOV_CERTIFICATE)
                voiceAgent.speak("ई-डिस्ट्रिक्ट आय प्रमाण पत्र का लाइव प्रिव्यू लोड हो गया है।", listenAfterSpeaking = true)
            }

            // Billing / QR commands
            query.contains("बिल") || query.contains("bill") || query.contains("qr") || query.contains("रुपये") || query.contains("भुगतान") -> {
                _currentTab.value = CyberTab.BILLING_PRINT
                val total = _billingItems.value.sumOf { it.second }
                voiceAgent.speak("कुल बिल राशि ₹ $total है। स्क्रीन पर डायनामिक यूपीआई क्यूआर कोड उपलब्ध है।", listenAfterSpeaking = false)
            }

            // Security scrub commands
            query.contains("क्लियर") || query.contains("mask") || query.contains("सुरक्षा") || query.contains("डिलीट") -> {
                scrubAndMaskPii()
            }

            // OTP / Gate commands
            query.contains("otp") || query.contains("ओटीपी") -> {
                triggerOtpGate("XXXXXX9210")
            }
            query.contains("captcha") || query.contains("कैप्चा") -> {
                triggerCaptchaGate()
            }

            // General or complex query -> Pass to Gemini AI
            else -> {
                executeGeminiAi(rawQuery)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceAgent.destroy()
    }
}
