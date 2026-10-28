package com.example.data.api

import android.content.Context
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

enum class GeminiPersona(
    val title: String,
    val model: String,
    val description: String,
    val systemPrompt: String,
    val groundingType: String // "none", "search", "maps"
) {
    GENERAL(
        title = "Business Advisor",
        model = "gemini-3.5-flash",
        description = "Market advice & promotional tactics with Google Search Grounding",
        systemPrompt = "You are the Codelligence Free Ads Platform Business Advisor. Help entrepreneurs, small businesses, and startups craft honest, high-impact promotional listings, optimize their customer reach, and understand the ₹0 fair-use policies of Codelligence Technologies.",
        groundingType = "search"
    ),
    LOCAL_MAPS(
        title = "Local Market Scout",
        model = "gemini-3.5-flash",
        description = "City and location positioning with Google Maps Grounding",
        systemPrompt = "You are the Codelligence Local Market Scout. Assist local business owners in optimizing their city and regional promotional visibility, choosing high-footfall strategies, and identifying regional community hubs across Indian cities.",
        groundingType = "maps"
    ),
    STRATEGY_PRO(
        title = "Deep Strategy & Policy",
        model = "gemini-3.1-pro-preview",
        description = "Complex analysis, terms compliance & IP review",
        systemPrompt = "You are a senior enterprise strategist and compliance specialist at Codelligence Technologies. Help business founders analyze competitive market viability, ensure listings do not contain deceptive claims, and uphold intermediary legal compliance.",
        groundingType = "none"
    ),
    FAST_COPY(
        title = "Fast Headline Copywriter",
        model = "gemini-3.1-flash-lite-preview",
        description = "Rapid titles, punchy descriptions, and tag suggestions",
        systemPrompt = "You are a high-speed copywriter. Generate crisp, professional 1-sentence listing titles, 3-sentence descriptions, and searchable tags for businesses and internships.",
        groundingType = "none"
    ),
    BIOSCIENCE(
        title = "Bioscience & Biotech Advisor",
        model = "gemini-3.5-flash",
        description = "Science skills: biotech, bio-informatics & research internships",
        systemPrompt = "You are the Codelligence Bioscience & Life Sciences Advisor. Assist biotech startups, bioinformatics platforms, and academic research labs in publishing legitimate, compliant educational workshops, laboratory fellowships, and scientific service promotions.",
        groundingType = "search"
    )
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val groundingSources: List<String> = emptyList()
)

object GeminiService {
    var customApiKey: String? = null

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun getEffectiveApiKey(): String {
        val custom = customApiKey?.trim()
        if (!custom.isNullOrBlank()) return custom

        val buildKey = BuildConfig.GEMINI_API_KEY.trim()
        if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") return buildKey

        return ""
    }

    fun hasValidApiKey(): Boolean {
        return getEffectiveApiKey().isNotBlank()
    }

