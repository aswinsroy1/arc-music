import re

content = open("app/src/main/java/com/example/MusicViewModel.kt").read()

new_imports = """import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.app.Application
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.db.MusicDatabase
import com.example.db.MusicRepository
import com.example.db.SongEntity
import com.example.db.initialSeedData
"""
content = content.replace("import androidx.lifecycle.ViewModel", new_imports)

content = content.replace("class MusicViewModel : ViewModel() {", """class MusicViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MusicRepository

    val randomPicks: StateFlow<List<SongEntity>>
    val recentlyPlayed: StateFlow<List<SongEntity>>
    val recommended: StateFlow<List<SongEntity>>
    
    val libraryAlbums: StateFlow<List<SongEntity>>
    val libraryArtists: StateFlow<List<SongEntity>>
    val libraryTracks: StateFlow<List<SongEntity>>
    
    val genreTopTracks: StateFlow<List<SongEntity>>
    val genreTopAlbums: StateFlow<List<SongEntity>>
    val genreTopArtists: StateFlow<List<SongEntity>>

    init {
        val database = MusicDatabase.getDatabase(application)
        repository = MusicRepository(database.musicDao())

        viewModelScope.launch {
            repository.seedDatabaseIfNeeded(initialSeedData)
        }

        randomPicks = repository.getSongsByCategory("RandomPicks").stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        recentlyPlayed = repository.getSongsByCategory("RecentlyPlayed").stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        recommended = repository.getSongsByCategory("RecommendedDownloads").stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
        libraryAlbums = repository.getSongsByCategory("LibraryAlbums").stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        libraryArtists = repository.getSongsByCategory("LibraryArtists").stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        libraryTracks = repository.getSongsByCategory("LibraryTracks").stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
        genreTopTracks = repository.getSongsByCategory("GenreTopTracks").stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        genreTopAlbums = repository.getSongsByCategory("GenreTopAlbums").stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        genreTopArtists = repository.getSongsByCategory("GenreTopArtists").stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }
""")

with open('app/src/main/java/com/example/MusicViewModel.kt', 'w') as f:
    f.write(content)

print("MusicViewModel patched")
