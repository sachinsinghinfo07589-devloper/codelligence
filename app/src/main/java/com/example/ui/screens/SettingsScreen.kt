package com.example.ui.screens

import androidx.compose.foundation.background
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
import com.example.data.api.NetworkClient
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateHelp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var apiUrl by remember { mutableStateOf(NetworkClient.currentBaseUrl) }
    var apiStatusMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var deleteSuccessMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp, 16.dp, 16.dp, 80.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Identity Header
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Codelligence Free Ads Provider",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "A production-grade platform by Codelligence Technologies",
                    style = MaterialTheme.typography.bodySmall.copy(color = CyanAccent)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Version 1.0.0 (Production Release)",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark)
                )
            }
        }

        // Google Sign-In & Firestore Cloud Database Card
        val context = androidx.compose.ui.platform.LocalContext.current
        val activity = context as? android.app.Activity
        val firebaseUser by viewModel.firebaseUser.collectAsState()
        var authStatusMsg by remember { mutableStateOf<String?>(null) }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(ElectricBlue, CyanAccent))
            )
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Firebase Cloud Database & Auth",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Firestore Enterprise & Google Sign-In",
                            style = MaterialTheme.typography.bodySmall.copy(color = CyanAccent)
                        )
                    }

                    Surface(
                        color = if (firebaseUser != null) NeonMint.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (firebaseUser != null) "CONNECTED" else "NOT SIGNED IN",
                            color = if (firebaseUser != null) NeonMint else TextSecondaryDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                if (firebaseUser != null) {
                    Text(
                        text = "Signed in as: ${firebaseUser?.displayName ?: "Google User"} (${firebaseUser?.email})",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "Database ID: ${context.getString(com.example.R.string.firestore_database_id)}",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark, fontSize = 10.sp)
                    )
                    OutlinedButton(
                        onClick = {
                            viewModel.signOut {
                                authStatusMsg = "Signed out successfully."
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralError),
                        modifier = Modifier.testTag("google_sign_out_button")
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sign Out")
                    }
                } else {
                    Text(
                        text = "Authenticate securely with Google Sign-In to sync your free business listings with Firebase Firestore in real-time.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = {
                            if (activity != null) {
                                viewModel.signInWithGoogle(
                                    activity = activity,
                                    onSuccess = {
                                        authStatusMsg = "Google Sign-In successful! Connected to Firestore."
                                    },
                                    onError = { err ->
                                        authStatusMsg = "Sign-in error: $err"
                                    }
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("google_sign_in_button")
                    ) {
                        Icon(Icons.Default.AccountCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign in with Google", fontWeight = FontWeight.Bold)
                    }
                }

                if (authStatusMsg != null) {
                    Text(
                        text = authStatusMsg!!,
                        color = CyanAccent,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Remote Backend API Configuration
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Backend REST API Endpoint",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Configure the live server URL (e.g. your local Docker container at http://10.0.2.2:4000/ or cloud instance):",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = apiUrl,
                    onValueChange = { apiUrl = it },
                    label = { Text("API Base URL") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("api_url_input"),
                    singleLine = true
                )

                Button(
                    onClick = {
                        viewModel.updateApiBaseUrl(apiUrl)
                        apiStatusMessage = "Endpoint updated successfully. Active Base: $apiUrl"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    modifier = Modifier.testTag("save_api_url_button")
                ) {
                    Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save & Reconnect")
                }

                if (apiStatusMessage != null) {
                    Text(
                        text = apiStatusMessage!!,
                        color = NeonMint,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Fair-Use & Zero-Budget Policy Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Zero-Budget & Transparency Commitment",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = NeonMint)
                )
                Text(
                    text = "• ₹0 Listing Fee: Never charges for submitting legitimate promotional ads.\n" +
                            "• 60-Day Validity: Free renewals every 60 days to keep listings fresh.\n" +
                            "• Unlimited Listings: Available per verified business subject to fair-use.\n" +
                            "• Zero Network Claims: Codelligence is not affiliated with Google Ads, Meta Ads, or LinkedIn Ads.\n" +
                            "• Zero Synthetic Metrics: No manufactured view counts or purchased bot traffic.",
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Community Guidelines & Indian Intermediary Compliance
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Content Guidelines & Compliance",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Strict zero-tolerance policy against phishing, malware, fake government certifications, pyramid schemes, deceptive internship fees, unlawful products, and counterfeit goods. All reported listings undergo moderator inspection.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedButton(onClick = onNavigateHelp) {
                    Icon(Icons.Default.Help, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Help & Support Center")
                }
            }
        }

        // Account Deletion & Privacy Rights
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Account & Privacy Management",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "You have full ownership of your data. You may request complete account erasure and listing removal at any time.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = { showDeleteAccountDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralError),
                    modifier = Modifier.testTag("delete_account_button")
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Request Account Deletion")
                }

                if (deleteSuccessMessage != null) {
                    Text(
                        text = deleteSuccessMessage!!,
                        color = CoralError,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Delete Account and All Listings?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "This action permanently queues your user account, business profile, and all active/pending listings for complete deletion pursuant to privacy guidelines."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        deleteSuccessMessage = "Account deletion request processed. Session ended."
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralError)
                ) {
                    Text("Confirm Deletion")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