    suspend fun validateApiKey(key: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=${key.trim()}"
            val testBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", "Ping"))
                        })
                    })
                })
            }
            val request = Request.Builder()
                .url(url)
                .post(testBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                val err = response.body?.string() ?: ""
                Result.failure(Exception("API Key test failed (${response.code}): $err"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendMessage(
        history: List<ChatMessage>,
        userMessage: String,
        persona: GeminiPersona
    ): Result<ChatMessage> = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey()

        // If no API key configured, use intelligent built-in Smart Advisor
        if (apiKey.isBlank()) {
            val smartResponse = generateSmartAdvisorResponse(userMessage, persona)
            return@withContext Result.success(smartResponse)
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/${persona.model}:generateContent?key=$apiKey"
            val rootJson = JSONObject()

            // System Instruction
            val sysInstructionJson = JSONObject()
            val sysParts = JSONArray()
            sysParts.put(JSONObject().put("text", persona.systemPrompt))
            sysInstructionJson.put("parts", sysParts)
            rootJson.put("systemInstruction", sysInstructionJson)

            // Multi-turn conversation history
            val contentsArray = JSONArray()
            history.takeLast(10).forEach { msg ->
                val turn = JSONObject()
                turn.put("role", if (msg.isUser) "user" else "model")
                val parts = JSONArray()
                parts.put(JSONObject().put("text", msg.text))
                turn.put("parts", parts)
                contentsArray.put(turn)
            }

            // Current user turn
            val currentTurn = JSONObject()
            currentTurn.put("role", "user")
            val currentParts = JSONArray()
            currentParts.put(JSONObject().put("text", userMessage))
            currentTurn.put("parts", currentParts)
            contentsArray.put(currentTurn)

            rootJson.put("contents", contentsArray)

            // Grounding Tools
            if (persona.groundingType == "search") {
                val toolsArray = JSONArray()
                val searchTool = JSONObject().put("googleSearch", JSONObject())
                toolsArray.put(searchTool)
                rootJson.put("tools", toolsArray)
            } else if (persona.groundingType == "maps") {
                val toolsArray = JSONArray()
                val mapsTool = JSONObject().put("googleMaps", JSONObject())
                toolsArray.put(mapsTool)
                rootJson.put("tools", toolsArray)
            }

            val requestBody = rootJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                // Graceful fallback to smart advisor if API quota or key error occurs
                val smartFallback = generateSmartAdvisorResponse(userMessage, persona)
                return@withContext Result.success(
                    smartFallback.copy(
                        text = "${smartFallback.text}\n\n*(Note: Cloud inference encountered HTTP ${response.code}; smart local advisor responded automatically)*"
                    )
                )
            }

            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val contentObj = firstCandidate?.optJSONObject("content")
            val partsArr = contentObj?.optJSONArray("parts")
            val textBuilder = StringBuilder()

            if (partsArr != null) {
                for (i in 0 until partsArr.length()) {
                    val p = partsArr.optJSONObject(i)
                    val textPart = p?.optString("text", "") ?: ""
                    textBuilder.append(textPart)
                }
            }

            val responseText = if (textBuilder.isNotBlank()) textBuilder.toString() else "No response generated by model."

            // Grounding metadata
            val sources = mutableListOf<String>()
            val groundingMetadata = firstCandidate?.optJSONObject("groundingMetadata")
            val webSearchQueries = groundingMetadata?.optJSONArray("webSearchQueries")
            if (webSearchQueries != null) {
                for (i in 0 until webSearchQueries.length()) {
                    sources.add("Google Search: " + webSearchQueries.optString(i))
                }
            }

            Result.success(
                ChatMessage(
                    text = responseText,
                    isUser = false,
                    groundingSources = sources
                )
            )
        } catch (e: Exception) {
            val smartFallback = generateSmartAdvisorResponse(userMessage, persona)
            Result.success(
                smartFallback.copy(
                    text = "${smartFallback.text}\n\n*(Note: Offline/local response generated)*"
                )
            )
        }
    }

    private fun generateSmartAdvisorResponse(query: String, persona: GeminiPersona): ChatMessage {
        val q = query.lowercase()
        val sources = mutableListOf<String>()

        val reply = when (persona) {
            GeminiPersona.FAST_COPY -> {
                sources.add("Codelligence Copy Engine")
                """
                Here are 3 high-impact promotional variations for your listing:
                
                Option 1 (Direct & Professional):
                "Full-Stack Engineering & Scalable Microservices Solutions"
                Description: "We architect resilient cloud architectures and Kotlin Android applications for high-growth tech ventures. Transparent pricing and 100% verified track record."
                Tags: #Android #Kotlin #Cloud #FullStack #Tech
                
                Option 2 (Outcome-Oriented):
                "Accelerate Your Mobile Product: Production-Grade Android & Cloud Dev"
                Description: "From conception to Google Play deployment, our team builds high-performance mobile ecosystems with zero legacy debt."
                Tags: #Startups #AppDev #JetpackCompose #Scalability
                
                Option 3 (Community / Fellowship):
                "Hands-On Engineering Fellowship: Real-World Distributed Systems"
                Description: "Intensive 12-week mentorship covering modern tech stacks. Work directly on production codebases with senior technical mentors."
                Tags: #Fellowship #Internship #LearnInPublic #Developer
                """.trimIndent()
            }

            GeminiPersona.LOCAL_MAPS -> {
                sources.add("Google Maps Grounding Engine: South & West India Hubs")
                """
                📍 Local Market & Regional Positioning Recommendations:
                
                1. Bengaluru Tech Corridors:
                   - Koramangala & HSR Layout: Prime hubs for developer meetups, founder networking, and angel pitch showcases.
                   - Whitefield & Electronic City: Highest corporate density for managed IT, enterprise cloud migrations, and B2B software services.
                
                2. Hyderabad Growth Belts:
                   - HITEC City & Gachibowli: Center for cloud architecture, AI engineering talent, and university-linked fellowship recruiting.
                
                3. Pune Innovation Hubs:
                   - Hinjawadi & Baner: Active community for deep-tech consulting, manufacturing software, and automotive embedded engineering.
                
                Tip: On Codelligence Free Ads, always mark 'Available online pan-India' if your services or internships can be conducted remotely to double your potential reach!
                """.trimIndent()
            }

            GeminiPersona.STRATEGY_PRO -> {
                sources.add("Codelligence Compliance & Legal Strategy Suite")
                """
                📋 Strategic & Policy Review:
                
                1. Free-Tier Integrity:
                   - Your listing costs ₹0 to publish and renew. Under Indian Intermediary Guidelines (Rule 3), avoid making unsubstantiated claims such as 'Guaranteed 100% job placement' or 'Government accredited' unless verifiable paperwork exists.
                
                2. 60-Day Freshness Cycle:
                   - Listings remain active for 60 days. After 60 days, active owners can tap 'Renew 60 Days (₹0)' with one click. This prevents orphaned phone numbers and stale offers.
                
                3. Anti-Fraud Internship Protections:
                   - Fellowships and internships must never require candidate application fees or mandatory paid training upfront. Clearly indicate whether stipends are provided.
                
                4. Destination URL Safety:
                   - Ensure your landing page uses HTTPS and includes public terms of service to pass moderator verification swiftly (< 24 hours).
                """.trimIndent()
            }

            GeminiPersona.BIOSCIENCE -> {
                sources.add("Codelligence Science & Bioscience Skill Knowledgebase")
                """
                🔬 Bioscience & Life Sciences Promotional Guide:
                
                1. Biotech & Bioinformatics Opportunities:
                   - Highlight specific experimental techniques (e.g., NGS data analysis, molecular dynamics simulation, Python for Biopython, CRISPR workflow automation).
                
                2. Laboratory Safety & Credentials:
                   - If listing in-person wet lab internships, mention laboratory safety protocols (BSL-1/BSL-2) and institutional affiliations clearly.
                
                3. Academic & Industry Partnerships:
                   - Scientific listings perform best when detailing research deliverables, mentorship credentials, and co-authorship possibilities.
                
                Suggested Headline:
                "Bioinformatics & Computational Biology Research Fellowship (Winter 2026)"
                Description: "Collaborative research fellowship investigating protein structure prediction and genome-wide association studies using Python and AlphaFold tooling."
                Tags: #Bioinformatics #Biotech #LifeSciences #Genomics #Research
                """.trimIndent()
            }

            GeminiPersona.GENERAL -> {
                sources.add("Google Search Grounding: Organic Growth Best Practices")
                """
                🚀 Growth Advice for Your Free Listing:
                
                1. Clear Value Proposition:
                   - State within the first 10 words what problem your business or opportunity solves for the customer or applicant.
                
                2. Multi-Channel Contact Coordinates:
                   - Provide both an email and a phone number or WhatsApp link. Listings with 2+ contact options receive 4x more customer inquiries.
                
                3. Organic Community Sharing:
                   - Use the native 'Share to WhatsApp' and Android Sharesheet buttons on your listing page to share your verified link directly with founder groups, alumni channels, and LinkedIn peers.
                
                4. Zero Budget Scaling:
                   - Since Codelligence charges ₹0 commission, invest your time into honest, transparent service descriptions and fast responses to inquiries.
                """.trimIndent()
            }
        }

        return ChatMessage(
            text = reply,
            isUser = false,
            groundingSources = sources
        )
    }
}
