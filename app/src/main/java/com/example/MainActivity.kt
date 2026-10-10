package com.example

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.domain.models.CyberTab
import com.example.ui.CyberViewModel
import com.example.ui.components.ApiKeyManagementDialog
import com.example.ui.components.LivePreviewContainer
import com.example.ui.components.PermissionGateDialog
import com.example.ui.screens.*
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NavyDark
import com.example.ui.theme.SaffronPrimary
import com.example.voice.VoiceState

class MainActivity : ComponentActivity() {

    private val viewModel: CyberViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                val currentTab by viewModel.currentTab.collectAsState()
                val isPreviewOpen by viewModel.isPreviewOpen.collectAsState()
                val previewType by viewModel.activePreviewType.collectAsState()
                val gateState by viewModel.gateState.collectAsState()
                val voiceState by viewModel.voiceAgent.voiceState.collectAsState()

                val saharaData by viewModel.saharaData.collectAsState()
                val legalData by viewModel.legalData.collectAsState()
                val certData by viewModel.certData.collectAsState()
                val photoConfig by viewModel.photoConfig.collectAsState()

                // Permission Launcher for Speech Audio
                var hasAudioPermission by remember {
                    mutableStateOf(
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                    )
                }

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    hasAudioPermission = isGranted
                }

                LaunchedEffect(Unit) {
                    if (!hasAudioPermission) {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Column {
                                    Text(
                                        text = "CyberSahara AI",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp,
                                        color = NavyDark
                                    )
                                    Text(
                                        text = "ऑल-इन-वन साइबर कैफे, वकील व सहारा एजेंट",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            actions = {
                                // Voice Status Pill
                                Surface(
                                    shape = CircleShape,
                                    color = when (voiceState) {
                                        VoiceState.LISTENING -> SaffronPrimary.copy(alpha = 0.2f)
                                        VoiceState.SPEAKING -> EmeraldSuccess.copy(alpha = 0.2f)
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    modifier = Modifier.padding(end = 6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when (voiceState) {
                                                        VoiceState.LISTENING -> SaffronPrimary
                                                        VoiceState.SPEAKING -> EmeraldSuccess
                                                        else -> Color.Gray
                                                    }
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = when (voiceState) {
                                                VoiceState.LISTENING -> "सुन रहा हूँ"
                                                VoiceState.SPEAKING -> "बोल रहा है"
                                                else -> "वॉयस रेडी"
                                            },
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (voiceState) {
                                                VoiceState.LISTENING -> SaffronPrimary
                                                VoiceState.SPEAKING -> EmeraldSuccess
                                                else -> Color.DarkGray
                                            }
                                        )
                                    }
                                }

                                val hasApiKey by viewModel.geminiManager.hasKey.collectAsState()
                                IconButton(
                                    onClick = { viewModel.openApiKeyDialog() },
                                    modifier = Modifier.testTag("top_api_key_button")
                                ) {
                                    BadgedBox(
                                        badge = {
                                            Badge(
                                                containerColor = if (hasApiKey) EmeraldSuccess else SaffronPrimary
                                            )
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Key,
                                            contentDescription = "API Key Settings",
                                            tint = NavyDark
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { viewModel.scrubAndMaskPii() },
                                    modifier = Modifier.testTag("top_privacy_scrub_button")
                                ) {
                                    Icon(
                                        Icons.Default.Shield,
                                        contentDescription = "Privacy Shield",
                                        tint = NavyDark
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp
                        ) {
                            CyberTab.values().forEach { tab ->
                                val isSelected = currentTab == tab
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { viewModel.setTab(tab) },
                                    icon = {
                                        Icon(
                                            imageVector = when (tab) {
                                                CyberTab.VOICE_AGENT -> Icons.Default.RecordVoiceOver
                                                CyberTab.SAHARA_GOVT -> Icons.Default.AccountBalance
                                                CyberTab.LEGAL_VAKIL -> Icons.Default.Gavel
                                                CyberTab.PHOTO_PVC -> Icons.Default.PhotoCamera
                                                CyberTab.WPS_OCR -> Icons.Default.DocumentScanner
                                                CyberTab.BILLING_PRINT -> Icons.Default.Receipt
                                            },
                                            contentDescription = tab.labelHindi
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = tab.labelHindi,
                                            fontSize = 9.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            maxLines = 1
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = SaffronPrimary,
                                        selectedTextColor = SaffronPrimary,
                                        indicatorColor = SaffronPrimary.copy(alpha = 0.15f)
                                    ),
                                    modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // Main Screen View
                        when (currentTab) {
                            CyberTab.VOICE_AGENT -> VoiceChatScreen(viewModel)
                            CyberTab.SAHARA_GOVT -> SaharaGovtScreen(viewModel)
                            CyberTab.LEGAL_VAKIL -> LegalVakilScreen(viewModel)
                            CyberTab.PHOTO_PVC -> PhotoPvcScreen(viewModel)
                            CyberTab.WPS_OCR -> WpsOcrScreen(viewModel)
                            CyberTab.BILLING_PRINT -> BillingPrintScreen(viewModel)
                        }

                        // Live Preview Overlay Sheet
                        AnimatedVisibility(
                            visible = isPreviewOpen,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                        ) {
                            LivePreviewContainer(
                                previewType = previewType,
                                saharaData = saharaData,
                                legalData = legalData,
                                certData = certData,
                                photoConfig = photoConfig,
                                onApproveVoiceOrButton = {
                                    viewModel.triggerLiveApprovalGate()
                                },
                                onDirectPrint = {
                                    triggerDirectPrint(context, "Document-Print")
                                    viewModel.voiceAgent.speak("प्रिंटर को कमांड भेज दी गई है।", listenAfterSpeaking = false)
                                },
                                onClose = { viewModel.closePreview() }
                            )
                        }

                        // Hybrid Automation Permission Checkpoint Dialog (Gate 1, 2, 3)
                        PermissionGateDialog(
                            gateState = gateState,
                            onDismiss = { viewModel.dismissGate() },
                            onOtpVerified = { otp -> viewModel.onOtpVerified(otp) },
                            onCaptchaSolved = { captcha -> viewModel.onCaptchaSolved(captcha) },
                            onPreviewApproved = { viewModel.onPreviewApproved() }
                        )

                        // Google Gemini API Key Management Dialog
                        val isApiKeyDialogOpen by viewModel.isApiKeyDialogOpen.collectAsState()
                        ApiKeyManagementDialog(
                            geminiManager = viewModel.geminiManager,
                            isOpen = isApiKeyDialogOpen,
                            onDismiss = { viewModel.closeApiKeyDialog() },
                            onKeySaved = {
                                viewModel.closeApiKeyDialog()
                                viewModel.voiceAgent.speak("Gemini API Key सफलतापूर्वक सहेज ली गई है।", listenAfterSpeaking = false)
                            }
                        )
                    }
                }
            }
        }
    }

    private fun triggerDirectPrint(context: Context, docName: String) {
        try {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            val webView = WebView(context)
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    val printAdapter = webView.createPrintDocumentAdapter(docName)
                    printManager?.print(docName, printAdapter, PrintAttributes.Builder().build())
                }
            }
            webView.loadDataWithBaseURL(null, "<html><body><h2>CYBER CAFE PRINT READY DOCUMENT</h2></body></html>", "text/html", "UTF-8", null)
        } catch (e: Exception) {
            // Handled safely
        }
    }
}
