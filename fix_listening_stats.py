import re

content = open("app/src/main/java/com/example/MainActivity.kt").read()

old_stats = """fun ListeningStatsSection(modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        Text(
            text = "Listening Stats",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        
        GlassCard(
            modifier = Modifier.padding(horizontal = 24.dp).clickable { onClick() }
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Artist
                Column {
                    Text(
                        text = "TOP ARTIST",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = "https://lh3.googleusercontent.com/aida-public/AB6AXuDcxr5OkSQfpI_jTkSInZTLTIQNPElvx4VTAwf6InyR5cV2DD4SLzOgYsBC1gNArokFiZMFSwmKVi6VW-OeV6ouanmXDcfN4aD-RtGJFuMNyYZTx5P6VkXi-b4eY5GWUNpAaGeTkiqgkdzS6Of-mtUzJt7rz9IYbGhj7V3IcTi8iHjlof7t5fJzN09WsP72jlTq2o-VEsgIRAPXzreisxiQKK8kmsYEbFlDl442gyzxMfa0UGT2M3aJ5eafCHY0tM_wkFed6Lty8vDU",
                            contentDescription = "Conan Gray",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Conan Gray",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                // Favorite Genre
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "FAVORITE GENRE",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Indie Pop",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }"""

new_stats = """fun ListeningStatsSection(modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    val viewModel: MusicViewModel = viewModel()
    val topArtists by viewModel.genreTopArtists.collectAsState()
    val topArtist = topArtists.firstOrNull()

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        Text(
            text = "Listening Stats",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        
        GlassCard(
            modifier = Modifier.padding(horizontal = 24.dp).clickable { onClick() }
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Artist
                Column {
                    Text(
                        text = "TOP ARTIST",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = topArtist?.imageUrl ?: "",
                            contentDescription = topArtist?.artist ?: "Unknown",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = topArtist?.artist ?: "Unknown",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                // Favorite Genre
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "FAVORITE GENRE",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = topArtist?.title ?: "Unknown", // we store genre string in title for top artists in seed data
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }"""

content = content.replace(old_stats, new_stats)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
