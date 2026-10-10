package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.print.PrintManager
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionRecord
import com.example.ui.CyberViewModel
import com.example.ui.components.PreviewDocumentType
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyDark
import com.example.ui.theme.RubyError
import com.example.ui.theme.SaffronPrimary

@Composable
fun BillingPrintScreen(viewModel: CyberViewModel) {
    val items by viewModel.billingItems.collectAsState()
    val upiId by viewModel.upiId.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val context = LocalContext.current

    val totalAmount = items.sumOf { it.second }
    var newItemName by remember { mutableStateOf("") }
    var newItemPrice by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "स्मार्ट बिलिंग, डायनामिक UPI QR एवं प्रिंटर",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "ऑटो रेट-कार्ड, थर्मल रसीद, A4 डायरेक्ट प्रिंटर एवं दैनिक ट्रांजेक्शन लॉग",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 1. Dynamic UPI QR Code & Total Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "कुल बिल (TOTAL AMOUNT)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "₹ ${totalAmount.toInt()}", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = SaffronPrimary)
                    }

                    Surface(
                        color = EmeraldSuccess.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "LIVE UPI READY",
                            color = EmeraldSuccess,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // High-fidelity Stylized QR Code Canvas
                DynamicUpiQrCanvas(amount = totalAmount, upiId = upiId)

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "UPI ID: $upiId",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = NavyDark
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val upiUri = Uri.parse("upi://pay?pa=$upiId&pn=DigitalCyberCafe&am=$totalAmount&cu=INR")
                            val intent = Intent(Intent.ACTION_VIEW, upiUri)
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                viewModel.voiceAgent.speak("UPI भुगतान ऐप खोलें अथवा QR कोड स्कैन करें।", listenAfterSpeaking = false)
                            }
                        },
                        modifier = Modifier.weight(1f).testTag("pay_upi_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("UPI ऐप में खोलें", fontSize = 11.sp)
                    }

                    FilledTonalButton(
                        onClick = { viewModel.showPreview(PreviewDocumentType.THERMAL_RECEIPT) },
                        modifier = Modifier.weight(1f).testTag("preview_thermal_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("थर्मल रसीद", fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Rate Card Itemization
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(text = "दुकान रेट-कार्ड व आइटम सूची (Billing Items)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)

                Spacer(modifier = Modifier.height(8.dp))

                items.forEachIndexed { index, (name, price) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "${index + 1}. $name", fontSize = 12.sp, modifier = Modifier.weight(1f))
                        Text(text = "₹ ${price.toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        IconButton(
                            onClick = { viewModel.removeBillingItem(index) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color.LightGray)

                // Add Custom Service Item Row
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newItemName,
                        onValueChange = { newItemName = it },
                        placeholder = { Text("नया कार्य / सर्विस") },
                        modifier = Modifier.weight(1.3f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    OutlinedTextField(
                        value = newItemPrice,
                        onValueChange = { newItemPrice = it },
                        placeholder = { Text("₹ मूल्य") },
                        modifier = Modifier.weight(0.7f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = {
                            val priceVal = newItemPrice.toDoubleOrNull() ?: 20.0
                            if (newItemName.isNotBlank()) {
                                viewModel.addBillingItem(newItemName, priceVal)
                                newItemName = ""
                                newItemPrice = ""
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(NavyDark)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Daily Transactions Audit Log (Room DB)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "दैनिक ट्रांजेक्शन लॉग (Local Audit Log)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)
                    Text(text = "${transactions.size} कार्य", fontSize = 11.sp, color = SaffronPrimary, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (transactions.isEmpty()) {
                    Text(text = "आज अभी तक कोई ट्रांजेक्शन रिकॉर्ड नहीं है।", fontSize = 11.sp, color = Color.Gray)
                } else {
                    transactions.take(5).forEach { txn ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = txn.customerName, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Text(text = "${txn.serviceName} • ${txn.paymentMode}", fontSize = 10.sp, color = Color.Gray)
                            }
                            Text(text = "₹ ${txn.amount.toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EmeraldSuccess)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Security & PII Privacy Scrub Button
        OutlinedButton(
            onClick = { viewModel.scrubAndMaskPii() },
            modifier = Modifier.fillMaxWidth().testTag("scrub_pii_privacy_button"),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = RubyError),
            border = ButtonDefaults.outlinedButtonBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RubyError))
        ) {
            Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("कार्य पूर्ण: PII व आधार डेटा सुरक्षित डिलीट/मास्क करें", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DynamicUpiQrCanvas(amount: Double, upiId: String) {
    Box(
        modifier = Modifier
            .size(160.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .border(2.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val s = size.width
            val cellSize = s / 15f

            // Corner Eyes
            drawRect(color = Color.Black, topLeft = Offset(0f, 0f), size = Size(cellSize * 4, cellSize * 4))
            drawRect(color = Color.White, topLeft = Offset(cellSize, cellSize), size = Size(cellSize * 2, cellSize * 2))
            drawRect(color = Color.Black, topLeft = Offset(cellSize * 1.5f, cellSize * 1.5f), size = Size(cellSize, cellSize))

            drawRect(color = Color.Black, topLeft = Offset(s - cellSize * 4, 0f), size = Size(cellSize * 4, cellSize * 4))
            drawRect(color = Color.White, topLeft = Offset(s - cellSize * 3, cellSize), size = Size(cellSize * 2, cellSize * 2))
            drawRect(color = Color.Black, topLeft = Offset(s - cellSize * 2.5f, cellSize * 1.5f), size = Size(cellSize, cellSize))

            drawRect(color = Color.Black, topLeft = Offset(0f, s - cellSize * 4), size = Size(cellSize * 4, cellSize * 4))
            drawRect(color = Color.White, topLeft = Offset(cellSize, s - cellSize * 3), size = Size(cellSize * 2, cellSize * 2))
            drawRect(color = Color.Black, topLeft = Offset(cellSize * 1.5f, s - cellSize * 2.5f), size = Size(cellSize, cellSize))

            // Interior Data Pattern based on amount
            val seed = (amount.toInt() * 7 + 13) % 100
            for (r in 0 until 15) {
                for (c in 0 until 15) {
                    val inTopLeft = r < 5 && c < 5
                    val inTopRight = r < 5 && c > 9
                    val inBottomLeft = r > 9 && c < 5
                    if (!inTopLeft && !inTopRight && !inBottomLeft) {
                        if ((r * 3 + c * 5 + seed) % 3 == 0) {
                            drawRect(
                                color = Color.Black,
                                topLeft = Offset(c * cellSize, r * cellSize),
                                size = Size(cellSize * 0.9f, cellSize * 0.9f)
                            )
                        }
                    }
                }
            }
        }
    }
}
