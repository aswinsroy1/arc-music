# Status

**Current State**:
- Initial setup and structural tasks completed.
- Renamed package to `com.aeswox.arcmusic`.
- Updated `minSdk` to 31.
- Updated app name to "Arc Music" and base theme to `Theme.ArcMusic`.
- Cleaned up README.md to remove AI Studio references.
- Added Hilt for Dependency Injection, registered ArcMusicApplication, and converted MusicViewModel.
- Replaced the placeholder `SongEntity` schema with a proper relational Room schema (Track, Album, Artist, Playlist, PlaylistTrack, PlayHistory) and separated DAOs.
- Implemented MediaStore scanning to populate the database with real local tracks, albums, and artists, including runtime permissions support and a Scan button on the Home Screen.
- Implemented Media3/ExoPlayer integration (`PlaybackService`, `MusicPlayerConnection`) for real background audio playback.
- Wired UI controls (Play/Pause, Skip Next, Skip Previous) in NowPlaying and MiniPlayer to real media controller. Queue generation automatically builds playlists from UI sections when a track is tapped.
- Fixed playback issues: Thumbnails now load from MediaStore `ALBUM_ID`, Seekbar scrubs correctly and instantly jumps to position, and NowPlaying Screen metadata correctly maps from ExoPlayer to UI. Optimized ExoPlayer seeking for FLAC files.
- Completed rewiring UI screens fully to the new relational schema (`Track`, `Album`, `Artist` etc.) rather than using intermediate mappings. Library screens now correctly fetch and render from DB entities.

- Wired Album Details screen to real database flows, replacing dummy data.

- Wired real PlayHistory logging: `MusicPlayerConnection.PlayerListener` now inserts a `play_history` row and increments `Track.playCount`/`Track.lastPlayedAt` on every `onMediaItemTransition`. Natural track completion (`STATE_ENDED`) marks the most recent row `completed = true`. Logic lives entirely in `MusicPlayerConnection` (injected with `MusicRepository`), not split across ViewModel.
- Wired Queue generation for Home screen sections (Recently Played, Random Picks, Recommended) and exposed real queue state in `QueueScreen.kt`.
- Improved `ArtworkRepository` to fetch artist images via MusicBrainz MBID -> TheAudioDB, avoiding name collisions for more accurate results.
- Wired `CollectionHealthScreen.kt` with real data computation (missing artwork, missing metadata, duplicate songs, corrupted tags, low-quality files) and a full MusicBrainz background sync for discography gaps (missing tracks/albums). Also implemented the "Review & Clean Up" duplicate song flow.
- Scoped MusicBrainz discography gap sync strictly to favorited artists. Added an artist-level favorite toggle to `ArtistDetailsScreen.kt` and updated `CollectionHealthScreen.kt` to accurately reflect gaps or an empty state based on favorited artists.
- Diagnosed and fixed the inflated discography-gap issue (e.g. 91 missing tracks for One Direction). The bug was twofold: MusicBrainz's `release` endpoint was returning un-filtered release editions truncated by a 100 limit (missing newer albums) and iterating over all editions of the same album caused massive double-counting. Fixed by querying `release` using the search endpoint (`query="arid:... AND status:official AND (primarytype:album OR primarytype:ep)"`), filtering out compilations/live albums locally, grouping by `release-group`, and picking the max track-count edition per distinct album to match against local data.

**What's Next**:
- Wire Home screen "Recently Played" section to `getRecentlyPlayedTracks()` — real data now exists.
- Wire Listening Stats screen to real `play_history` data.
- Wire `PlaylistDetailsScreen.kt` to real data.
- Verify functionality of the Library tab in the actual app instance.

**Known Issues**:
- Project is missing the `arc-music-build-plan.md` referenced in global guidelines.
- Acceptance check (DB query post-playback) requires a connected device — run `check_play_history.ps1` after playing tracks.
- Last.fm API Key is still a placeholder (`"YOUR_LAST_FM_API_KEY"`) which blocks Last.fm artwork fetching from working until a real key is provided.
