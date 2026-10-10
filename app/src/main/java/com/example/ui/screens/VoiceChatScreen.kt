package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CyberViewModel
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyDark
import com.example.ui.theme.SaffronPrimary
import com.example.voice.VoiceState

@Composable
fun VoiceChatScreen(viewModel: CyberViewModel) {
    val voiceAgent = viewModel.voiceAgent
    val voiceState by voiceAgent.voiceState.collectAsState()
    val messages by voiceAgent.messages.collectAsState()
    val liveSpokenText by voiceAgent.liveSpokenText.collectAsState()
    val currentLang by voiceAgent.currentLanguage.collectAsState()

    var inputPrompt by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll on new message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Mic Pulsing Animation
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (voiceState == VoiceState.LISTENING) 1.25f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Top Language Selector & Mode Indicator
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = "Language",
                        tint = SaffronPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "भाषा (Language):",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Language chips
                val languages = listOf(
                    "hi-IN" to "हिन्दी",
                    "en-IN" to "English",
                    "bho-IN" to "भोजपुरी",
                    "bn-IN" to "বাংলা",
                    "te-IN" to "తెలుగు",
                    "mr-IN" to "मराठी"
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(languages) { (code, name) ->
                        FilterChip(
                            selected = currentLang == code,
                            onClick = { voiceAgent.setLanguage(code) },
                            label = { Text(name, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SaffronPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Center Voice Visualizer Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("voice_visualizer_card"),
            colors = CardDefaults.cardColors(
                containerColor = when (voiceState) {
                    VoiceState.LISTENING -> SaffronPrimary.copy(alpha = 0.12f)
                    VoiceState.SPEAKING -> EmeraldSuccess.copy(alpha = 0.12f)
                    VoiceState.PROCESSING -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                    else -> MaterialTheme.colorScheme.surface
                }
            ),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Interactive Mic Icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(
                            when (voiceState) {
                                VoiceState.LISTENING -> SaffronPrimary
                                VoiceState.SPEAKING -> EmeraldSuccess
                                VoiceState.PROCESSING -> MaterialTheme.colorScheme.secondary
                                else -> NavyDark
                            }
                        )
                        .clickable {
                            if (voiceState == VoiceState.LISTENING) {
                                voiceAgent.stopListening()
                            } else {
                                voiceAgent.startListening()
                            }
                        }
                        .testTag("main_mic_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (voiceState) {
                            VoiceState.LISTENING -> Icons.Default.Mic
                            VoiceState.SPEAKING -> Icons.AutoMirrored.Filled.VolumeUp
                            VoiceState.PROCESSING -> Icons.Default.HourglassTop
                            else -> Icons.Default.MicNone
                        },
                        contentDescription = "Microphone",
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = when (voiceState) {
                        VoiceState.LISTENING -> "सुन रहा हूँ... बोलिए (Listening...)"
                        VoiceState.SPEAKING -> "एआई बोल रहा है (Speaking Response...)"
                        VoiceState.PROCESSING -> "आदेश प्रोसेस हो रहा है (Processing Command...)"
                        else -> "माइक दबाकर बोलें (Tap Mic or Speak)"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = when (voiceState) {
                        VoiceState.LISTENING -> SaffronPrimary
                        VoiceState.SPEAKING -> EmeraldSuccess
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )

                AnimatedVisibility(visible = liveSpokenText.isNotBlank()) {
                    Text(
                        text = "\"$liveSpokenText\"",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val hasKey by viewModel.geminiManager.hasKey.collectAsState()
        if (!hasKey) {
            Surface(
                onClick = { viewModel.openApiKeyDialog() },
                color = SaffronPrimary.copy(alpha = 0.12f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("api_key_prompt_banner")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Key,
                        contentDescription = null,
                        tint = SaffronPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "कृपया जारी रखने के लिए अपनी Gemini API Key दर्ज करें।",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = SaffronPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "दर्ज करें >",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SaffronPrimary
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Quick Suggestion Chips
        val quickCommands = listOf(
            "Gemini API Key",
            "सहारा रिफंड चेक करो",
            "वकालतनामा ड्राफ्ट करो",
            "शपथ पत्र बनाओ",
            "कानूनी नोटिस तैयार करो",
            "8 पासपोर्ट फोटो शीट",
            "PVC आधार कार्ड",
            "कुल बिल व QR कोड",
            "OTP गेट खोलो"
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(quickCommands) { cmd ->
                SuggestionChip(
                    onClick = { voiceAgent.simulateSpokenCommand(cmd) },
                    label = { Text(cmd, fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    icon = {
                        Icon(
                            Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = SaffronPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Message Feed
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                MessageBubble(msg.text, msg.isUser)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Input Field & Send/Mic row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputPrompt,
                onValueChange = { inputPrompt = it },
                placeholder = { Text("बोलें या यहाँ टाइप करें...") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("voice_chat_text_input"),
                singleLine = true,
                shape = RoundedCornerShape(24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (inputPrompt.isNotBlank()) {
                        voiceAgent.simulateSpokenCommand(inputPrompt)
                        inputPrompt = ""
                    } else {
                        voiceAgent.startListening()
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SaffronPrimary)
                    .testTag("send_voice_button")
            ) {
                Icon(
                    imageVector = if (inputPrompt.isNotBlank()) Icons.AutoMirrored.Filled.Send else Icons.Default.Mic,
                    contentDescription = "Send",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun MessageBubble(text: String, isUser: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (isUser) 14.dp else 2.dp,
                bottomEnd = if (isUser) 2.dp else 14.dp
            ),
            color = if (isUser) SaffronPrimary else MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    text = if (isUser) "आप (You)" else "एआई सहायक (AI Agent)",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isUser) Color.White.copy(alpha = 0.8f) else SaffronPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
