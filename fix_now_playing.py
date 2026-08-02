import re

content = open("app/src/main/java/com/example/NowPlayingScreen.kt").read()

old_lyrics1 = """    val lyrics = listOf(
        "You're just a wishbone",
        "Waiting for the snap",
        "Because you're always",
        "Looking for a way",
        "To break the mold",
        "And start again",
        "But I'm not sure",
        "If I can wait",
        "For you to find",
        "Your way back home"
    )"""

new_lyrics1 = """    val lyrics = listOf(
        "Lyrics not available"
    )"""

content = content.replace(old_lyrics1, new_lyrics1)

with open("app/src/main/java/com/example/NowPlayingScreen.kt", "w") as f:
    f.write(content)
