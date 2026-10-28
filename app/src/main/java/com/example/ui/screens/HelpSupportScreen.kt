package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.NeonMint

@Composable
fun HelpSupportScreen(modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp, 16.dp, 16.dp, 80.dp)
            .testTag("help_support_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Help & Support Center",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Everything you need to know about publishing and discovering free promotions on Codelligence Free Ads Provider.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // FAQ 1
        FaqCard(
            question = "Is publishing really 100% free with ₹0 hidden charges?",
            answer = "Yes! Codelligence Free Ads Provider was founded on a ₹0 initial budget model. Any legitimate startup, freelancer, educator, local shop, or internship provider can submit unlimited promotions without payment."
        )

        // FAQ 2
        FaqCard(
            question = "How does the 60-day renewal policy work?",
            answer = "Promotions remain active for 60 days. To ensure only active and responsive businesses appear in the public feed, you can easily tap 'Renew 60 Days (₹0)' anytime in your 'My Listings' dashboard."
        )

        // FAQ 3
        FaqCard(
            question = "Why does my listing enter 'Pending Review' first?",
            answer = "To protect the community from malicious phishing, deceptive unpaid training disguised as internships, and fake government certificates, human moderators review every new submission before it goes live."
        )

        // FAQ 4
        FaqCard(
            question = "Is Codelligence affiliated with Google Ads or Meta Ads?",
            answer = "No. Codelligence Free Ads Provider is completely independent and owned by Codelligence Technologies. It does not place ads on external platforms or purchase ad inventory from third parties."
        )

        // Grievance Officer Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Grievance Officer & Support Coordinates",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = CyanAccent)
                )
                Text(
                    text = "Codelligence Technologies Legal & Moderation Team\n" +
                            "Email: support@codelligence.tech\n" +
                            "Intermediary Grievance: grievance@codelligence.tech\n" +
                            "Response SLA: Within 36 hours pursuant to Information Technology (Intermediary Guidelines) Rules.",
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun FaqCard(question: String, answer: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = question,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = answer,
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
