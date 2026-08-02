import re

content_main = open("app/src/main/java/com/example/MainActivity.kt").read()
content_genre = open("app/src/main/java/com/example/GenreHubScreen.kt").read()
content_lib = open("app/src/main/java/com/example/LibraryComponents.kt").read()

def get_lists(content, list_name):
    return re.findall(rf'val {list_name} = listOf\((.*?)\)', content, re.DOTALL)

print("Main songs:", len(get_lists(content_main, "songs")))
print("Main categories:", len(get_lists(content_main, "categories")))
print("Genre tracks:", len(get_lists(content_genre, "tracks")))
print("Genre images:", len(get_lists(content_genre, "images")))
print("Genre albums:", len(get_lists(content_genre, "albums")))
print("Genre artists:", len(get_lists(content_genre, "artists")))
print("Lib tracks:", len(get_lists(content_lib, "tracks")))

