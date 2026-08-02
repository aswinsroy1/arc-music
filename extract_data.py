import re
import os

files_to_check = [
    "app/src/main/java/com/example/MainActivity.kt",
    "app/src/main/java/com/example/GenreHubScreen.kt",
    "app/src/main/java/com/example/LibraryComponents.kt",
    "app/src/main/java/com/example/ListeningStatsScreen.kt"
]
for f in files_to_check:
    if os.path.exists(f):
        print(f"checking {f}")
