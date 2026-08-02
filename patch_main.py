import re

content = open("app/src/main/java/com/example/MainActivity.kt").read()

content = content.replace("import androidx.lifecycle.viewmodel.compose.viewModel", "import androidx.lifecycle.viewmodel.compose.viewModel\nimport androidx.compose.runtime.collectAsState\nimport androidx.compose.runtime.getValue")

def replace_section(content, section_name, viewmodel_prop):
    pattern = r'fun ' + section_name + r'(.*?)\{\n    val songs = listOf\([\s\S]*?\n    \)'
    
    replacement = r'fun ' + section_name + r'\1{\n    val viewModel: MusicViewModel = viewModel()\n    val songEntities by viewModel.' + viewmodel_prop + r'.collectAsState()\n    val songs = songEntities.map { Song(it.title, it.artist, it.imageUrl) }'
    
    return re.sub(pattern, replacement, content)

content = replace_section(content, "RandomPicksSection", "randomPicks")
content = replace_section(content, "RecentlyPlayedSection", "recentlyPlayed")
content = replace_section(content, "RecommendedDownloadsSection", "recommended")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)

print("MainActivity patched")
