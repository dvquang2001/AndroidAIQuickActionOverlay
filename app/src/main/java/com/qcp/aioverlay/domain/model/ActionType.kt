package com.qcp.aioverlay.domain.model

enum class ActionType(val label: String, val prompt: String) {
    TRANSLATE(
        label = "Translate",
        prompt = "Translate the following text to English. Return only the translation:\n\n"
    ),
    SUMMARIZE(
        label = "Summarize",
        prompt = "Summarize the following text concisely in English:\n\n"
    ),
    EXPLAIN(
        label = "Explain",
        prompt = "Improve and rewrite the following text to be clearer and more professional:\n\n"
    ),
    CUSTOM(
        label = "Custom",
        prompt = ""
    )
}