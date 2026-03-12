package com.kareem.gitmatch.feature.discover.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.kareem.gitmatch.core.model.FeedCard
import com.kareem.gitmatch.core.theme.Indigo

/**
 * Extracts a favicon/logo URL from a website URL.
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
fun NewsCardContent(
    card: FeedCard,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Image header — article image or website logo fallback
        if (!card.imageUrl.isNullOrEmpty()) {
            AsyncImage(
                model = card.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
        } else {
            // Fallback: show website favicon/logo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = getFaviconUrl(card.sourceUrl),
                    contentDescription = "Source logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                )
            }
        }

        // Card text content
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
        // Header: Type badge + Date
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (card.isReleaseNote) {
                    Indigo.copy(alpha = 0.15f)
                } else {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                }
            ) {
                Text(
                    text = if (card.isReleaseNote) "RELEASE" else "NEWS",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (card.isReleaseNote) Indigo else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            card.publishedAt?.let { date ->
                Text(
                    text = formatDisplayDate(date),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Source row: favicon + domain name
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = getFaviconUrl(card.sourceUrl),
                contentDescription = "Source favicon",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = card.sourceUrl
                    .removePrefix("https://")
                    .removePrefix("http://")
                    .substringBefore("/"),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Title
        Text(
            text = card.title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(20.dp))

        // AI 3-bullet TL;DR
        card.bullets
            .split("|")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .forEach { bullet ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "\u2022 ",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = bullet,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Source info
        if (card.subtitle.isNotEmpty()) {
            Text(
                text = card.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        } // end inner Column
    } // end outer Column
}

private fun formatDisplayDate(isoDate: String): String {
    // Simple date extraction from ISO 8601 string
    return isoDate.take(10)
}
