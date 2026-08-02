import re

content = open("app/src/main/java/com/example/MainActivity.kt").read()

old_code = """    var currentSong by remember { mutableStateOf<Song?>(Song("Care", "Conan Gray", "https://lh3.googleusercontent.com/aida-public/AB6AXuDcxr5OkSQfpI_jTkSInZTLTIQNPElvx4VTAwf6InyR5cV2DD4SLzOgYsBC1gNArokFiZMFSwmKVi6VW-OeV6ouanmXDcfN4aD-RtGJFuMNyYZTx5P6VkXi-b4eY5GWUNpAaGeTkiqgkdzS6Of-mtUzJt7rz9IYbGhj7V3IcTi8iHjlof7t5fJzN09WsP72jlTq2o-VEsgIRAPXzreisxiQKK8kmsYEbFlDl442gyzxMfa0UGT2M3aJ5eafCHY0tM_wkFed6Lty8vDU")) }
    
    val onSongClick: (Song) -> Unit = { song ->
        currentSong = song
        isMiniPlayerVisible = true
    }"""

new_code = """    val viewModel: MusicViewModel = viewModel()
    val currentlyPlayingEntity by viewModel.currentlyPlaying.collectAsState()
    val currentSong = currentlyPlayingEntity?.let { Song(it.title, it.artist, it.imageUrl) }
    
    val onSongClick: (Song) -> Unit = { song ->
        viewModel.setCurrentlyPlaying(com.example.db.SongEntity(title = song.title, artist = song.artist, imageUrl = song.imageUrl, category = ""))
        isMiniPlayerVisible = true
    }"""

if old_code in content:
    content = content.replace(old_code, new_code)
    with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
        f.write(content)
    print("Replaced currentSong definition in MusicHomeScreen")
else:
    print("Could not find old_code")
