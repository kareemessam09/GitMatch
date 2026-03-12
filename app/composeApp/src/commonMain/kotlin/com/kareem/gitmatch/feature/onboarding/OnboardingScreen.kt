package com.kareem.gitmatch.feature.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import gitmatch.composeapp.generated.resources.Res
import gitmatch.composeapp.generated.resources.logo
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    uiState: OnboardingUiState,
    onIntent: (OnboardingIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(Res.drawable.logo),
            contentDescription = "GitMatch Logo",
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Welcome to GitMatch",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Step indicator
        Text(
            text = "Step ${uiState.currentStep + 1} of 2",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        when (uiState.currentStep) {
            0 -> {
                // Step 1: Choose programming languages
                Text(
                    text = "What languages do you code in?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.availableLanguages.forEach { language ->
                        val isSelected = language in uiState.selectedLanguages
                        FilterChip(
                            selected = isSelected,
                            onClick = { onIntent(OnboardingIntent.ToggleLanguage(language)) },
                            label = {
                                Text(
                                    text = language,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = { onIntent(OnboardingIntent.NextStep) },
                    enabled = uiState.canProceedFromLanguages,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Next",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                if (uiState.selectedLanguages.isEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Select at least 1 language",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            1 -> {
                // Step 2: Choose topics
                Text(
                    text = "What topics interest you?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.availableTopics.forEach { topic ->
                        val isSelected = topic in uiState.selectedTopics
                        FilterChip(
                            selected = isSelected,
                            onClick = { onIntent(OnboardingIntent.ToggleTopic(topic)) },
                            label = {
                                Text(
                                    text = topic,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { onIntent(OnboardingIntent.PreviousStep) },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Back",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Button(
                        onClick = { onIntent(OnboardingIntent.Continue) },
                        enabled = uiState.canContinue && !uiState.isLoading,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(
                                text = "Continue",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }

                if (uiState.selectedTopics.isEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Select at least 1 topic",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
