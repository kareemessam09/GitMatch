package com.kareem.gitmatch.feature.onboarding

data class OnboardingUiState(
    val availableLanguages: List<String> = listOf(
        "Kotlin", "Java", "Python", "Rust", "Go", "TypeScript",
        "Swift", "C++", "C#", "Ruby", "Dart", "JavaScript"
    ),
    val availableTopics: List<String> = listOf(
        "Android", "iOS", "React", "AI/ML", "DevOps", "Web3",
        "Flutter", "Cloud", "Security", "Data Science",
        "Backend", "Frontend", "Mobile", "Game Dev"
    ),
    val selectedLanguages: Set<String> = emptySet(),
    val selectedTopics: Set<String> = emptySet(),
    /** 0 = languages step, 1 = topics step */
    val currentStep: Int = 0,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
) {
    val canProceedFromLanguages: Boolean
        get() = selectedLanguages.isNotEmpty()

    val canContinue: Boolean
        get() = selectedTopics.isNotEmpty()

    val totalSelected: Int
        get() = selectedLanguages.size + selectedTopics.size
}

sealed interface OnboardingIntent {
    data class ToggleLanguage(val language: String) : OnboardingIntent
    data class ToggleTopic(val topic: String) : OnboardingIntent
    data object NextStep : OnboardingIntent
    data object PreviousStep : OnboardingIntent
    data object Continue : OnboardingIntent
}
