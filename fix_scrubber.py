content = open("app/src/main/java/com/example/NowPlayingScreen.kt").read()
content = content.replace('text = "1:35"', 'text = "0:00"')
content = content.replace('text = "-2:49"', 'text = "0:00"')
with open("app/src/main/java/com/example/NowPlayingScreen.kt", "w") as f:
    f.write(content)
