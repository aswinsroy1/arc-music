with open('app/src/main/java/com/example/LibraryComponents.kt', 'r') as f:
    content = f.read()

old_code = """
@Composable
fun LibraryMainSection(modifier: Modifier = Modifier, tabName: String = "", onNavigateToAlbumDetails: () -> Unit = {}, onNavigateToPlaylistDetails: () -> Unit = {}) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.55f))
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { }) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Date added",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }"""

new_code = """
@Composable
fun LibraryMainSection(modifier: Modifier = Modifier, tabName: String = "", onNavigateToAlbumDetails: () -> Unit = {}, onNavigateToPlaylistDetails: () -> Unit = {}) {
    var sortExpanded by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf("Date added") }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.55f))
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                }"""

content = content.replace(old_code.strip(), new_code.strip())

with open('app/src/main/java/com/example/LibraryComponents.kt', 'w') as f:
    f.write(content)
