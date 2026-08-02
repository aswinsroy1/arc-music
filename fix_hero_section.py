import re

content = open("app/src/main/java/com/example/MainActivity.kt").read()

old_hero = """fun HeroSection(
    currentSong: Song?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    onPlayClick: (Song) -> Unit = {}
) {
    if (isPlaying && currentSong != null) {"""

new_hero = """fun HeroSection(
    currentSong: Song?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    onPlayClick: (Song) -> Unit = {}
) {
    val viewModel: MusicViewModel = viewModel()
    val randomPicks by viewModel.randomPicks.collectAsState()

    if (isPlaying && currentSong != null) {"""

content = content.replace(old_hero, new_hero)

old_else = """    } else {
        val calendar = java.util.Calendar.getInstance()
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        
        val suggestedSong = when (hour) {
            in 5..11 -> Song("Sunrise", "Norah Jones", "https://lh3.googleusercontent.com/aida-public/AB6AXuB3R1P9c6U7K_6W9K_6M1G3x4P2t4X1o_8d2N_5O8x5K4W_9K5X5H2O8q_0O_7H4E7F4d8K8W_7W5B2e7K5X9C8N8U7w9V7T2J2R_9H1G8s_0J8V1G5N4M8y5M9J9K_9W_0B8B_6M_6E7C_8W8T9N1C5H8W6J8E3G9W3O9Z5E7T5A7N2M7O3R2T_7M1H9K9K6K8S2R_8P1R2U9N_9T9F9X6J2B9F8J7B1M7H9N_6j3O8S4D1E3R1B8d_0V9G")
            in 12..16 -> Song("Ballad of the Homeschooled Girl", "Olivia Rodrigo", "https://lh3.googleusercontent.com/aida-public/AB6AXuB3R1P9c6U7K_6W9K_6M1G3x4P2t4X1o_8d2N_5O8x5K4W_9K5X5H2O8q_0O_7H4E7F4d8K8W_7W5B2e7K5X9C8N8U7w9V7T2J2R_9H1G8s_0J8V1G5N4M8y5M9J9K_9W_0B8B_6M_6E7C_8W8T9N1C5H8W6J8E3G9W3O9Z5E7T5A7N2M7O3R2T_7M1H9K9K6K8S2R_8P1R2U9N_9T9F9X6J2B9F8J7B1M7H9N_6j3O8S4D1E3R1B8d_0V9G")
            in 17..20 -> Song("Sunset Lover", "Petit Biscuit", "https://lh3.googleusercontent.com/aida-public/AB6AXuB3R1P9c6U7K_6W9K_6M1G3x4P2t4X1o_8d2N_5O8x5K4W_9K5X5H2O8q_0O_7H4E7F4d8K8W_7W5B2e7K5X9C8N8U7w9V7T2J2R_9H1G8s_0J8V1G5N4M8y5M9J9K_9W_0B8B_6M_6E7C_8W8T9N1C5H8W6J8E3G9W3O9Z5E7T5A7N2M7O3R2T_7M1H9K9K6K8S2R_8P1R2U9N_9T9F9X6J2B9F8J7B1M7H9N_6j3O8S4D1E3R1B8d_0V9G")
            else -> Song("Midnight City", "M83", "https://lh3.googleusercontent.com/aida-public/AB6AXuB3R1P9c6U7K_6W9K_6M1G3x4P2t4X1o_8d2N_5O8x5K4W_9K5X5H2O8q_0O_7H4E7F4d8K8W_7W5B2e7K5X9C8N8U7w9V7T2J2R_9H1G8s_0J8V1G5N4M8y5M9J9K_9W_0B8B_6M_6E7C_8W8T9N1C5H8W6J8E3G9W3O9Z5E7T5A7N2M7O3R2T_7M1H9K9K6K8S2R_8P1R2U9N_9T9F9X6J2B9F8J7B1M7H9N_6j3O8S4D1E3R1B8d_0V9G")
        }

        Box("""

new_else = """    } else {
        val suggestedSong = randomPicks.firstOrNull()?.let { Song(it.title, it.artist, it.imageUrl) } ?: Song("Unknown", "Unknown", "")
        Box("""

content = content.replace(old_else, new_else)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
