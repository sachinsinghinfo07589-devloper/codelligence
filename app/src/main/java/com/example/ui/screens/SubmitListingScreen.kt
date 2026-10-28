package com.example.ui.screens

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Listing
import com.example.data.model.ListingStatus
import com.example.data.model.ListingType
import com.example.ui.components.ListingCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitListingScreen(
    viewModel: MainViewModel,
    onSubmissionSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentBusiness by viewModel.currentBusiness.collectAsState()
    val scrollState = rememberScrollState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Software and web development") }
    var selectedType by remember { mutableStateOf(ListingType.SERVICE) }
    var destinationUrl by remember { mutableStateOf("") }
    var city by remember { mutableStateOf(currentBusiness?.city ?: "Bengaluru") }
    var state by remember { mutableStateOf(currentBusiness?.state ?: "Karnataka") }
    var isOnlineAvailable by remember { mutableStateOf(true) }
    var contactEmail by remember { mutableStateOf(currentBusiness?.publicEmail ?: "") }
    var contactPhone by remember { mutableStateOf(currentBusiness?.publicPhone ?: "") }

    var termsAccepted by remember { mutableStateOf(false) }
    var accuracyDeclared by remember { mutableStateOf(false) }

    var showPreview by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var showCategoryMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp, 16.dp, 16.dp, 90.dp)
            .testTag("submit_listing_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Form Header Banner
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Publish Free Promotion",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Surface(
                        color = NeonMint.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "₹0 FEE",
                            color = NeonMint,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Submitting as: ${currentBusiness?.name ?: "Verified Business"}",
                    style = MaterialTheme.typography.bodySmall.copy(color = CyanAccent)
                )
                Text(
                    text = "Submissions undergo quick community verification before appearing in the live feed. Valid for 60 days with free renewal.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        }

        // AI Fast Draft Assist Chip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Listing Details", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            AssistChip(
                onClick = {
                    title = "Cloud Infrastructure & Full-Stack Engineering Fellowship"
                    description = "Comprehensive engineering fellowship with verified mentors covering Kotlin, Jetpack Compose, Docker, and PostgreSQL. ₹0 fee for participants."
                    destinationUrl = "https://codelligence.tech/fellowships"
                },
                leadingIcon = {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                },
                label = { Text("Auto-Draft with Gemini", fontSize = 11.sp, color = CyanAccent) },
                colors = AssistChipDefaults.assistChipColors(containerColor = ElectricBlue.copy(alpha = 0.15f))
            )
        }

        // Title
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Listing Title *") },
            placeholder = { Text("e.g., Scalable Cloud & Android Development") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("submit_title_input"),
            singleLine = true
        )

        // Description
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Detailed Description *") },
            placeholder = { Text("Describe your services, products, or internship opportunities clearly...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .testTag("submit_description_input")
        )

        // Category Selector
        ExposedDropdownMenuBox(
            expanded = showCategoryMenu,
            onExpandedChange = { showCategoryMenu = !showCategoryMenu }
        ) {
            OutlinedTextField(
                value = selectedCategory,
                onValueChange = {},
                readOnly = true,
                label = { Text("Category *") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryMenu) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
                    .testTag("submit_category_dropdown")
            )
            ExposedDropdownMenu(
                expanded = showCategoryMenu,
                onDismissRequest = { showCategoryMenu = false }
            ) {
                viewModel.categories.forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(cat.name) },
                        onClick = {
                            selectedCategory = cat.name
                            showCategoryMenu = false
                        }
                    )
                }
            }
        }

        // Listing Type Selector
        Text(
            text = "Promotion Type *",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ListingType.values().take(3).forEach { type ->
                FilterChip(
                    selected = (selectedType == type),
                    onClick = { selectedType = type },
                    label = { Text(type.label) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ListingType.values().drop(3).forEach { type ->
                FilterChip(
                    selected = (selectedType == type),
                    onClick = { selectedType = type },
                    label = { Text(type.label) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Destination URL
        OutlinedTextField(
            value = destinationUrl,
            onValueChange = { destinationUrl = it },
            label = { Text("Destination URL / Portfolio (optional)") },
            placeholder = { Text("https://yourcompany.com/offer") },
            leadingIcon = { Icon(Icons.Default.Language, contentDescription = null, tint = CyanAccent) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("submit_url_input"),
            singleLine = true
        )

        // Location Info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("City") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("submit_city_input"),
                singleLine = true
            )
            OutlinedTextField(
                value = state,
                onValueChange = { state = it },
                label = { Text("State") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("submit_state_input"),
                singleLine = true
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = isOnlineAvailable,
                onCheckedChange = { isOnlineAvailable = it }
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Available online / remote pan-India",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Contact Info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = contactEmail,
                onValueChange = { contactEmail = it },
                label = { Text("Contact Email") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("submit_email_input"),
                singleLine = true
            )
            OutlinedTextField(
                value = contactPhone,
                onValueChange = { contactPhone = it },
                label = { Text("Phone") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("submit_phone_input"),
                singleLine = true
            )
        }

        // Policy & Declarations
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = termsAccepted,
                        onCheckedChange = { termsAccepted = it },
                        modifier = Modifier.testTag("terms_checkbox")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "I acknowledge the Codelligence Community Guidelines and agree to the 60-day ₹0 fair-use policy.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = accuracyDeclared,
                        onCheckedChange = { accuracyDeclared = it },
                        modifier = Modifier.testTag("accuracy_checkbox")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "I declare that I own or represent this organization, and that no misleading, deceptive, or unlawful claims are made.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = CoralError,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        // Preview Toggle
        OutlinedButton(
            onClick = { showPreview = !showPreview },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = if (showPreview) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (showPreview) "Hide Live Card Preview" else "View Live Card Preview")
        }

        if (showPreview) {
            val previewListing = Listing(
                businessId = currentBusiness?.id ?: "biz_preview",
                businessName = currentBusiness?.name ?: "Your Business Name",
                title = if (title.isBlank()) "Your Listing Title Here" else title,
                description = if (description.isBlank()) "Detailed promotion description will display here." else description,
                category = selectedCategory,
                listingType = selectedType,
                destinationUrl = destinationUrl.ifBlank { null },
                city = city.ifBlank { null },
                state = state.ifBlank { null },
                isOnlineAvailable = isOnlineAvailable,
                status = ListingStatus.PENDING_REVIEW
            )

            ListingCard(
                listing = previewListing,
                isSaved = false,
                onCardClick = {},
                onSaveToggle = {},
                onShareClick = {}
            )
        }

        // Submit Button
        Button(
            onClick = {
                if (title.isBlank() || description.isBlank()) {
                    errorMessage = "Title and description are required."
                    return@Button
                }
                if (!termsAccepted || !accuracyDeclared) {
                    errorMessage = "You must accept the terms and declare accuracy before submitting."
                    return@Button
                }

                errorMessage = null
                isSubmitting = true
                viewModel.submitListing(
                    title = title,
                    description = description,
                    category = selectedCategory,
                    listingType = selectedType,
                    destinationUrl = destinationUrl.ifBlank { null },
                    city = city.ifBlank { null },
                    state = state.ifBlank { null },
                    isOnlineAvailable = isOnlineAvailable,
                    contactEmail = contactEmail.ifBlank { null },
                    contactPhone = contactPhone.ifBlank { null }
                )
                isSubmitting = false
                onSubmissionSuccess()
            },
            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("submit_ad_button")
        ) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Submit for Free Review (₹0)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}
