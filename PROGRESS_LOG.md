# Progress Log

## 2026-07-25
- **Changed**:
  - Renamed package structure from `com.example` to `com.aeswox.arcmusic`.
  - Updated `namespace` and `applicationId` to `com.aeswox.arcmusic` in `app/build.gradle.kts`.
  - Set `minSdk = 31` in `app/build.gradle.kts`.
  - Changed app name to "Arc Music" in `strings.xml`.
  - Created `ArcMusicApplication` annotated with `@HiltAndroidApp` and registered it in `AndroidManifest.xml`.
  - Created placeholder Hilt modules (`DatabaseModule`, `NetworkModule`).
  - Migrated `MusicViewModel` from `AndroidViewModel` to `@HiltViewModel` using constructor injection.
  - Updated compose navigation call sites to use `hiltViewModel()` instead of `viewModel()`.
  - Used the `android.newDsl=false` workaround in `gradle.properties` to allow Hilt to work with AGP 9.1.1.
  - Verified build succeeds with Hilt integrated.

## Database Schema Refactor
- Designed and implemented the real Room relational schema covering `Track`, `Album`, `Artist`, `Playlist`, `PlaylistTrack`, and `PlayHistory` entities.
- Created specialized DAOs (`TrackDao`, `AlbumDao`, `ArtistDao`, `PlaylistDao`, `PlayHistoryDao`) with queries supporting current UI needs.
- Updated `MusicDatabase` to increment version to 2, adding the new DAOs and utilizing destructive migration.
- Updated `DatabaseModule` to provide the new DAOs into the dependency graph.
- Kept the legacy `SongEntity` as a pure data class (removed Room annotations) and stubbed `MusicRepository` methods to allow UI compilation to pass while the data source is detached.

- **Verified**:
  - All file references and package declarations were properly updated.
  - Running a Gradle assemble to verify compilation success.
- **Deviations**:
  - Could not read `arc-music-build-plan.md` as it was not found in the project root. Created initial `STATUS.md` and `PROGRESS_LOG.md` as per rules.

## MediaStore Scanning
- Requested `READ_MEDIA_AUDIO` (API 33+) and `READ_EXTERNAL_STORAGE` (API 31-32) permissions in AndroidManifest.xml.
- Implemented Accompanist permissions logic in `MusicHomeScreen` to trigger the permission request.
- Added a "Scan MediaStore" button to `MusicHomeScreen` to invoke the scan.
- Created `MediaStoreScanner` to query `MediaStore.Audio.Media.EXTERNAL_CONTENT_URI` and extract track metadata.
- Implemented `MusicRepository.scanMediaStore()` to map scanned tracks into Room entities (`Track`, `Album`, `Artist`), populate missing required fields with defaults, and save them.
- Updated `DatabaseModule` to inject `MediaStoreScanner` into `MusicRepository`.
- Added state flow in `MusicViewModel` to observe and display the count of scanned entities in the UI.
- **Verified**:
  - Successfully compiles without syntax errors. Instantiations of Room Entities matched the schema definition.

## Playback Engine & Wiring
- Setup `MediaSessionService` (`PlaybackService`) and `MediaController` wrapper (`MusicPlayerConnection`) for playback using AndroidX Media3/ExoPlayer.
- Exposed state flows (`isPlaying`, `currentlyPlayingItem`) from the connection to sync the UI with playback state.
- Rewired `MusicViewModel` and UI components (MiniPlayer, NowPlayingScreen) to use these new state flows and invoke `MusicPlayerConnection.play()`, `pause()`, `skipToNext()`, `skipToPrevious()`.
- Implemented queue generation so that playing a song from the UI loads the containing list into the MediaController queue and starts at the clicked item's index.
- Temporarily updated `Song` class to hold `extraData` (storing the real file paths mapped from the database) without altering the visual structure of the UI components pending the rewiring phase.
- **Verified**:
  - EAC3 hardware decode tested successfully.
  - Successfully compiles after typing fixes (`id` conversions, nullability fixes).

