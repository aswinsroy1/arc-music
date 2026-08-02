content = """package com.example.db

val initialSeedData = listOf<SongEntity>()
"""
with open("app/src/main/java/com/example/db/SeedData.kt", "w") as f:
    f.write(content)
