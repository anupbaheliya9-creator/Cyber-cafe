package com.example.data.api

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class GeminiResult {
    data class Success(val text: String) : GeminiResult()
    data class Error(val message: String, val isKeyError: Boolean = false) : GeminiResult()
    object KeyMissing : GeminiResult()
}

class GeminiManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("gemini_prefs", Context.MODE_PRIVATE)
    private val keyStorageName = "gemini_api_key"

    private val _hasKey = MutableStateFlow(hasValidKey())
    val hasKey: StateFlow<Boolean> = _hasKey.asStateFlow()

    private val _keyErrorMessage = MutableStateFlow<String?>(null)
    val keyErrorMessage: StateFlow<String?> = _keyErrorMessage.asStateFlow()

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun getStoredApiKey(): String {
        val userKey = prefs.getString(keyStorageName, "")?.trim() ?: ""
        if (userKey.isNotBlank()) return userKey

        // Fallback to BuildConfig if provided via Secrets panel / .env
        val buildKey = try {
            BuildConfig.GEMINI_API_KEY.trim()
        } catch (e: Exception) {
            ""
        }
        return if (buildKey.startsWith("AIzaSy")) buildKey else ""
    }

    fun hasValidKey(): Boolean {
        val key = getStoredApiKey()
        return key.isNotBlank() && key.startsWith("AIzaSy")
    }

    fun getMaskedKey(): String {
        val key = getStoredApiKey()
        return if (key.length >= 10) {
            "${key.take(6)}••••••••${key.takeLast(4)}"
        } else if (key.isNotBlank()) {
            "••••••••"
        } else {
            "कुंजी उपलब्ध नहीं है"
        }
    }

    fun saveApiKey(rawKey: String): Boolean {
        val trimmed = rawKey.trim()
        if (!trimmed.startsWith("AIzaSy")) {
            _keyErrorMessage.value = "अमान्य कुंजी! Gemini API Key 'AIzaSy...' से शुरू होनी चाहिए।"
            return false
        }
        if (trimmed.length < 25) {
            _keyErrorMessage.value = "API Key की लंबाई बहुत कम है। कृपया पूरी कुंजी दर्ज करें।"
            return false
        }

        prefs.edit().putString(keyStorageName, trimmed).apply()
        _hasKey.value = true
        _keyErrorMessage.value = null
        return true
    }

    fun clearApiKey() {
        prefs.edit().remove(keyStorageName).apply()
        _hasKey.value = false
        _keyErrorMessage.value = "कृपया जारी रखने के लिए अपनी Gemini API Key दर्ज करें।"
    }

    suspend fun generateContent(
        prompt: String,
        systemInstruction: String = "आप एक विशेषज्ञ भारतीय साइबर कैफे, कानूनी सलाहकार और सहारा रिफंड AI एजेंट हैं। सटीक, प्रामाणिक और आधिकारिक प्रारूप में उत्तर दें।"
    ): GeminiResult = withContext(Dispatchers.IO) {
        val apiKey = getStoredApiKey()

        // Rule 3: Check for key existence prior to every API call.
        if (apiKey.isBlank() || !apiKey.startsWith("AIzaSy")) {
            _hasKey.value = false
            _keyErrorMessage.value = "कृपया जारी रखने के लिए अपनी Gemini API Key दर्ज करें।"
            return@withContext GeminiResult.KeyMissing
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val jsonBody = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    put(JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        }
                        put("parts", partsArray)
                    })
                }
                put("contents", contentsArray)

                put("systemInstruction", JSONObject().apply {
                    val sysParts = JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", systemInstruction)
                        })
                    }
                    put("parts", sysParts)
                })

                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("maxOutputTokens", 1200)
                })
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""

                // Rule 4: Error Handling for 400, 403 or Invalid Key
                if (response.code == 400 || response.code == 403 || responseBody.contains("API_KEY_INVALID") || responseBody.contains("API key not valid")) {
                    clearApiKey()
                    return@withContext GeminiResult.Error(
                        message = "API Key अमान्य (Invalid) है। कृपया 'AIzaSy...' से शुरू होने वाली वैध Gemini API Key दर्ज करें।",
                        isKeyError = true
                    )
                }

                if (!response.isSuccessful) {
                    return@withContext GeminiResult.Error("API त्रुटि (${response.code}): सर्वर से संपर्क नहीं हो सका।")
                }

                val jsonResponse = JSONObject(responseBody)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val contentObj = firstCandidate.optJSONObject("content")
                    val parts = contentObj?.optJSONArray("parts")
                    val text = parts?.optJSONObject(0)?.optString("text") ?: ""
                    if (text.isNotBlank()) {
                        return@withContext GeminiResult.Success(text)
                    }
                }

                return@withContext GeminiResult.Error("उत्तर प्राप्त नहीं हुआ।")
            }
        } catch (e: Exception) {
            Log.e("GeminiManager", "Request error: ${e.message}")
            return@withContext GeminiResult.Error("नेटवर्क या कनेक्शन त्रुटि: ${e.localizedMessage ?: "अज्ञात समस्या"}")
        }
    }
}
