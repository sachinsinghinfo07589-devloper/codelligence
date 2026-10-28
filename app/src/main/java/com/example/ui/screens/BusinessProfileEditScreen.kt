package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.BusinessProfile
import com.example.ui.theme.ElectricBlue
import com.example.ui.viewmodel.MainViewModel

@Composable
fun BusinessProfileEditScreen(
    viewModel: MainViewModel,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentBusiness by viewModel.currentBusiness.collectAsState()
    val scrollState = rememberScrollState()

    var name by remember { mutableStateOf(currentBusiness?.name ?: "") }
    var description by remember { mutableStateOf(currentBusiness?.shortDescription ?: "") }
    var category by remember { mutableStateOf(currentBusiness?.category ?: "Software and web development") }
    var websiteUrl by remember { mutableStateOf(currentBusiness?.websiteUrl ?: "") }
    var publicEmail by remember { mutableStateOf(currentBusiness?.publicEmail ?: "") }
    var publicPhone by remember { mutableStateOf(currentBusiness?.publicPhone ?: "") }
    var city by remember { mutableStateOf(currentBusiness?.city ?: "") }
    var state by remember { mutableStateOf(currentBusiness?.state ?: "") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp, 16.dp, 16.dp, 80.dp)
            .testTag("business_profile_edit_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Edit Business Profile",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Public contact information and business description displayed on your promotional cards.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Business / Organization Name *") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("biz_name_input")
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Short Description / Tagline *") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .testTag("biz_desc_input")
        )

        OutlinedTextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Primary Business Category") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = websiteUrl,
            onValueChange = { websiteUrl = it },
            label = { Text("Official Website URL") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = publicEmail,
                onValueChange = { publicEmail = it },
                label = { Text("Public Contact Email") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = publicPhone,
                onValueChange = { publicPhone = it },
                label = { Text("Public Phone Number") },
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("City") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = state,
                onValueChange = { state = it },
                label = { Text("State") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                val updated = (currentBusiness ?: BusinessProfile(
                    userId = "usr_01",
                    name = name,
                    shortDescription = description,
                    category = category
                )).copy(
                    name = name,
                    shortDescription = description,
                    category = category,
                    websiteUrl = websiteUrl.ifBlank { null },
                    publicEmail = publicEmail.ifBlank { null },
                    publicPhone = publicPhone.ifBlank { null },
                    city = city.ifBlank { null },
                    state = state.ifBlank { null }
                )
                viewModel.updateBusinessProfile(updated)
                onSaved()
            },
            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("save_business_profile_btn")
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Business Profile", fontWeight = FontWeight.Bold)
        }
    }
}
