import re

content = open("app/src/main/java/com/example/MusicViewModel.kt").read()

old_code = """    val genreTopArtists: StateFlow<List<SongEntity>>"""

new_code = """    val genreTopArtists: StateFlow<List<SongEntity>>
    
    private val _currentlyPlaying = MutableStateFlow<SongEntity?>(null)
    val currentlyPlaying: StateFlow<SongEntity?> = _currentlyPlaying.asStateFlow()
    
    fun setCurrentlyPlaying(song: SongEntity?) {
        _currentlyPlaying.value = song
    }"""

if old_code in content:
    content = content.replace(old_code, new_code)
    with open("app/src/main/java/com/example/MusicViewModel.kt", "w") as f:
        f.write(content)
    print("Replaced currentlyPlaying definition in MusicViewModel")
else:
    print("Could not find old_code")
