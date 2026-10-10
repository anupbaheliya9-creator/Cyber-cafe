package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.models.GateState
import com.example.domain.models.GateType
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.SaffronPrimary

@Composable
fun PermissionGateDialog(
    gateState: GateState,
    onDismiss: () -> Unit,
    onOtpVerified: (String) -> Unit,
    onCaptchaSolved: (String) -> Unit,
    onPreviewApproved: () -> Unit
) {
    if (gateState.activeGate == GateType.NONE) return

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("permission_gate_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
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
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(
                            when (gateState.activeGate) {
                                GateType.OTP_VERIFICATION -> SaffronPrimary.copy(alpha = 0.15f)
                                GateType.CAPTCHA_CHALLENGE -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                GateType.LIVE_PREVIEW_APPROVAL -> EmeraldSuccess.copy(alpha = 0.15f)
                                else -> MaterialTheme.colorScheme.primaryContainer
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (gateState.activeGate) {
                            GateType.OTP_VERIFICATION -> Icons.Default.VpnKey
                            GateType.CAPTCHA_CHALLENGE -> Icons.Default.Security
                            GateType.LIVE_PREVIEW_APPROVAL -> Icons.Default.CheckCircle
                            else -> Icons.Default.Lock
                        },
                        contentDescription = "Checkpoint",
                        tint = when (gateState.activeGate) {
                            GateType.OTP_VERIFICATION -> SaffronPrimary
                            GateType.CAPTCHA_CHALLENGE -> MaterialTheme.colorScheme.secondary
                            GateType.LIVE_PREVIEW_APPROVAL -> EmeraldSuccess
                            else -> MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = gateState.gateTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = gateState.gateDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // Gate Specific Controls
                when (gateState.activeGate) {
                    GateType.OTP_VERIFICATION -> {
                        OtpGateContent(
                            targetPhone = gateState.otpTargetPhone,
                            onVerify = onOtpVerified
                        )
                    }
                    GateType.CAPTCHA_CHALLENGE -> {
                        CaptchaGateContent(
                            initialCaptcha = gateState.captchaText,
                            onSolve = onCaptchaSolved
                        )
                    }
                    GateType.LIVE_PREVIEW_APPROVAL -> {
                        LiveApprovalGateContent(
                            actionLabel = gateState.pendingActionLabel,
                            onApprove = onPreviewApproved
                        )
                    }
                    else -> {}
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("gate_cancel_button")
                ) {
                    Text("रद्द करें (Cancel)")
                }
            }
        }
    }
}

@Composable
private fun OtpGateContent(
    targetPhone: String,
    onVerify: (String) -> Unit
) {
    var otpValue by remember { mutableStateOf("482910") }
    var isBiometricMode by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "पंजीकृत मोबाइल: $targetPhone",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = otpValue,
            onValueChange = { if (it.length <= 6) otpValue = it },
            label = { Text("6-अंकों का OTP") },
            placeholder = { Text("123456") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("otp_input_field"),
            textStyle = MaterialTheme.typography.headlineSmall.copy(
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 6.sp
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onVerify(otpValue) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("verify_otp_button"),
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
            ) {
                Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("सत्यापित करें")
            }

            OutlinedButton(
                onClick = {
                    otpValue = "904128"
                    onVerify(otpValue)
                },
                modifier = Modifier.testTag("biometric_scan_button")
            ) {
                Icon(Icons.Default.Fingerprint, contentDescription = "Biometric", modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun CaptchaGateContent(
    initialCaptcha: String,
    onSolve: (String) -> Unit
) {
    var captchaCode by remember { mutableStateOf(initialCaptcha) }
    var enteredCaptcha by remember { mutableStateOf("") }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Visual Captcha Badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFE2E8F0))
                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                .padding(horizontal = 24.dp, vertical = 10.dp)
                .clickable {
                    val chars = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"
                    captchaCode = (1..5).map { chars.random() }.joinToString("")
                },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = captchaCode,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 8.sp,
                        color = Color(0xFF1E293B)
                    )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh Captcha",
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Text(
            text = "नया कोड पाने के लिए कैप्चा पर टैप करें",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        OutlinedTextField(
            value = enteredCaptcha,
            onValueChange = { enteredCaptcha = it.uppercase() },
            label = { Text("कैप्चा कोड दर्ज करें") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("captcha_input_field"),
            trailingIcon = {
                IconButton(onClick = { enteredCaptcha = captchaCode }) {
                    Icon(Icons.Default.AutoFixHigh, contentDescription = "Auto Fill", tint = SaffronPrimary)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onSolve(if (enteredCaptcha.isBlank()) captchaCode else enteredCaptcha) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("submit_captcha_button")
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("कैप्चा सत्यापित करें (OK)")
        }
    }
}

@Composable
private fun LiveApprovalGateContent(
    actionLabel: String,
    onApprove: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            color = EmeraldSuccess.copy(alpha = 0.1f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.RecordVoiceOver,
                    contentDescription = null,
                    tint = EmeraldSuccess,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "एआई एजेंट पूछ रहा है: \"क्या सभी जानकारी सही है?\"",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = EmeraldSuccess
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onApprove,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("approve_live_preview_button"),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
        ) {
            Icon(Icons.Default.DoneAll, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (actionLabel.isNotBlank()) actionLabel else "हाँ, सब सही है - प्रिंट करें (OK)")
        }
    }
}