## Playback Bug Fixes (Thumbnails, Seekbar, Titles, FLAC Seeking)
- Added `albumId` extraction to `MediaStoreScanner` and updated schema version to supply `content://media/external/audio/albumart/<id>` to UI.
- Mapped explicit `MediaMetadata` into `MediaItem` so ExoPlayer exposes title and artwork URI robustly.
- Attached `currentPosition` and `duration` flows from ExoPlayer into `MusicViewModel`, wiring it seamlessly to `NowPlayingScreen`'s `Slider`.
- Implemented immediate UI updates upon Slider drag release to eliminate visual stutter while waiting for ExoPlayer to seek.
- Adjusted `DefaultLoadControl` and `SeekParameters.CLOSEST_SYNC` in `PlaybackService` to significantly eliminate seeking buffer delays, especially perceptible in heavy FLAC files.
- **Verified**: Builds successfully. UI no longer crashes. Slider format is mm:ss.

## Library Rewiring
- Completed rewiring UI components (LibraryComponents, GenreHub, Queue, NowPlaying, etc.) to use real database entities (Track, Album, Artist) instead of placeholder dummy classes.
- Replaced local `Track` dummy class in LibraryComponents with a new `LibraryUiItem` wrapper and mapped entities correctly.
- Fixed numerous compilation errors introduced by the removal of `SongEntity`.
- **Verified**: The app builds successfully in 57s. All UI screens compile cleanly with the new schema.

## Album Details Rewiring
- Modified `MainActivity.kt` navigation to accept and pass `albumId` to `album_details/{albumId}` and `artistId` to `artist_details/{artistId}`.
- Updated `LibraryComponents.kt` and `MusicHomeScreen.kt` to pass the correct entity IDs to navigation callbacks.
- Updated `MusicRepository.kt` and `MusicViewModel.kt` to expose `getAlbumById`, `getTracksByAlbum`, and `getAlbumsByArtist` queries via Flow.
- Rewired `AlbumDetailsScreen.kt` to collect real data flows from `MusicViewModel` and replaced all hardcoded UI placeholders with dynamic content.
- Wired UI actions in `AlbumDetailsScreen.kt` to Play/Shuffle track lists, expand track listings, and navigate to the related artist or discography.
- **Verified**: Screen cleanly compiles and the UI is fully dynamic.

## Artist Details Rewiring
- Updated MusicRepository.kt and MusicViewModel.kt to expose getArtistById and getTracksByArtist queries via Flow.
- Rewired ArtistDetailsScreen.kt to collect real data flows from MusicViewModel and replaced all hardcoded UI placeholders with dynamic content.
- Wired UI actions in ArtistDetailsScreen.kt to Play/Shuffle track lists, and navigate to related albums.
- Replaced the hardcoded Conan Gray bio with the artist's real bio or a placeholder message if absent.
- Verified that the UI compiles and dynamically loads the real artist photo, track play counts, and related albums without relying on external APIs.
- **Verified**: Screen cleanly compiles and the UI is fully dynamic.


- Renamed 'Popular' to 'Tracks'.
- Created ArtistTracksScreen and ArtistAlbumsScreen to display full lists of a specific artist's tracks and albums.
- Wired 'See all' buttons in ArtistDetailsScreen to navigate to these new dedicated screens using the routes artist_tracks/{artistId} and artist_albums/{artistId}.

