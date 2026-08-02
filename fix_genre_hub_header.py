import re

with open('app/src/main/java/com/example/GenreHubScreen.kt', 'r') as f:
    content = f.read()

# Add Header to GenreHubScreenContent
old_lazy_column = """    LazyColumn(
        contentPadding = PaddingValues(top = 24.dp, bottom = bottomPadding + 24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        modifier = modifier.fillMaxSize()
    ) {
        item {
            GenreHeroSection(genreName = genreName, modifier = Modifier.padding(horizontal = 24.dp))
        }"""
new_lazy_column = """    LazyColumn(
        contentPadding = PaddingValues(top = 24.dp, bottom = bottomPadding + 24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        modifier = modifier.fillMaxSize()
    ) {
        item {
            Header(
                modifier = Modifier.padding(horizontal = 24.dp),
                onSettingsClick = { },
                onBackClick = onNavigateBack
            )
        }
        item {
            GenreHeroSection(genreName = genreName, modifier = Modifier.padding(horizontal = 24.dp))
        }"""
content = content.replace(old_lazy_column, new_lazy_column)

with open('app/src/main/java/com/example/GenreHubScreen.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    main_content = f.read()

old_header = """@Composable
fun Header(modifier: Modifier = Modifier, onSettingsClick: () -> Unit = {}) {

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { /*TODO*/ }) {"""
new_header = """@Composable
fun Header(modifier: Modifier = Modifier, onSettingsClick: () -> Unit = {}, onBackClick: () -> Unit = { /*TODO*/ }) {

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {"""
main_content = main_content.replace(old_header, new_header)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(main_content)
