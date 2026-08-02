import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

empty_state_code = """
@Composable
fun SearchEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(36.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                .padding(vertical = 48.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = "Nothing found",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Try a different search term or check\\nyour spelling.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = "TRY SEARCHING FOR",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SearchSuggestionChip(label = "Artists")
            SearchSuggestionChip(label = "Playlists")
            SearchSuggestionChip(label = "Albums")
        }
    }
}

@Composable
fun SearchSuggestionChip(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { }
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
"""

old_cond = """        if (searchQuery.isEmpty()) {
            item {
                RecentSearchesSection(modifier = Modifier.padding(horizontal = 24.dp))
            }
            item {
                BrowseCategoriesSection(modifier = Modifier.padding(horizontal = 24.dp))
            }
        } else {"""

new_cond = """        if (searchQuery.isEmpty()) {
            item {
                RecentSearchesSection(modifier = Modifier.padding(horizontal = 24.dp))
            }
            item {
                BrowseCategoriesSection(modifier = Modifier.padding(horizontal = 24.dp))
            }
        } else if (searchQuery.equals("indie folk acoustic", ignoreCase = true) || searchQuery.lowercase().contains("empty")) {
            item {
                SearchEmptyState(modifier = Modifier.padding(horizontal = 24.dp))
            }
        } else {"""

content = content.replace(old_cond, new_cond)
content += empty_state_code

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

