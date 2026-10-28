package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.RejectReasonDialog
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ModeratorAdminScreen(
    viewModel: MainViewModel,
    onRoleSwitchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val pendingListings by viewModel.pendingListings.collectAsState()
    val allListings by viewModel.allListings.collectAsState()
    val reports by viewModel.reports.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Pending Queue, 1: Reports, 2: Audit Logs & Stats
    var rejectingListing by remember { mutableStateOf<Listing?>(null) }

    val isAuthorized = currentUser.role == UserRole.MODERATOR || currentUser.role == UserRole.ADMIN

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 75.dp)
            .testTag("moderator_admin_screen")
    ) {
        // Role check banner
        if (!isAuthorized) {
            Card(
                colors = CardDefaults.cardColors(containerColor = AmberPending.copy(alpha = 0.15f)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AmberPending)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Moderation Portal Access",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = AmberPending)
                        )
                        Text(
                            text = "Currently viewing as ${currentUser.role.name}. Switch to Moderator or Admin role to review listings.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Button(
                        onClick = onRoleSwitchClick,
                        colors = ButtonDefaults.buttonColors(containerColor = AmberPending),
                        modifier = Modifier.testTag("switch_to_moderator_button")
                    ) {
                        Text("Switch Role", color = NavyBlack, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Summary Stats Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val approvedCount = allListings.count { it.status == ListingStatus.APPROVED }
            val pendingCount = pendingListings.size
            val openReportsCount = reports.count { it.status == "OPEN" }

            StatCard(title = "Active Live", count = approvedCount.toString(), color = NeonMint, modifier = Modifier.weight(1f))
            StatCard(title = "In Queue", count = pendingCount.toString(), color = AmberPending, modifier = Modifier.weight(1f))
            StatCard(title = "Open Reports", count = openReportsCount.toString(), color = CoralError, modifier = Modifier.weight(1f))
        }

        // Tab Navigation
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = CyanAccent
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Pending Review (${pendingListings.size})", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("User Reports (${reports.size})", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Audit Trail", fontWeight = FontWeight.Bold) }
            )
        }

        when (selectedTab) {
            0 -> PendingQueueTab(
                pendingListings = pendingListings,
                isAuthorized = isAuthorized,
                onApprove = { viewModel.approveListing(it.id) },
                onRejectClick = { rejectingListing = it }
            )
            1 -> ReportsQueueTab(
                reports = reports,
                isAuthorized = isAuthorized,
                onDismiss = { viewModel.dismissReport(it.id) },
                onSuspend = { report ->
                    viewModel.suspendListing(report.listingId, "Suspended due to substantiated user report: ${report.reason}")
                    viewModel.resolveReport(report.id, "Listing suspended")
                }
            )
            2 -> AuditLogsTab(auditLogs = auditLogs)
        }
    }

    if (rejectingListing != null) {
        RejectReasonDialog(
            listingTitle = rejectingListing!!.title,
            onDismiss = { rejectingListing = null },
            onConfirmReject = { reason ->
                viewModel.rejectListing(rejectingListing!!.id, reason)
                rejectingListing = null
            }
        )
    }
}

@Composable
fun StatCard(title: String, count: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = count, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, color = color))
            Text(text = title, style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
        }
    }
}

@Composable
fun PendingQueueTab(
    pendingListings: List<Listing>,
    isAuthorized: Boolean,
    onApprove: (Listing) -> Unit,
    onRejectClick: (Listing) -> Unit
) {
    if (pendingListings.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonMint, modifier = Modifier.size(54.dp))
                Text("Moderation Queue Clear", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text(
                    text = "No submissions currently pending review. All submissions are verified.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(pendingListings, key = { it.id }) { listing ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pending_item_${listing.id}")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = listing.businessName,
                                style = MaterialTheme.typography.labelMedium.copy(color = CyanAccent, fontWeight = FontWeight.Bold)
                            )
                            StatusBadge(status = listing.status)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = listing.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = listing.description,
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                            maxLines = 3
                        )

                        if (!listing.destinationUrl.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Link, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = listing.destinationUrl,
                                    style = MaterialTheme.typography.bodySmall.copy(color = CyanAccent, fontSize = 11.sp),
                                    maxLines = 1
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Moderation Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { onApprove(listing) },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonMint),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("approve_btn_${listing.id}")
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = NavyBlack, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Approve", color = NavyBlack, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { onRejectClick(listing) },
                                colors = ButtonDefaults.buttonColors(containerColor = CoralError),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("reject_btn_${listing.id}")
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Reject", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportsQueueTab(
    reports: List<Report>,
    isAuthorized: Boolean,
    onDismiss: (Report) -> Unit,
    onSuspend: (Report) -> Unit
) {
    if (reports.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No user abuse reports on file.")
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(reports, key = { it.id }) { report ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Flagged: ${report.listingTitle}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = CoralError)
                            )
                            Surface(
                                color = if (report.status == "OPEN") CoralError.copy(alpha = 0.2f) else NeonMint.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = report.status,
                                    color = if (report.status == "OPEN") CoralError else NeonMint,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "Reason: ${report.reason}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        if (report.details.isNotBlank()) {
                            Text(
                                text = "Details: ${report.details}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "Reported by: ${report.reporterEmail}",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark)
                        )

                        if (report.status == "OPEN") {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = { onDismiss(report) }) {
                                    Text("Dismiss as Benign")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { onSuspend(report) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CoralError)
                                ) {
                                    Text("Suspend Listing")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AuditLogsTab(auditLogs: List<AuditLogEntry>) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Platform Audit Log Trail",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Immutable server-side action history for moderation actions and policy enforcement.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(auditLogs, key = { it.id }) { log ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            color = ElectricBlue.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = log.action,
                                color = CyanAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = dateFormat.format(Date(log.timestamp)),
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark, fontSize = 10.sp)
                        )
                    }

                    Text(
                        text = log.details,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Actor: ${log.actorEmail} (${log.actorRole})",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark, fontSize = 10.sp)
                    )
                }
            }
        }
    }
}
