package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.ChatMessage
import com.example.data.api.GeminiPersona
import com.example.data.api.GeminiService
import com.example.ui.components.ApiKeyManagementDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun ChatbotScreen(
    viewModel: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var selectedPersona by remember { mutableStateOf(GeminiPersona.GENERAL) }
    var inputText by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var showApiKeyDialog by remember { mutableStateOf(false) }

    val hasKey = GeminiService.hasValidApiKey()

    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                text = "Welcome to Codelligence AI Assistant. I can help you draft compelling promotional listings, check compliance with our ₹0 fair-use policy, optimize city reach with Google Maps grounding, or assist with Bioscience & Tech ventures. Select a specialized advisor above to get started!",
                isUser = false
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("gemini_chatbot_screen")
    ) {
        // Persona Selector Chips & Key Access Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Advisor Role & AI Model:",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    AssistChip(
                        onClick = { showApiKeyDialog = true },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.VpnKey,
                                contentDescription = "API Key",
                                modifier = Modifier.size(13.dp),
                                tint = if (hasKey) NeonMint else CyanAccent
                            )
                        },
                        label = {
                            Text(
                                text = if (hasKey) "API Key Active" else "API Key & Access",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (hasKey) NeonMint else CyanAccent
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.testTag("chatbot_api_key_btn")
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GeminiPersona.values().forEach { persona ->
                        val isSelected = selectedPersona == persona
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedPersona = persona },
                            label = {
                                Column {
                                    Text(persona.title, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Text(
                                        text = "${persona.model} ${if (persona.groundingType != "none") "• ${persona.groundingType.uppercase()}" else ""}",
                                        fontSize = 9.sp,
                                        color = if (isSelected) CyanAccent else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricBlue.copy(alpha = 0.25f),
                                selectedLabelColor = CyanAccent
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                selectedBorderColor = CyanAccent,
                                borderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                }
            }
        }

        // Active Persona Description
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (selectedPersona.groundingType) {
                        "search" -> Icons.Default.Search
                        "maps" -> Icons.Default.Place
                        else -> Icons.Default.Psychology
                    },
                    contentDescription = null,
                    tint = CyanAccent,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = selectedPersona.description,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant),
                    maxLines = 1
                )
            }
        }

        // Message List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatBubble(message = msg)
            }

            if (isGenerating) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 6.dp)
                    ) {
                        CircularProgressIndicator(
                            color = CyanAccent,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${selectedPersona.title} is thinking (${selectedPersona.model})...",
                            style = MaterialTheme.typography.labelSmall.copy(color = CyanAccent)
                        )
                    }
                }
            }
        }

        // Quick Suggestion Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val suggestions = when (selectedPersona) {
                GeminiPersona.BIOSCIENCE -> listOf(
                    "Draft biotech internship ad",
                    "Bioinformatics workshop outline",
                    "Clinical research compliance"
                )
                GeminiPersona.LOCAL_MAPS -> listOf(
                    "High-footfall tech hubs in Bengaluru",
                    "Local services visibility tactics",
                    "Pune startup co-working zones"
                )
                GeminiPersona.STRATEGY_PRO -> listOf(
                    "Verify terms compliance for my ad",
                    "Competitor positioning review",
                    "Anti-fraud internship policy"
                )
                GeminiPersona.FAST_COPY -> listOf(
                    "Generate 3 catchy ad headlines",
                    "Write 2-sentence value prop",
                    "Suggest 5 search keywords"
                )
                else -> listOf(
                    "How to maximize free reach at ₹0?",
                    "Optimize listing description",
                    "Best practices for tech service ads"
                )
            }

            suggestions.forEach { prompt ->
                SuggestionChip(
                    onClick = {
                        inputText = prompt
                    },
                    label = { Text(prompt, fontSize = 10.sp) }
                )
            }
        }

        // Input Field and Send Button
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ask ${selectedPersona.title}...") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    shape = RoundedCornerShape(20.dp),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                IconButton(
                    onClick = {
                        val userText = inputText.trim()
                        if (userText.isNotBlank() && !isGenerating) {
                            inputText = ""
                            val userMsg = ChatMessage(text = userText, isUser = true)
                            messages.add(userMsg)
                            isGenerating = true

                            coroutineScope.launch {
                                listState.animateScrollToItem(messages.size - 1)
                                val result = GeminiService.sendMessage(
                                    history = messages.dropLast(1),
                                    userMessage = userText,
                                    persona = selectedPersona
                                )

                                isGenerating = false
                                result.onSuccess { reply ->
                                    messages.add(reply)
                                    listState.animateScrollToItem(messages.size - 1)
                                }.onFailure { error ->
                                    messages.add(
                                        ChatMessage(
                                            text = "Error connecting to Gemini: ${error.localizedMessage}",
                                            isUser = false
                                        )
                                    )
                                    listState.animateScrollToItem(messages.size - 1)
                                }
                            }
                        }
                    },
                    enabled = inputText.isNotBlank() && !isGenerating,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (inputText.isNotBlank() && !isGenerating) ElectricBlue else MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("send_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (inputText.isNotBlank() && !isGenerating) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    if (showApiKeyDialog) {
        ApiKeyManagementDialog(
            viewModel = viewModel,
            onDismiss = { showApiKeyDialog = false }
        )
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.isUser

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(ElectricBlue, CyanAccent))),
                contentAlignment = Alignment.Center
            ) {
                Text("G", color = NavyBlack, fontWeight = FontWeight.Black, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            color = if (isUser) ElectricBlue else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (isUser) 14.dp else 2.dp,
                bottomEnd = if (isUser) 2.dp else 14.dp
            ),
            border = if (!isUser) ButtonDefaults.outlinedButtonBorder() else null,
            modifier = Modifier.widthIn(max = 290.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )
                )

                if (message.groundingSources.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sources: ${message.groundingSources.joinToString(", ")}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            color = CyanAccent
                        )
                    )
                }
            }
        }
    }
}
