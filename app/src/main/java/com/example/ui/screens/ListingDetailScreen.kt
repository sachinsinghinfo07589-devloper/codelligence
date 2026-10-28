package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Listing
import com.example.ui.components.ReportListingDialog
import com.example.ui.components.StatusBadge
import com.example.ui.components.shareListing
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun ListingDetailScreen(
    listingId: String,
    viewModel: MainViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allListings by viewModel.allListings.collectAsState()
    val savedIds by viewModel.savedListingIds.collectAsState()
    val listing = allListings.find { it.id == listingId }

    var showReportDialog by remember { mutableStateOf(false) }
    var reportSubmittedMessage by remember { mutableStateOf<String?>(null) }

    if (listing == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Listing not found", style = MaterialTheme.typography.titleMedium)
        }
        return
    }

    val isSaved = savedIds.contains(listing.id)
    val remainingDays = maxOf(0L, (listing.expiresAt - System.currentTimeMillis()) / (24 * 60 * 60 * 1000L))

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp, 16.dp, 16.dp, 80.dp)
            .testTag("listing_detail_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Business Header Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(listOf(ElectricBlue, CyanAccent))
            )
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(ElectricBlue.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = listing.businessName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verified",
                                    tint = CyanAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "Verified Codelligence Member",
                                style = MaterialTheme.typography.labelSmall.copy(color = CyanAccent)
                            )
                        }
                    }

                    StatusBadge(status = listing.status)
                }
            }
        }

        // Title and Category Header
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = ElectricBlue.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = listing.listingType.label.uppercase(),
                        color = CyanAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = listing.category,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = listing.title,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )

            // Validity Countdown Badge
            Surface(
                color = NeonMint.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                border = ButtonDefaults.outlinedButtonBorder().copy(brush = Brush.linearGradient(listOf(NeonMint, CyanAccent)))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = NeonMint,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$remainingDays days remaining in 60-day free promotional cycle (₹0 Fee)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NeonMint,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        // Description Section
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "About this promotion",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = listing.description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp
                    )
                )
            }
        }

        // Location & Availability
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Location & Availability",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Place, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (listing.city != null) "${listing.city}, ${listing.state ?: ""}" else "Pan-India",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (listing.isOnlineAvailable) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = NeonMint, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Services / Openings available online & remotely",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        // Contact & Connect Action Buttons
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Connect Directly with Business",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )

            if (!listing.destinationUrl.isNullOrBlank()) {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(listing.destinationUrl))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("visit_destination_button")
                ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Visit Official Website / Offer", fontWeight = FontWeight.Bold)
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (!listing.contactEmail.isNullOrBlank()) {
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:${listing.contactEmail}")
                                putExtra(Intent.EXTRA_SUBJECT, "Inquiry regarding: ${listing.title}")
                            }
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Email")
                    }
                }

                if (!listing.contactPhone.isNullOrBlank()) {
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${listing.contactPhone}"))
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Call")
                    }
                }
            }
        }

        // Secondary Actions: Share, Save, Report
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { shareListing(context, listing) },
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share")
            }

            OutlinedButton(
                onClick = {
                    val shareMsg = "Check out *${listing.title}* on Codelligence Free Ads Platform (₹0 Fee): ${listing.destinationUrl ?: "https://codelligence.tech"}"
                    val waIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        setPackage("com.whatsapp")
                        putExtra(Intent.EXTRA_TEXT, shareMsg)
                    }
                    try {
                        context.startActivity(waIntent)
                    } catch (_: Exception) {
                        shareListing(context, listing)
                    }
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonMint),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = NeonMint, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("WhatsApp", color = NeonMint)
            }

            OutlinedButton(
                onClick = { viewModel.toggleSave(listing.id) },
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = null,
                    tint = if (isSaved) CyanAccent else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isSaved) "Saved" else "Save")
            }

            TextButton(
                onClick = { showReportDialog = true },
                colors = ButtonDefaults.textButtonColors(contentColor = CoralError),
                modifier = Modifier.testTag("report_listing_button")
            ) {
                Icon(Icons.Outlined.Report, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Report")
            }
        }

        if (reportSubmittedMessage != null) {
            Surface(
                color = CoralError.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = reportSubmittedMessage!!,
                    color = CoralError,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }

        // Platform Transparency Notice
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(12.dp),
            border = ButtonDefaults.outlinedButtonBorder()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Platform Transparency Notice",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = CyanAccent)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Codelligence Free Ads Provider is an independent community promotion and business discovery service owned and operated by Codelligence Technologies. It does not claim affiliation with Google Ads, Meta Ads, LinkedIn Ads, or any third-party ad networks. Initial listing price: ₹0.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                )
            }
        }
    }

    if (showReportDialog) {
        ReportListingDialog(
            listingTitle = listing.title,
            onDismiss = { showReportDialog = false },
            onSubmitReport = { reason, details ->
                viewModel.reportListing(listing.id, reason, details)
                reportSubmittedMessage = "Thank you. Your report has been submitted to the Codelligence moderation team for investigation."
            }
        )
    }
}
