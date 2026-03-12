package com.kareem.gitmatch.feature.vault.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.kareem.gitmatch.core.model.FeedItemType
import com.kareem.gitmatch.core.theme.Amber
import com.kareem.gitmatch.core.theme.CodeStyle
import com.kareem.gitmatch.core.theme.Indigo
import com.kareem.gitmatch.core.theme.ZincBorder

/**
 * Gets a favicon URL for fallback when no image is available.
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
fun SavedCardItem(
    card: FeedCard,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, ZincBorder),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail image
            val imageUrl = card.imageUrl
            if (!imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                )
            } else {
                // Fallback: favicon or placeholder
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = getFaviconUrl(card.sourceUrl),
                        contentDescription = "Source logo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }

            // Text content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Type badge row
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (card.type == FeedItemType.REPO) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        } else {
                            Indigo.copy(alpha = 0.15f)
                        },
                        border = BorderStroke(1.dp,
                            if (card.type == FeedItemType.REPO)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else Indigo.copy(alpha = 0.2f)
                        )
                    ) {
                        Text(
                            text = if (card.type == FeedItemType.REPO) "REPO" else "NEWS",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (card.type == FeedItemType.REPO) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                Indigo
                            },
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (card.type == FeedItemType.REPO) {
                        Spacer(modifier = Modifier.width(8.dp))
                        card.starCount?.let { stars ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Star,
                                    contentDescription = "Stars",
                                    modifier = Modifier.size(12.dp),
                                    tint = Amber
                                )
                                Text(
                                    text = "$stars",
                                    style = CodeStyle,
                                    color = Amber,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        card.language?.let { lang ->
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = lang,
                                style = CodeStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Title
                Text(
                    text = card.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Summary
                if (card.summary.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = card.summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
