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
import com.example.data.model.Listing
import com.example.data.model.ListingStatus
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun MyListingsScreen(
    viewModel: MainViewModel,
    onListingClick: (Listing) -> Unit,
    onNavigateSubmit: () -> Unit,
    onNavigateEditProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val businessProfile by viewModel.currentBusiness.collectAsState()
    val myListings by viewModel.myListings.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("my_listings_screen"),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Business Profile Header Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = businessProfile?.name ?: "Business Profile",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verified",
                                    tint = CyanAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = businessProfile?.category ?: "Enterprise",
                                style = MaterialTheme.typography.bodySmall.copy(color = CyanAccent)
                            )
                        }

                        OutlinedButton(
                            onClick = onNavigateEditProfile,
                            modifier = Modifier.testTag("edit_business_profile_btn")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit")
                        }
                    }

                    Text(
                        text = businessProfile?.shortDescription ?: "Welcome to your business hub.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${myListings.size} Total Promotions Posted",
                            style = MaterialTheme.typography.labelSmall.copy(color = NeonMint, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Fair-Use: Unlimited @ ₹0",
                            style = MaterialTheme.typography.labelSmall.copy(color = CyanAccent)
                        )
                    }
                }
            }
        }

        // Section Title with Submit FAB
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Business Listings",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Button(
                    onClick = onNavigateSubmit,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("post_ad_btn_my_listings")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Post Ad")
                }
            }
        }

        if (myListings.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.PostAdd, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(48.dp))
                        Text("No Listings Submitted Yet", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Text(
                            text = "Publish your first free promotional listing today to reach customers and tech talent.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onNavigateSubmit) {
                            Text("Create First Listing (₹0)")
                        }
                    }
                }
            }
        } else {
            items(myListings, key = { it.id }) { listing ->
                MyListingItemCard(
                    listing = listing,
                    onClick = { onListingClick(listing) },
                    onRenew = { viewModel.renewListing(listing.id) }
                )
            }
        }
    }
}

@Composable
fun MyListingItemCard(
    listing: Listing,
    onClick: () -> Unit,
    onRenew: () -> Unit
) {
    val remainingDays = maxOf(0L, (listing.expiresAt - System.currentTimeMillis()) / (24 * 60 * 60 * 1000L))

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = listing.listingType.label,
                    style = MaterialTheme.typography.labelSmall.copy(color = CyanAccent, fontWeight = FontWeight.Bold)
                )
                StatusBadge(status = listing.status)
            }

            Text(
                text = listing.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Text(
                text = listing.description,
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                maxLines = 2
            )

            // Rejection reason warning if rejected
            if (listing.status == ListingStatus.REJECTED && !listing.rejectionReason.isNullOrBlank()) {
                Surface(
                    color = CoralError.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Moderator Rejection Reason:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = CoralError)
                        )
                        Text(
                            text = listing.rejectionReason,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = NeonMint, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when (listing.status) {
                            ListingStatus.APPROVED -> "$remainingDays days remaining"
                            ListingStatus.PENDING_REVIEW -> "Under Review (< 24h)"
                            ListingStatus.REJECTED -> "Needs Correction"
                            ListingStatus.SUSPENDED -> "Suspended"
                            else -> "Expired"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (listing.status == ListingStatus.APPROVED) NeonMint else AmberPending,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                // Renew button for approved or expired items
                if (listing.status == ListingStatus.APPROVED || listing.status == ListingStatus.EXPIRED) {
                    TextButton(onClick = onRenew) {
                        Icon(Icons.Default.Autorenew, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Renew 60 Days (₹0)", fontSize = 12.sp, color = CyanAccent)
                    }
                }
            }
        }
    }
}
