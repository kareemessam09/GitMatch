package com.kareem.gitmatch.feature.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.ForkRight
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.GpsFixed
import androidx.compose.material.icons.rounded.Newspaper
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Stars
import androidx.compose.material.icons.rounded.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.kareem.gitmatch.core.model.FeedCard
import com.kareem.gitmatch.core.model.FeedItemType
import com.kareem.gitmatch.core.theme.Amber
import com.kareem.gitmatch.core.theme.CodeStyle
import com.kareem.gitmatch.core.theme.Indigo
import com.kareem.gitmatch.core.theme.ZincBorder

/**
 * Gets a favicon URL from a source URL.
 * Uses Google's faviconV2 service for high-quality icons.
 */
private fun getFaviconUrl(sourceUrl: String): String {
    val domain = sourceUrl
        .removePrefix("https://")
        .removePrefix("http://")
        .substringBefore("/")
    return "https://t3.gstatic.com/faviconV2?client=SOCIAL&type=FAVICON&fallback_opts=TYPE,SIZE,URL&url=https://$domain&size=256"
}

@Composable
fun DetailScreen(
    uiState: DetailUiState,
    onBack: () -> Unit,
    onOpenUrl: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = "Details",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
        }

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            uiState.card != null -> {
                DetailContent(
                    card = uiState.card,
                    onOpenUrl = onOpenUrl
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailContent(
    card: FeedCard,
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Hero image section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            if (!card.imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = card.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Gradient scrim
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.8f)
                                ),
                                startY = 100f
                            )
                        )
                )
            } else {
                // Fallback with favicon
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = getFaviconUrl(card.sourceUrl),
                        contentDescription = "Source logo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                }
            }

            // Type badge overlay (top-left)
            Surface(
                modifier = Modifier
                    .padding(12.dp)
                    .align(Alignment.TopStart),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                border = BorderStroke(1.dp, ZincBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (card.type == FeedItemType.REPO) Icons.Rounded.Folder else Icons.Rounded.Newspaper,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (card.type == FeedItemType.REPO) "REPOSITORY" else "NEWS",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Content section
        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Title
            Text(
                text = card.title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )

            if (card.subtitle.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = card.subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Stats row (for repos)
            if (card.type == FeedItemType.REPO) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    card.starCount?.let { stars ->
                        StatPill(
                            icon = Icons.Rounded.Star,
                            value = formatStarCount(stars),
                            color = Amber
                        )
                    }
                    card.forksCount?.let { forks ->
                        if (forks > 0) {
                            StatPill(
                                icon = Icons.Rounded.ForkRight,
                                value = "$forks",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    card.openIssuesCount?.let { issues ->
                        if (issues > 0) {
                            StatPill(
                                icon = Icons.Rounded.BugReport,
                                value = "$issues",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Language + license + good first issues
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    card.language?.let { lang ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        ) {
                            Text(
                                text = lang,
                                style = CodeStyle,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                    card.licenseName?.let { license ->
                        if (license.isNotEmpty() && license != "NOASSERTION") {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, ZincBorder)
                            ) {
                                Text(
                                    text = license,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                    if (card.hasGoodFirstIssues) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Indigo.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, Indigo.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Stars,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = Indigo
                                )
                                Text(
                                    text = "Good First Issues",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Indigo,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = ZincBorder)
            Spacer(modifier = Modifier.height(24.dp))

            // AI Summary card
            if (card.summary.isNotEmpty()) {
                SectionCard(
                    title = "AI Summary",
                    icon = Icons.Rounded.SmartToy
                ) {
                    Text(
                        text = card.summary,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Key Points
            if (card.bullets.isNotEmpty()) {
                // Bullets arrive as a single pipe-delimited string
                val bulletItems = card.bullets
                    .split("|")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }

                if (bulletItems.isNotEmpty()) {
                    SectionCard(
                        title = "Key Points",
                        icon = Icons.Rounded.VpnKey
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            bulletItems.forEachIndexed { index, bullet ->
                                if (index > 0) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    // Bullet dot
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 8.dp)
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = bullet,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Why it's interesting
            card.whyInteresting?.let { why ->
                if (why.isNotEmpty()) {
                    SectionCard(
                        title = "Why It\u2019s Interesting",
                        icon = Icons.Rounded.AutoAwesome
                    ) {
                        Text(
                            text = why,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Ideal for
            card.idealFor?.let { ideal ->
                if (ideal.isNotEmpty()) {
                    SectionCard(
                        title = "Ideal For",
                        icon = Icons.Rounded.GpsFixed
                    ) {
                        Text(
                            text = ideal,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Topics
            if (card.topics.isNotEmpty()) {
                Text(
                    text = "Topics",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    card.topics.forEach { topic ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f))
                        ) {
                            Text(
                                text = topic,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            HorizontalDivider(color = ZincBorder)
            Spacer(modifier = Modifier.height(24.dp))

            // Action button
            val isRepo = card.type == FeedItemType.REPO

            Button(
                onClick = { onOpenUrl(card.sourceUrl) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRepo) MaterialTheme.colorScheme.onBackground else Indigo
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isRepo) Icons.Rounded.Code else Icons.AutoMirrored.Rounded.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (isRepo) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = if (isRepo) "Open on GitHub" else "Read Article",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isRepo) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * A styled section card for grouping content — flat with border.
 */
@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(1.dp, ZincBorder)
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

/**
 * Pill-shaped stat display with Material Icon (stars, forks, issues).
 */
@Composable
private fun StatPill(
    icon: ImageVector,
    value: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = color
            )
            Text(
                text = value,
                style = CodeStyle,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun formatStarCount(count: Int): String {
    return when {
        count >= 1000 -> "${count / 1000}.${(count % 1000) / 100}k"
        else -> count.toString()
    }
}
