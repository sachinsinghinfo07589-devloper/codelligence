package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodelligenceTopBar(
    title: String,
    currentUser: User,
    unreadNotificationsCount: Int = 0,
    onRoleSwitchClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onNavigationClick: (() -> Unit)? = null
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.statusBarsPadding()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (onNavigationClick != null) {
                        IconButton(
                            onClick = onNavigationClick,
                            modifier = Modifier.testTag("top_bar_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(ElectricBlue, CyanAccent)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "C",
                                color = NavyBlack,
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                    }

                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Codelligence Technologies",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = CyanAccent,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Role pill button
                    AssistChip(
                        onClick = onRoleSwitchClick,
                        label = {
                            Text(
                                text = currentUser.role.name.replace("_", " "),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = when (currentUser.role) {
                                    UserRole.ADMIN -> Icons.Default.Security
                                    UserRole.MODERATOR -> Icons.Default.Shield
                                    UserRole.BUSINESS_OWNER -> Icons.Default.Business
                                    UserRole.USER -> Icons.Default.Person
                                    UserRole.GUEST -> Icons.Default.Public
                                },
                                contentDescription = "Role",
                                modifier = Modifier.size(14.dp),
                                tint = when (currentUser.role) {
                                    UserRole.ADMIN -> CoralError
                                    UserRole.MODERATOR -> AmberPending
                                    UserRole.BUSINESS_OWNER -> CyanAccent
                                    else -> ElectricBlueLight
                                }
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = AssistChipDefaults.assistChipBorder(
                            enabled = true,
                            borderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.testTag("role_switcher_chip")
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    // Notifications icon with badge
                    IconButton(
                        onClick = onNotificationsClick,
                        modifier = Modifier.testTag("notifications_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotificationsCount > 0) {
                                    Badge(
                                        containerColor = CoralError,
                                        contentColor = Color.White
                                    ) {
                                        Text("$unreadNotificationsCount")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Notifications",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: ListingStatus) {
    val (bgColor, textColor) = when (status) {
        ListingStatus.APPROVED -> Pair(NeonMint.copy(alpha = 0.15f), NeonMint)
        ListingStatus.PENDING_REVIEW -> Pair(AmberPending.copy(alpha = 0.15f), AmberPending)
        ListingStatus.REJECTED -> Pair(CoralError.copy(alpha = 0.15f), CoralError)
        ListingStatus.SUSPENDED -> Pair(CoralError.copy(alpha = 0.25f), CoralError)
        ListingStatus.EXPIRED -> Pair(Color.Gray.copy(alpha = 0.15f), Color.LightGray)
        ListingStatus.DRAFT -> Pair(ElectricBlue.copy(alpha = 0.15f), ElectricBlueLight)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = textColor,
                fontSize = 11.sp
            )
        )
    }
}

@Composable
fun ListingCard(
    listing: Listing,
    isSaved: Boolean,
    onCardClick: () -> Unit,
    onSaveToggle: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        onClick = onCardClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(
                    MaterialTheme.colorScheme.outline,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("listing_card_${listing.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Business Name & Status/Validity
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(ElectricBlue.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = listing.businessName,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verified",
                        tint = CyanAccent,
                        modifier = Modifier.size(13.dp)
                    )
                }

                // Type badge
                SuggestionChip(
                    onClick = {},
                    label = {
                        Text(
                            text = listing.listingType.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = when (listing.listingType) {
                            ListingType.INTERNSHIP -> NeonMint.copy(alpha = 0.15f)
                            ListingType.EVENT -> AmberPending.copy(alpha = 0.15f)
                            else -> ElectricBlue.copy(alpha = 0.15f)
                        },
                        labelColor = when (listing.listingType) {
                            ListingType.INTERNSHIP -> NeonMint
                            ListingType.EVENT -> AmberPending
                            else -> CyanAccent
                        }
                    ),
                    border = null,
                    modifier = Modifier.height(26.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = listing.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Description
            Text(
                text = listing.description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category & Location Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Category Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = listing.category,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // City / Online
                val locationText = if (listing.city != null) {
                    "${listing.city}${if (listing.state != null) ", ${listing.state}" else ""}"
                } else if (listing.isOnlineAvailable) {
                    "Online Available"
                } else "Pan-India"

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (listing.city != null) Icons.Default.Place else Icons.Default.Language,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = CyanAccent
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = locationText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        ),
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // Footer: 60-day Validity pill, Share, Save
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val remainingDays = maxOf(0L, (listing.expiresAt - System.currentTimeMillis()) / (24 * 60 * 60 * 1000L))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = NeonMint,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$remainingDays days remaining (₹0)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NeonMint,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onShareClick,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("share_listing_${listing.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onSaveToggle,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("save_listing_${listing.id}")
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isSaved) CyanAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RoleSwitcherDialog(
    currentRole: UserRole,
    onRoleSelected: (UserRole) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Switch Platform Role",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Select any user role to experience the platform permissions, submission limits, and in-app moderation features:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                UserRole.values().forEach { role ->
                    val isSelected = role == currentRole
                    Card(
                        onClick = {
                            onRoleSelected(role)
                            onDismiss()
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) ElectricBlue.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                        ),
                        border = if (isSelected) {
                            CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(ElectricBlue, CyanAccent)))
                        } else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("select_role_${role.name}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = role.name.replace("_", " "),
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) CyanAccent else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = when (role) {
                                        UserRole.GUEST -> "Browse approved public listings only"
                                        UserRole.USER -> "Search, save favorites, report listings"
                                        UserRole.BUSINESS_OWNER -> "Manage business profile & submit free ads"
                                        UserRole.MODERATOR -> "Review queue, approve/reject submissions"
                                        UserRole.ADMIN -> "Full platform controls, audit logs & settings"
                                    },
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Active",
                                    tint = CyanAccent
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun ReportListingDialog(
    listingTitle: String,
    onDismiss: () -> Unit,
    onSubmitReport: (reason: String, details: String) -> Unit
) {
    var selectedReason by remember { mutableStateOf("Misleading claims or false information") }
    var details by remember { mutableStateOf("") }

    val reasons = listOf(
        "Misleading claims or false information",
        "Phishing, malware, or suspicious destination URL",
        "Deceptive internship or fraudulent job offer",
        "Counterfeit, scam, or unlawful product/service",
        "Spam, repetitive, or unauthorized advertisement",
        "Infringing trademarks or copyright"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Report Listing",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Reporting: \"$listingTitle\"",
                    style = MaterialTheme.typography.labelMedium.copy(color = CyanAccent)
                )

                Text(
                    text = "Select primary reason:",
                    style = MaterialTheme.typography.labelSmall
                )

                reasons.forEach { reason ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReason = reason }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = (reason == selectedReason),
                            onClick = { selectedReason = reason }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = reason,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("Additional details (optional)") },
                    placeholder = { Text("Explain why this listing violates terms...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .testTag("report_details_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmitReport(selectedReason, details)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = CoralError),
                modifier = Modifier.testTag("submit_report_button")
            ) {
                Text("Submit Report", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun RejectReasonDialog(
    listingTitle: String,
    onDismiss: () -> Unit,
    onConfirmReject: (reason: String) -> Unit
) {
    var reason by remember { mutableStateOf("") }
    var selectedPreset by remember { mutableStateOf("") }

    val presets = listOf(
        "Invalid or broken destination URL",
        "Misleading internship / hidden training fees detected",
        "Duplicate submission / excessive repetitive posting",
        "Missing verified business contact coordinates"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Reject Submission",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = CoralError)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Provide mandatory reason for \"$listingTitle\":",
                    style = MaterialTheme.typography.bodySmall
                )

                presets.forEach { preset ->
                    AssistChip(
                        onClick = {
                            selectedPreset = preset
                            reason = preset
                        },
                        label = { Text(preset, style = MaterialTheme.typography.labelSmall) }
                    )
                }

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason for Rejection *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reject_reason_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (reason.isNotBlank()) {
                        onConfirmReject(reason)
                        onDismiss()
                    }
                },
                enabled = reason.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CoralError),
                modifier = Modifier.testTag("confirm_reject_button")
            ) {
                Text("Reject Listing")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun shareListing(context: Context, listing: Listing) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(
            Intent.EXTRA_TEXT,
            "Discover \"${listing.title}\" by ${listing.businessName} on Codelligence Free Ads Provider (₹0 Fee):\n" +
                    (listing.destinationUrl ?: "https://codelligence.tech/listings/${listing.id}")
        )
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Share Listing via")
    context.startActivity(shareIntent)
}
