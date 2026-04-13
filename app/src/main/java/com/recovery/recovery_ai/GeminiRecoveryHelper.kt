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
                    A user has a $risk injury risk after a workout.
                    Give exactly 3 short recovery suggestions.
                    The 3 suggestions must be:
                    1 recovery movement suggestion
                    1 hydration suggestion
                    1 rest or sleep suggestion
                    
                    Return each suggestion on a new line.
                    Do NOT use bullets.
                    Do NOT use numbering.
                    Keep it educational and non-diagnostic.
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