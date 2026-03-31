package com.shruti.reminderapp.Utils

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import org.json.JSONObject

object Chatbot {

    fun escapeText(text: String): String {
        return JSONObject.quote(text)
    }

    private val geminiModel by lazy {
        GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = "AIzaSyBXoUCJOUZf955xvmUVALhHp2cX5aNEQqk" // You can replace with a secure storage mechanism
        )
    }

    // Fast suspend function for generating text
    suspend fun getGeminiTextResponse(prompt: String,onSuccess:(String)-> Unit) {
            try {
                val response = geminiModel.generateContent(
                    content {
                        text(prompt)
                    }
                )
                response.text ?: "No response from Gemini"

                val result = escapeText(response.text.toString())
                onSuccess(result)
            } catch (e: Exception) {
                e.printStackTrace()
                "Error: ${e.message}"
                onSuccess("Unable to fetch response!")
            }

    }
}