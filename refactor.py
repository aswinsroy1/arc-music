import os

def replace_in_file(filepath, old_str, new_str, import_statement=None):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    if old_str in content:
        content = content.replace(old_str, new_str)
        if import_statement and import_statement not in content:
            # add import below package declaration
            content = content.replace('package com.aeswox.arcmusic\n', f'package com.aeswox.arcmusic\n{import_statement}\n')
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)

base_dir = r"c:\Users\aswin\OneDrive\Desktop\Arc Music\app\src\main\java\com\aeswox\arcmusic"

files = [
    "MainActivity.kt",
    "GenreHubScreen.kt",
    "NowPlayingScreen.kt",
    "LibraryComponents.kt"
]

for file in files:
    filepath = os.path.join(base_dir, file)
    replace_in_file(filepath, "MusicViewModel = viewModel()", "MusicViewModel = hiltViewModel()", "import androidx.hilt.navigation.compose.hiltViewModel")
    
# for MainActivity, we also need to add @AndroidEntryPoint
main_path = os.path.join(base_dir, "MainActivity.kt")
with open(main_path, 'r', encoding='utf-8') as f:
    main_content = f.read()

if "@AndroidEntryPoint" not in main_content:
    main_content = main_content.replace('class MainActivity : ComponentActivity() {', 'import dagger.hilt.android.AndroidEntryPoint\n\n@AndroidEntryPoint\nclass MainActivity : ComponentActivity() {')
    with open(main_path, 'w', encoding='utf-8') as f:
        f.write(main_content)

print("Done refactoring Hilt.")