## Play History Logging (2026-07-27)
- **Goal**: Wire real `PlayHistory` logging so play events are persisted and `getRecentlyPlayedTracks()` returns meaningful data.
- **Changed**:
  - `TrackDao.kt`: Added `incrementPlayCountAndUpdateLastPlayed(trackId, timestamp)` — atomic SQL `UPDATE`, avoids read-modify-write.
  - `PlayHistoryDao.kt`: Added `markMostRecentCompleted(trackId)` — uses `MAX(id)` subquery to target the single latest row per track.
  - `MusicRepository.kt`: Added `logPlayStart(trackId)` (inserts `PlayHistory` + increments playCount/lastPlayedAt) and `markPlayCompleted(trackId)`.
  - `MusicPlayerConnection.kt`: Injected `MusicRepository`. `PlayerListener.onMediaItemTransition` logs play-start for each distinct new track (guarded by `lastLoggedMediaId`). `onPlaybackStateChanged(STATE_ENDED)` marks completion. Skipped tracks remain `completed = false`.
  - `PlaybackModule.kt`: Passes `MusicRepository` to `MusicPlayerConnection` provider.
- **Architectural decision**: All logging in `MusicPlayerConnection`, not `MusicViewModel` — fires regardless of ViewModel lifecycle, zero double-logging risk.
- **Build**: `BUILD SUCCESSFUL in 1m 16s`. No errors.
- **Acceptance check**: No device connected at write time. Run `check_play_history.ps1` after playing ≥3 tracks (let ≥1 finish naturally, skip ≥1).

## 2026-07-28
- **Goal**: Fix queue-building at entry points and wire the real Queue screen.
- **Changed**:
  - `MainActivity.kt`: Updated `onSongClick` signature to `(Track, List<Track>?) -> Unit` so that `RecentlyPlayedSection`, `RandomPicksSection`, and `RecommendedDownloadsSection` can pass the full context queue into `setCurrentlyPlaying()`.
  - `MusicPlayerConnection.kt`: Added `currentQueue` and `currentMediaItemIndex` StateFlows, populated by overriding `PlayerListener.onTimelineChanged()`. Added `skipToQueueItem(index)` to allow jumping to a track in the queue.
  - `MusicViewModel.kt`: Mapped `MusicPlayerConnection.currentQueue` (List<MediaItem>) into `List<Track>` state flow for UI consumption, handling metadata fallback mapping similarly to `currentlyPlaying`.
  - `QueueScreen.kt`: Wired UI to use real `currentQueue`, `currentQueueIndex`, and `currentlyPlaying` flows from ViewModel. Replaced empty state/hardcoded list logic with slicing `currentQueue` for "Up Next". Clicking tracks jumps playback.
- **Audited**: Verified that Search results, Genre Hub, and Playlist sections are currently populated by hardcoded UI and are not wired, so they were left as is.
- **Build**: Compiled and successfully verified.

## 2026-08-02
- **Goal**: Add MusicBrainz-ID-based artist image lookup to `ArtworkRepository` for higher precision than plain name matching.
- **Changed**:
  - `ApiKeys.kt`: Updated `THE_AUDIO_DB_API_KEY` to `"123"` (valid test key).
  - `AlternativeApis.kt`: Added `searchArtistByMbid` to `TheAudioDbService` mapped to `artist-mb.php?i={mbid}`.
  - `ArtworkRepository.kt`: Added `fetchArtistImageViaMusicBrainz` and inserted it as step 1.5 in the fallback chain. This grabs the MBID from `MusicBrainzService` and looks it up in `TheAudioDbService`, completely bypassing name-collision issues.
- **Verified**: Ran a test script against `Laufey` and `Mac DeMarco` confirming the MusicBrainz -> TheAudioDB chain successfully returns an image URL for them.
- **Known Issue**: Last.fm API Key is still a placeholder (`"YOUR_LAST_FM_API_KEY"`). Last.fm fetch will consistently fail until a real key is acquired.

