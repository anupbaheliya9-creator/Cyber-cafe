package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.api.GeminiManager
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RubyError
import com.example.ui.theme.SaffronPrimary

@Composable
fun ApiKeyManagementDialog(
    geminiManager: GeminiManager,
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onKeySaved: () -> Unit
) {
    if (!isOpen) return

    val hasKey by geminiManager.hasKey.collectAsState()
    val keyErrorMsg by geminiManager.keyErrorMessage.collectAsState()

    var inputKey by remember { mutableStateOf("") }
    var isKeyVisible by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("api_key_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(SaffronPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = "API Key",
                        tint = SaffronPrimary,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Gemini API Key प्रबंधन",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "कृपया जारी रखने के लिए अपनी Gemini API Key दर्ज करें।",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                // Current Key Status
                Surface(
                    color = if (hasKey) EmeraldSuccess.copy(alpha = 0.1f) else SaffronPrimary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (hasKey) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (hasKey) EmeraldSuccess else SaffronPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (hasKey) "सक्रिय कुंजी: ${geminiManager.getMaskedKey()}" else "कुंजी सेट नहीं है",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (hasKey) EmeraldSuccess else SaffronPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Input Field
                OutlinedTextField(
                    value = inputKey,
                    onValueChange = {
                        inputKey = it
                        localError = null
                    },
                    label = { Text("Google Gemini API Key") },
                    placeholder = { Text("AIzaSy...") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("gemini_api_key_input"),
                    visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                            Icon(
                                imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (isKeyVisible) "Hide Key" else "Show Key"
                            )
                        }
                    },
                    supportingText = {
                        Text(
                            text = "कुंजी 'AIzaSy...' से शुरू होनी चाहिए (सुरक्षित स्थानीय स्टोरेज)",
                            fontSize = 10.sp
                        )
                    },
                    isError = localError != null || keyErrorMsg != null
                )

                // Error Message Display
                val activeError = localError ?: keyErrorMsg
                AnimatedVisibility(visible = activeError != null) {
                    Text(
                        text = activeError ?: "",
                        color = RubyError,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (hasKey) {
                        OutlinedButton(
                            onClick = {
                                geminiManager.clearApiKey()
                                inputKey = ""
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RubyError),
                            modifier = Modifier.testTag("clear_api_key_button")
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }

                    Button(
                        onClick = {
                            if (geminiManager.saveApiKey(inputKey)) {
                                inputKey = ""
                                onKeySaved()
                                onDismiss()
                            } else {
                                localError = "कृपया 'AIzaSy...' से शुरू होने वाली वैध Gemini API Key दर्ज करें।"
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_api_key_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("सहेजें (Save API Key)")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_api_key_dialog")
                ) {
                    Text("बंद करें (Close)")
                }
            }
        }
    }
}
