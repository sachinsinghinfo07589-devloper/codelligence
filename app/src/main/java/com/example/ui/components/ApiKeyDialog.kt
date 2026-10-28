package com.example.ui.components

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.R
import com.example.data.api.GeminiService
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun ApiKeyManagementDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentCustomKey by viewModel.customGeminiKey.collectAsState()
    var inputKey by remember { mutableStateOf(currentCustomKey) }
    var showPassword by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<String?>(null) }
    var isTesting by remember { mutableStateOf(false) }
    var testSuccess by remember { mutableStateOf<Boolean?>(null) }

    val hasSystemKey = BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"
    val effectiveKey = GeminiService.getEffectiveApiKey()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.VpnKey, contentDescription = null, tint = CyanAccent)
                Text(
                    text = "API Key & Cloud Credentials",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Gemini API Status Badge
                Surface(
                    color = if (effectiveKey.isNotBlank()) NeonMint.copy(alpha = 0.15f) else ElectricBlue.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
                    border = ButtonDefaults.outlinedButtonBorder()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Gemini AI Status:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Surface(
                                color = if (effectiveKey.isNotBlank()) NeonMint else ElectricBlueLight,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (effectiveKey.isNotBlank()) "KEY ACTIVE" else "SMART ADVISOR",
                                    color = NavyBlack,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = when {
                                currentCustomKey.isNotBlank() -> "Using custom user-provided API key."
                                hasSystemKey -> "Using AI Studio runtime injected GEMINI_API_KEY."
                                else -> "No cloud key configured. Intelligent built-in Smart Advisor handles all inquiries with ₹0 local inference."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Custom Key Input Field
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Custom Gemini API Key:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    OutlinedTextField(
                        value = inputKey,
                        onValueChange = {
                            inputKey = it
                            testResult = null
                            testSuccess = null
                        },
                        placeholder = { Text("AIzaSy...") },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showPassword) "Hide key" else "Show key"
                                )
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("gemini_key_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.setCustomGeminiKey(inputKey)
                                testResult = "Custom key saved successfully."
                                testSuccess = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_custom_key_btn")
                        ) {
                            Text("Save Key")
                        }

                        OutlinedButton(
                            onClick = {
                                val keyToTest = inputKey.ifBlank { effectiveKey }
                                if (keyToTest.isBlank()) {
                                    testResult = "Please enter an API key to test."
                                    testSuccess = false
                                } else {
                                    isTesting = true
                                    testResult = null
                                    viewModel.validateGeminiKey(keyToTest) { valid, msg ->
                                        isTesting = false
                                        testSuccess = valid
                                        testResult = msg
                                    }
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            enabled = !isTesting,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("test_custom_key_btn")
                        ) {
                            if (isTesting) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Test Key")
                            }
                        }
                    }

                    if (currentCustomKey.isNotBlank()) {
                        TextButton(
                            onClick = {
                                inputKey = ""
                                viewModel.setCustomGeminiKey("")
                                testResult = "Custom key cleared. Reset to default."
                                testSuccess = true
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Clear Custom Key", color = CoralError, fontSize = 11.sp)
                        }
                    }

                    if (testResult != null) {
                        Surface(
                            color = if (testSuccess == true) NeonMint.copy(alpha = 0.15f) else CoralError.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = testResult!!,
                                color = if (testSuccess == true) NeonMint else CoralError,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

                // Firebase Cloud Database Info
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Firebase Project & Database Config:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    val dbId = try {
                        context.getString(R.string.firestore_database_id)
                    } catch (_: Exception) {
                        "Not Found"
                    }

                    val webClient = try {
                        context.getString(R.string.default_web_client_id)
                    } catch (_: Exception) {
                        "Default Client"
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Project: gen-lang-client-0547367874", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text("Database: $dbId", fontSize = 10.sp, color = CyanAccent)
                            Text("Web OAuth ID: ${webClient.take(24)}...", fontSize = 10.sp, color = TextSecondaryDark)
                            Text("Rules: Active & Deployed", fontSize = 10.sp, color = NeonMint)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                modifier = Modifier.testTag("close_api_key_dialog")
            ) {
                Text("Done")
            }
        }
    )
}
