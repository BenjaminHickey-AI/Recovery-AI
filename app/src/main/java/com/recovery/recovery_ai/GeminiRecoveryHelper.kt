package com.recovery.recovery_ai

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend

object GeminiRecoveryHelper {
    fun getRecoveryAdvice(
        risk: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prompt = """
                You are a fitness recovery coach.

                User injury risk level: $risk

                If risk is HIGH:
                - focus on reducing strain and recovery, stress recovery and focus on recovery

                If risk is MEDIUM:
                - balance recovery and light activity

                If risk is LOW:
                - focus on optimizing performance and maintaining recovery

                Return a recovery plan in this format:
                
                MOBILITY:
                <short instruction that are clear and concise>
                
                HYDRATION:
                <short instruction>
                
                REST:
                <short instruction>
                
                RETURN:
                <when to train again safely>
                
                WARNING:
                <1 short sentence>
                
                Keep it clean, short, helpful, concise, and non-medical.
                No extra text.
                """.trimIndent()

                val model = Firebase.ai(
                    backend = GenerativeBackend.googleAI()
                ).generativeModel("gemini-2.5-flash")

                val response = model.generateContent(prompt)

                val suggestionText = response.text ?: "RecoveryAI Coach Is Resting. Hydrate, Rest and Stretch."

                withContext(Dispatchers.Main) {
                    onSuccess(suggestionText)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Gemini error")
                }
            }
        }
    }
}