package com.kareem.gitmatch.feature.discover.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kareem.gitmatch.core.model.FeedTab

@Composable
fun FeedTabRow(
    selectedTab: FeedTab,
    onTabSelected: (FeedTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = FeedTab.entries
    val selectedIndex = tabs.indexOf(selectedTab)

    PrimaryTabRow(
        selectedTabIndex = selectedIndex,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        tabs.forEach { tab ->
            Tab(
                selected = tab == selectedTab,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            )
        }
    }
}