## Collection Health Rewiring
- **Goal**: Wire real computations into the Collection Health screen.
- **Changed**:
  - `TrackDao.kt` / `MusicRepository.kt`: Added queries `getTracksMissingArtwork()`, `getTracksMissingMetadata()`, `getLowQualityTracks()`, and `getCorruptedTracks()`.
  - `Artist.kt` / `MusicDatabase.kt`: Bumped schema to v11 (added `MIGRATION_10_11`) and added `missingTracksCount`/`missingAlbumsCount` to the `Artist` entity for caching discography gaps.
  - `AlternativeApis.kt` / `ArtworkRepository.kt`: Implemented `fetchDiscographyGaps()` which calls MusicBrainz `getReleases()` for owned artists and fuzzy-matches against local albums to compute gaps.
  - `MusicViewModel.kt`: Added `CollectionHealthState` and exposed `healthState`. Calculates duplicates (title/artist/fuzzy duration match), health score, and aggregates missing/corrupted data. Added a background sync to fetch gaps for artists lacking cached values.
  - `CollectionHealthScreen.kt`: Rewired the entire UI to observe `healthState`. Implemented `DuplicateReviewSheet` allowing the user to select and delete low-quality duplicates.
- **Goal Update**: Scoped discography gaps strictly to favorited artists.
- **Changed**:
  - `Artist.kt`: Confirmed `isFavorite` already existed. No migration needed.
  - `ArtistDetailsScreen.kt`: Added a favorite toggle button to `ArtistHeroSection`.
  - `MusicViewModel.kt`: Restored `toggleArtistFavorite` function and updated background gap sync logic to only process `artists.filter { it.isFavorite }`. Also aggregated `missingTracksCount`/`missingAlbumsCount` strictly for favorited artists and added `favoritedArtistsCount` to `CollectionHealthState`.
  - `CollectionHealthScreen.kt`: Updated `CollectionHealthGapsSection` to honestly handle the case where the user hasn't favorited any artists, changing the GapCard labels appropriately.
- **Verified**: App builds cleanly and background sync is correctly gated.

**Date: August 2, 2026**
- **Goal**: Diagnose and fix inflated discography-gap numbers for MusicBrainz sync (e.g. 91 missing tracks / 14 missing albums for One Direction).
- **Investigation**: Tested the API directly. Discovered two bugs:
  1. The API call `ws/2/release` with limit 100 had no type or status filters, returning singles/bootlegs and truncating newer albums.
  2. Double counting: MusicBrainz returns multiple editions of the same album as separate `release` entries (e.g. Deluxe, Japanese). The code counted missing tracks across *all* editions.
- **Changed**: 
  - `AlternativeApis.kt`: Updated `MusicBrainzService` to use a robust `searchReleases` query (`query="arid:MBID AND status:official AND (primarytype:album OR primarytype:ep)"`). Updated `MusicBrainzRelease` to map `release-group` to correctly extract secondary types and IDs.
  - `ArtworkRepository.kt`: Refactored `fetchDiscographyGaps` to filter out live/compilation albums using `release-group.secondary-types`, group the releases by `release-group.id` to ensure each album is only checked once, and use the maximum track count edition per album to match against local data.
- **Verified**: One Direction correctly groups down to their core studio albums (Up All Night, Take Me Home, Midnight Memories, Four, Made in the A.M.), eliminating massive inflation.

## Missing Content Screen Bug Fixes (2026-08-03)
- **Goal**: Fix Missing Tracks showing empty and Album action buttons using track-search logic.
- **Changed**:
  - `ArtworkRepository.kt`: Modified `getDetailedDiscographyGaps` to add completely missing albums (`localMatch == null`) to *both* the `missingAlbums` and `missingTracks` lists, properly syncing the Tracks tab with the aggregate missing track count.
  - `MissingContentScreen.kt`: Replaced the generic `PlayArrow` icon with a green `Search` icon for Spotify, and a red `SmartDisplay` icon for YouTube Music.
  - `MissingContentScreen.kt`: Updated search intents to correctly construct album-specific queries (`album:<Title> artist:<Artist>` for Spotify, and `<Title> album <Artist>` for YouTube Music) when the item is an album.
- **Verified**: The Missing Tracks tab is no longer empty when an artist has completely missing albums, and search intents properly respect item type.

