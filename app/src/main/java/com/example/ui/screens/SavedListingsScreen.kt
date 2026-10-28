package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Listing
import com.example.ui.components.ListingCard
import com.example.ui.components.shareListing
import com.example.ui.theme.CyanAccent
import com.example.ui.viewmodel.MainViewModel

@Composable
fun SavedListingsScreen(
    viewModel: MainViewModel,
    onListingClick: (Listing) -> Unit,
    onExploreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val savedListings by viewModel.savedListings.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("saved_listings_screen"),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = 6.dp)) {
                Text(
                    text = "Saved Bookmarks",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${savedListings.size} businesses and opportunities saved for offline access.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (savedListings.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.BookmarkBorder, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(52.dp))
                        Text("No Saved Listings Yet", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text(
                            text = "Tap the bookmark icon on any promotional card to save it for quick reference.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onExploreClick) {
                            Text("Discover Businesses")
                        }
                    }
                }
            }
        } else {
            items(savedListings, key = { it.id }) { listing ->
                ListingCard(
                    listing = listing,
                    isSaved = true,
                    onCardClick = { onListingClick(listing) },
                    onSaveToggle = { viewModel.toggleSave(listing.id) },
                    onShareClick = { shareListing(context, listing) }
                )
            }
        }
    }
}
