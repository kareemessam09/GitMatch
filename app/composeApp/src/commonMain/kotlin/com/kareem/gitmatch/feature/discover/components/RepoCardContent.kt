package com.kareem.gitmatch.feature.discover.components

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Stars
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.kareem.gitmatch.core.model.FeedCard
import com.kareem.gitmatch.core.theme.Amber
import com.kareem.gitmatch.core.theme.CodeStyle
import com.kareem.gitmatch.core.theme.Indigo
import com.kareem.gitmatch.core.theme.ZincBorder

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RepoCardContent(
    card: FeedCard,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Image header — shown when image URL is available, skipped otherwise
        if (!card.imageUrl.isNullOrEmpty()) {
            AsyncImage(
                model = card.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            )
        } else {
            // Placeholder bar when no image is available
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Folder,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Repository",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
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
        // Header: Star count + Language badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = "Stars",
                    modifier = Modifier.size(18.dp),
                    tint = Amber
                )
                Text(
                    text = " ${formatStarCount(card.starCount)}",
                    style = CodeStyle,
                    color = Amber,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = " stars",
                    style = MaterialTheme.typography.titleMedium,
                    color = Amber,
                    fontWeight = FontWeight.Bold
                )
            }
            card.language?.let { lang ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = lang,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Title (owner/name)
        Text(
            text = card.title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        if (card.subtitle.isNotEmpty()) {
            Text(
                text = card.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Stats row: forks, open issues, license
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            card.forksCount?.let { forks ->
                if (forks > 0) {
                    StatChip(label = "$forks forks")
                }
            }
            card.openIssuesCount?.let { issues ->
                if (issues > 0) {
                    StatChip(label = "$issues issues")
                }
            }
            card.licenseName?.let { license ->
                if (license.isNotEmpty() && license != "NOASSERTION") {
                    StatChip(label = license)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // AI Summary
        if (card.summary.isNotEmpty()) {
            Text(
                text = "\u201C${card.summary}\u201D",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                overflow = TextOverflow.Ellipsis
            )
        }

            Spacer(modifier = Modifier.height(8.dp))





        Spacer(modifier = Modifier.height(4.dp))

        // Topics tags
        if (card.topics.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                card.topics.take(5).forEach { topic ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = topic,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Good First Issues badge
        if (card.hasGoodFirstIssues) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Indigo.copy(alpha = 0.15f),
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
        } // end inner Column
    } // end outer Column
}

@Composable
private fun StatChip(label: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, ZincBorder)
    ) {
        Text(
            text = label,
            style = CodeStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

private fun formatStarCount(count: Int?): String {
    if (count == null) return "0"
    return when {
        count >= 1000 -> "${count / 1000}.${(count % 1000) / 100}k"
        else -> count.toString()
    }
}
