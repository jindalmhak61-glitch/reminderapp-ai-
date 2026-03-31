package com.shruti.reminderapp.Utils

class LipSyncProcessor {

    // Mapping phonemes → morph targets
    fun phonemeToViseme(phoneme: String): Map<Int, Float> {
        return when (phoneme.lowercase()) {

            "a", "aa", "ah" -> mapOf(0 to 1.0f)       // viseme A
            "o", "oh" -> mapOf(1 to 1.0f)            // viseme O
            "m", "mm" -> mapOf(2 to 1.0f)            // viseme M (closed lips)

            else -> emptyMap()
        }
    }
}
