import re

with open('app/src/main/java/com/example/NowPlayingScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("Icons.Outlined.PlaylistAdd", "Icons.Default.PlaylistAdd")
content = content.replace("Icons.Outlined.FavoriteBorder", "Icons.Default.FavoriteBorder")
content = content.replace("Icons.Outlined.Download", "Icons.Default.Download")
content = content.replace("Icons.Outlined.Album", "Icons.Default.Album")
content = content.replace("Icons.Outlined.Person", "Icons.Default.Person")
content = content.replace("Icons.Outlined.Share", "Icons.Default.Share")
content = content.replace("Icons.Outlined.Info", "Icons.Default.Info")

with open('app/src/main/java/com/example/NowPlayingScreen.kt', 'w') as f:
    f.write(content)
print("Done")
