with open('app/src/main/java/com/example/LibraryComponents.kt', 'r') as f:
    content = f.read()

import_code = "import androidx.compose.foundation.border\nimport androidx.compose.ui.unit.sp\n"

if "import androidx.compose.foundation.border" not in content:
    content = content.replace("import androidx.compose.ui.unit.dp\n", "import androidx.compose.ui.unit.dp\n" + import_code)


old_main_section = """
fun LibraryMainSection(modifier: Modifier = Modifier, tabName: String = "", onNavigateToAlbumDetails: () -> Unit = {}, onNavigateToPlaylistDetails: () -> Unit = {}, onNavigateToArtistDetails: () -> Unit = {}) {
    var sortExpanded by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf("Date added") }
    var isGridView by remember { mutableStateOf(true) }

    Box(
"""

new_main_section = """
fun LibraryMainSection(modifier: Modifier = Modifier, tabName: String = "", onNavigateToAlbumDetails: () -> Unit = {}, onNavigateToPlaylistDetails: () -> Unit = {}, onNavigateToArtistDetails: () -> Unit = {}) {
    var sortExpanded by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf("Date added") }
    var sortOrder by remember { mutableStateOf("Descending") }
    var isGridView by remember { mutableStateOf(true) }

    if (sortExpanded) {
        SortBottomSheet(
            onDismissRequest = { sortExpanded = false },
            currentSortOption = sortOption,
            currentSortOrder = sortOrder,
            onApply = { option, order ->
                sortOption = option
                sortOrder = order
                sortExpanded = false
            }
        )
    }

    Box(
"""

old_dropdown_box = """
                    Box {
                        IconButton(onClick = { sortExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Sort by",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        DropdownMenu(
                            expanded = sortExpanded,
                            onDismissRequest = { sortExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Date added") },
                                onClick = {
                                    sortOption = "Date added"
                                    sortExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Name") },
                                onClick = {
                                    sortOption = "Name"
                                    sortExpanded = false
                                }
                            )
                        }
                    }
"""

new_dropdown_box = """
                    IconButton(onClick = { sortExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Sort by",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
"""


sort_bottom_sheet_components = """

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortBottomSheet(
    onDismissRequest: () -> Unit,
    currentSortOption: String,
    currentSortOrder: String,
    onApply: (String, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var sortOption by remember { mutableStateOf(currentSortOption) }
    var sortOrder by remember { mutableStateOf(currentSortOrder) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sort by",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            SortOptionRow(
                title = "Name",
                icon = Icons.Default.SortByAlpha,
                isSelected = sortOption == "Name",
                onClick = { sortOption = "Name" }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SortOptionRow(
                title = "Date added",
                icon = Icons.Default.DateRange,
                isSelected = sortOption == "Date added",
                onClick = { sortOption = "Date added" }
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                text = "ORDER",
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SortOrderButton(
                    title = "Ascending",
                    icon = Icons.Default.ArrowUpward,
                    isSelected = sortOrder == "Ascending",
                    onClick = { sortOrder = "Ascending" },
                    modifier = Modifier.weight(1f)
                )
                SortOrderButton(
                    title = "Descending",
                    icon = Icons.Default.ArrowDownward,
                    isSelected = sortOrder == "Descending",
                    onClick = { sortOrder = "Descending" },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = { onApply(sortOption, sortOrder) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = "Apply Sorting",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
fun SortOptionRow(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (isSelected) MaterialTheme.colorScheme.surfaceVariant else androidx.compose.ui.graphics.Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (isSelected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent)
                .border(
                    width = 2.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun SortOrderButton(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f))
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
    }
}
"""

content = content.replace(old_main_section.strip(), new_main_section.strip())
content = content.replace(old_dropdown_box.strip(), new_dropdown_box.strip())

content += sort_bottom_sheet_components

with open('app/src/main/java/com/example/LibraryComponents.kt', 'w') as f:
    f.write(content)

