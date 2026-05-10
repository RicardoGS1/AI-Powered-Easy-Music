package com.virtualworld.easymusic.data.remote.gemini

import com.google.gson.annotations.SerializedName

data class GeminiGenerateRequest(
    @SerializedName("contents") val contents: List<GeminiContentBlock>,
    @SerializedName("generationConfig") val generationConfig: GeminiGenerationConfig? = null
)

data class GeminiContentBlock(
    @SerializedName("role") val role: String? = "user",
    @SerializedName("parts") val parts: List<GeminiTextPart>
)

data class GeminiTextPart(
    @SerializedName("text") val text: String
)

data class GeminiGenerationConfig(
    @SerializedName("temperature") val temperature: Float = 0.35f,
    @SerializedName("maxOutputTokens") val maxOutputTokens: Int = 768,
    @SerializedName("responseMimeType") val responseMimeType: String = "application/json"
)

data class GeminiGenerateResponse(
    @SerializedName("candidates") val candidates: List<GeminiCandidate>? = null,
    @SerializedName("error") val error: GeminiApiErrorBody? = null
)

data class GeminiCandidate(
    @SerializedName("content") val content: GeminiCandidateContent? = null
)

data class GeminiCandidateContent(
    @SerializedName("parts") val parts: List<GeminiTextPart>? = null
)

data class GeminiApiErrorBody(
    @SerializedName("message") val message: String? = null,
    @SerializedName("status") val status: String? = null
)
