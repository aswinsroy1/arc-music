# Progress Log

## 2026-08-18 — LyricsPlus as Primary Online Lyrics Source

### Goal
Add LyricsPlus (`https://lyricsplus.prjktla.my.id/v2/lyrics/get`) as the first online lyrics source tried, before the existing LRCLIB fallback. Local sources (embedded tags, `.lrc` sidecar files) remain checked first. Word-level (`type=Word`) responses provide richer syllable-by-syllable data than LRCLIB's LRC format for tracks where Apple Music sync data is available upstream.

### New Files
- `data/network/LyricsPlusService.kt` — Retrofit interface: `GET /v2/lyrics/get?title&artist`. No API key. Follows the same pattern as `LrcLibApiService`.
- `data/network/LyricsPlusResponse.kt` — Moshi models: `LyricsPlusResponse` (type, lyrics[]), `LyricsPlusLine` (time ms, text, syllabus[]), `LyricsPlusSyllable` (time ms, text). `@JsonClass(generateAdapter = true)` on all three.

### Modified Files
- `di/NetworkModule.kt` — Added `provideLyricsPlusService()`: dedicated Retrofit instance at `https://lyricsplus.prjktla.my.id/`, using the shared `OkHttpClient`.
- `data/repository/LyricsRepositoryImpl.kt`:
  - Injected `LyricsPlusService` via constructor.
  - Inserted `fetchFromLyricsPlus(track)` as step 3 in the `fetchers` chain (between local `.lrc` at step 2 and `fetchFromLrcLib` at step 4).
  - Implemented `fetchFromLyricsPlus()`:
    - `type=Word` → maps `syllabus[]` entries to `List<SyncedWord>` (time already in ms, word boundary derived from trailing space in raw text), producing fully populated `SyncedLine(words=…)` for the existing word-by-word UI.
    - `type=Line` → `words=null`, `SyncedLine` carries only `time` + `line` text; existing line-display path handles this identically to LRCLIB line-only results.
    - Any exception or empty response → returns `null`, chain falls through to LRCLIB silently.
  - Caching: on success, result written to existing `lyricsCache` map at the same callsite as all other sources.

### Key Decisions
- **No new cache layer**: The existing in-memory `lyricsCache` is sufficient. Adding a Room or disk cache for this source would duplicate the LRC file-write mechanism already used by `downloadAndSaveLyrics()`.
- **Word boundary logic**: LyricsPlus `syllabus[].text` uses a trailing space to indicate the end of a word (e.g. `"I "`, `"been "`, `"call"`). The first syllable in each line always starts a new word. `startsNewWord` is set true if the *previous* syllable's raw text ends with a space.
- **Scope unchanged**: `downloadAndSaveLyrics()` and `hasLocalOrEmbeddedLyrics()` are not modified — they only touch local and LRCLIB paths, which is correct.

### Build
- `BUILD SUCCESSFUL in 2m 20s`. Zero new errors. Two pre-existing warnings unchanged (deprecated `fallbackToDestructiveMigration`, Moshi KAPT deprecation notice).

### Acceptance Checks (require device)
1. **Word-level source**: Play "Blinding Lights" by The Weeknd (or another track with Apple Music sync upstream in LyricsPlus). Confirm word-by-word highlighting advances per syllable rather than per line.
2. **LrcLib fallback**: Play a track LyricsPlus doesn't have. Confirm logcat shows `"Found lyrics from source 4"` (LRCLIB), not an error, and lyrics display normally.
3. **Local priority**: Add a `.lrc` file next to an audio file. Confirm logcat shows `"Found lyrics from source 2"` and neither LyricsPlus nor LRCLIB is called.
4. **Unreachable graceful fallback**: Temporarily point `lyricsplus.prjktla.my.id` to a non-routable address (e.g. via `/etc/hosts` on a rooted device) and confirm lyrics still appear via LRCLIB with no error dialog.

## 2026-08-11 — AMOLED Dark Theme
- **Goal**: Implement a true-black AMOLED dark theme and wire it to a persistent user toggle in Settings, auditing and removing all hardcoded UI colors.
- **Changed**:
  - `Color.kt` / `Theme.kt`: Added a full suite of `Dark*` color tokens featuring `#000000` background and `0D0D0D` surfaces, and wired them into `DarkColorScheme`.
  - `SettingsRepository.kt` / `MusicViewModel.kt`: Added a persistent `ThemeMode` flow backed by DataStore.
  - `SettingsScreen.kt`, `MainActivity.kt`, `MissingArtworkScreen.kt`, `CollectionGrowthScreen.kt`, `EqualizerScreen.kt`: Audited and replaced hardcoded `Color.White`, `Color.Black`, `Color(0xFFF2F4F7)`, `Color(0xFFF8F8F8)` and grey hex values with their semantic `MaterialTheme.colorScheme` equivalents.
  - `ReusableComponents.kt`: Modified the `glassEffect` modifier to dynamically calculate its tint based on the luminance of the current background color (app-theme-aware, not just system-theme-aware) so glass blurs look correct on AMOLED black.
- **Verified**: The app builds cleanly. `CollectionGrowthScreen` gradient overlays intentionally keep `Color.White` text since they sit on dark gradient scrims.
- **Fixes**: Re-audited and removed remaining hardcoded colors from the `HeroSection` in `MainActivity.kt` and `EqualizerScreen.kt` that were missed in the initial sweep.
- **Audio Equalizer Fix**: Added the required `android.permission.MODIFY_AUDIO_SETTINGS` to `AndroidManifest.xml` so the ExoPlayer Equalizer actually processes audio (previously it was silently failing without this permission). Also fixed the hardcoded colors in `CustomVerticalSlider.kt`. Forced the audio session bind priority to `Int.MAX_VALUE`.
- **Appearance Settings Persistence**: Wired the 3 Appearance settings sliders (Glass Tint Transparency, Monochromatic Noise Factor, Glow Intensity) to `SettingsRepository.kt` via DataStore. Used synchronous `runBlocking` in the `MusicViewModel` initializer to ensure the saved preferences are rendered perfectly on frame 1 without flashing the default values first.
- **Appearance Screen Custom Sliders**: Built a `CustomHorizontalSlider.kt` to replace the default Material 3 `Slider` in `AppearanceScreen.kt`, perfectly matching the custom canvas-drawn aesthetic and interaction model of the Equalizer's `CustomVerticalSlider.kt`.
- **Search Screen Dark Mode Polish**: Removed hardcoded white/pastel backgrounds in the Search Screen (`MainActivity.kt`). The search bar now uses `MaterialTheme.colorScheme.surfaceContainerHigh`. The "Browse Categories" cards now dynamically composite 12% alpha of their vibrant accent color over the `surfaceContainer` base, rendering as rich, premium jewel tones in dark mode while preserving the bright pastel look in light mode.
## 2026-08-07 — Collection Growth: Include Top-Listened Artists in Fetch Pipeline

### Goal
Extend the existing Collection Growth fetch pipeline (New Release, Discovery, Missing Tracks/discography-gap detection) to cover the top 10 most-listened artists from `PlayHistory`, not just favorited artists.

### Changed Files
- **`db/MusicRepository.kt`**
  - Added `getTopListenedArtists(limit: Int = 10): List<Artist>` — aggregates listening time from `play_history` (same calculation as Listening Stats' Top Artists), ranks by total accumulated minutes, and looks up the top N names in the `artists` table. Artists with no corresponding entity are silently excluded.
  - Modified `loadCollectionGrowthData()`: fetches top-listened artists and merges with `favoritedArtists` via `(favorited + topListened).distinctBy { it.id }`. The merged set (`qualifyingArtists`) drives all four card type loops and the Discovery cap. Renamed `hasFavoritedArtists` → `hasQualifyingArtists` in the returned `CollectionGrowthData`.

- **`MusicViewModel.kt`**
  - `CollectionGrowthData.hasFavoritedArtists` renamed to `hasQualifyingArtists` (with KDoc noting it covers both favorited and top-listened).
  - `init` backfill coroutine: now fetches `repository.getTopListenedArtists(10)`, merges with favorited artists (deduped by id), and runs `refreshArtistGrowthData()` for every qualifying artist — same staleness gates as before.
  - `loadCollectionGrowth()` guard updated from `!data.hasFavoritedArtists` to `!data.hasQualifyingArtists`.

### Key Design Decisions
- **Threshold**: Top 10 by listening time — manageable, meaningful scope, same spirit as why favoriting was originally used.
- **No double-fetching**: Deduplication by `artist.id` before any loop. An artist who is both favorited and top-listened triggers exactly one `refreshArtistGrowthData()` call.
- **Re-evaluation cadence**: Top-listened set recomputed on each backfill tick (≈ every 7 days per the staleness policy). No extra timer, no extra infrastructure.
- **No DB schema changes** — computed purely from existing `play_history + tracks` data.
- **No UI changes** — cards render identically regardless of whether the artist qualified via favoriting or listening history.
- **Existing favorited-artist path fully preserved** — favorited artists appear first in the merged list, and all existing cache / throttle / staleness behaviour is untouched.

### Build
`BUILD SUCCESSFUL in 1m 38s`. All warnings are pre-existing (`@Json` annotation scope, deprecated `fallbackToDestructiveMigration`). Zero new errors.

### Acceptance Checks (require device)
1. Play an artist heavily without favoriting them → check logcat for `"Background scan started for <ArtistName>"` on first launch, or `"Serving <ArtistName> missing content from cache"` on subsequent.
2. Confirm Collection Growth cards appear for that artist (New Releases, Missing Tracks, or Complete Collection).
3. Confirm existing favorited artist cards are unaffected.
4. Dismiss a top-listened artist card → force-close + reopen → card should not reappear.

---

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

- **Goal (Follow-up)**: Fix Missing Content screen failing to load data entirely (both tracks and albums empty).
- **Changed**:
  - Added a 1.2 second delay inside `MusicViewModel.loadMissingContent` and `ArtworkRepository.getDetailedDiscographyGaps` to respect MusicBrainz's strict 1 req/sec rate limit. Rapid sequential requests were causing HTTP 503 errors, resulting in empty data.
- **Verified**: Builds cleanly, pushed to remote.

- **Goal (Follow-up 2)**: Fix Missing Content Singles Fix: Updated missing content screen to also identify and show full singles in a separate tab.
- **Concurrent Gap Fetches**: Background gap fetching processes for favorited artists now run concurrently rather than sequentially dropping operations.
- **Fanart.tv Integration**: Added Fanart.tv as an alternative high-quality artist art fallback mechanism, configurable with a user-provided API key from Settings.
- **Icon Rendering Fix**: Ensured the Dolby Atmos icon respects dynamic tinting (`colorFilter`) on the Now Playing screen.
- **Repository Hygiene**: Purged over 160 leftover root-level scratch scripts (`.py`, `.ps1`, `.txt`, `.log`, `.kt`, `.sh`) accumulated from previous ad-hoc tasks, and robustly configured `.gitignore` to prevent any such file types from accidentally lingering in the repository root moving forward. Checked for stray `.env` (none found; `.env` is safely gitignored). Verified complete build integrity post-cleanup.
- **Root Cause**: `MissingContentScreen` was instantiating a new, uninitialized `MusicViewModel` via `hiltViewModel()` instead of using the shared Activity-scoped instance. Since the DB fetch is asynchronous, `libraryArtists.value` was empty when `loadMissingContent()` executed in its `LaunchedEffect`.
- **Changed**:
  - `MainActivity.kt`: Passed the shared `viewModel` to `MissingContentScreen` to reuse the pre-loaded data.
  - `MusicViewModel.kt`: Removed the raw debug string and reverted to the default `MissingContentUiState.Empty()` message.
- **Verified**: The screen now properly reads the populated `libraryArtists` state from the shared ViewModel and calculates gaps correctly.

## Missing Content Screen — Bug Fixes & Real Caching (2026-08-04)
- **Bug 1 — Expand shows nothing**: Root cause confirmed: the MusicBrainz `/ws/2/release` search endpoint never returns inline `recordings` data regardless of the `inc=recordings` parameter — that only works on the single-release lookup endpoint `/ws/2/release/{mbid}`. All previous `officialTracks` lists were empty. Fixed by adding `getReleaseById(mbid)` to `MusicBrainzService` and doing a per-album lookup (with a 1.2s rate-limit delay) to fetch the real tracklist before diffing against local files.
- **Bug 2 — Cache not persisting across restarts**: `scanMediaStore()` was re-creating `Artist` objects from scan results without preserving `hasScannedMissingContent`, so every restart wiped the flag back to `false`. Fixed by preserving all existing fields (`missingTracksCount`, `missingAlbumsCount`, `hasScannedMissingContent`) from the database when re-inserting artists.
- **Bug 2 — Cache duplicates on re-fetch**: `MissingContentDao.insertAll` used `REPLACE` strategy but the `id` is auto-generated, so re-fetching stacked up duplicate rows rather than replacing them. Fixed with a `deleteAllByArtist()` call before every `insertAll()` in the new `persistMissingContent()` helper.
- **Bug 2 — No staleness policy**: Added `cachedAt: Long` column to `CachedMissingContent` (DB migration 13→14), and a `getOldestCachedAt()` DAO query. Both `getDetailedDiscographyGaps()` and `toggleArtistFavorite()` now check if the cache is older than 7 days before re-fetching.
- **Bug 3 — Auto-scan on favorite**: `toggleArtistFavorite()` now immediately launches a background scan via `backgroundScope` (a `CoroutineScope(SupervisorJob() + Dispatchers.IO)` scoped to the repository, replacing the previous fragile `GlobalScope.launch`). The scan respects the same 7-day staleness gate — it will not re-fetch if the cache is still fresh.
- **Build**: `BUILD SUCCESSFUL in 1m 22s`. No errors.
- **Acceptance checks** (require device):
  1. Tap a Partial Album → confirm actual track names expand below it.
  2. Force-close and reopen → check logcat for `"Serving X missing content from cache"` — no MusicBrainz requests should fire.
  3. Favorite a brand-new artist → check logcat for `"Background scan started for X"` and confirm cached data exists after ~30s.

## 2026-08-06 — Collection Growth: Wire All Four Card Types

### New Files
- `db/entities/CachedNewRelease.kt` — Room entity caching recent MusicBrainz release-groups not yet in local library (7-day staleness).
- `db/entities/DismissedGrowthCard.kt` — Room entity with composite PK `(cardType, title, artistName)` for permanent card dismissals.
- `db/daos/NewReleaseDao.kt` — DAO for `cached_new_releases` table.
- `db/daos/DismissedCardDao.kt` — DAO for `dismissed_growth_cards` table.

### Modified Files
- `db/MusicDatabase.kt` — Bumped to **v16**; registered new entities + DAOs; added `MIGRATION_15_16` creating both new tables.
- `di/DatabaseModule.kt` — Added `provideNewReleaseDao`, `provideDismissedCardDao` providers; updated `MusicRepository` provider constructor.
- `data/network/AlternativeApis.kt` — Added `LastFmService.getArtistSimilar()`, `LastFmService.getArtistTopTags()`; added all response model classes (`LastFmSimilarResponse`, `LastFmSimilarArtist`, `LastFmTopTagsResponse`, `LastFmTag`); added `MusicBrainzService.searchReleaseGroups()` + `MusicBrainzReleaseGroupResponse` / `MusicBrainzReleaseGroupItem` models.
- `data/network/ArtworkRepository.kt` — Added `resolveArtistMbidPublic()` (public wrapper for MBID resolution); added `fetchNewReleases()` (MusicBrainz release-group query, 90-day window, 1.2s throttle); added `fetchSimilarArtists()` (Last.fm `artist.getSimilar` + `artist.getTopTags`, polite 300ms delay); added `NewReleaseItem` + `DiscoveryItem` DTOs.
- `db/MusicRepository.kt` — Added `newReleaseDao` + `dismissedCardDao` constructor parameters; added `loadCollectionGrowthData(lastFmApiKey)` which orchestrates all four card types from existing cache + fresh fetches; added `dismissGrowthCard(type, title, artist)`; added private `fetchAndCacheNewReleases()` helper to avoid `val cannot be reassigned` in try/catch.
- `MusicViewModel.kt` — Added `GrowthCard` sealed class with 4 subtypes; added `CollectionGrowthData` + `CollectionGrowthUiState`; added `_growthState` StateFlow; added `loadCollectionGrowth()` (interleaves card types for varied feed); added `dismissGrowthCard(card)` (persists + removes from state immediately).
- `CollectionGrowthScreen.kt` — **Full rewrite**: LazyColumn driven by `growthState`; 4 card composables (`CompleteCollectionCard`, `NewReleaseCard`, `DiscoveryCard`, `MissingTracksCard`); fixed broken buttons (Open Spotify / Search YouTube replacing Jellyfin/Download); Dismiss on all cards; Loading + Empty states.
- `MainActivity.kt` — Passes `viewModel = viewModel` to `CollectionGrowthScreen`.

### Key Decisions
- **Reuse, not rebuild**: Complete Collection and Missing Tracks cards read directly from `CachedMissingContent` (populated by Collection Health / Missing Content scan). No second MusicBrainz query for these.
- **Discovery gating**: If `lastFmApiKey` is null/blank, `fetchSimilarArtists()` is skipped entirely — zero Discovery cards, no error, no UI change. Consistent with Fanart.tv gating pattern.
- **Dismiss scope**: `dismissed_growth_cards` table is shared infrastructure but dismiss button is only on Growth cards for now, not on Missing Content screen (different UX model).

### Build
- `BUILD SUCCESSFUL in 1m 36s`. Warnings are all pre-existing (`@Json` annotation scope, deprecated `fallbackToDestructiveMigration`). Zero new errors.

### Acceptance Checks (require device)
1. Navigate to Collection Growth → confirm Loading indicator while data loads, cards appear after.
2. Favorite 1+ artists with Collection Health gaps already scanned → confirm Complete Collection and Missing Tracks cards show real album names and counts.
3. Tap Dismiss on any card → confirm card vanishes immediately; force-close + reopen → confirm card does not reappear.
4. With no Last.fm API key → confirm no Discovery cards appear (no error, no crash).
5. With a Last.fm API key set → confirm Discovery cards appear with "Since you love X" and optional genre chip.
6. Tap "Open Spotify" → confirm Spotify search opens; tap "Search YouTube" → confirm YouTube Music search opens.
7. With no favorited artists → confirm empty state icon + message ("Favorite some artists…").


## Collection Growth UI Reorganization (2026-08-06)
- **Goal**: Reorganize Collection Growth into grouped, collapsible sections.
- **Changed**:
  - CollectionGrowthScreen.kt: Replaced flat card list with grouped sections ('Almost Complete', 'New Releases', 'Missing Tracks', 'Discover').
  - Converted Discovery cards to a horizontal scrolling chip row with a Bottom Sheet for details.
  - Converted other cards to be collapsible by default, expanding to reveal full details (buttons, progress) on tap.
- **Verified**: Builds successfully and UI reflects the grouped layout.

## Collection Growth Performance (2026-08-06)
- **Goal**: Move Collection Growth network requests to background tasks.
- **Changed**:
  - Migrated 'New Release' and 'Discovery' fetch logic out of synchronous UI loading into 
efreshArtistGrowthData background worker.
  - Implemented Room entities CachedNewRelease and CachedDiscovery to persist the data locally.
  - Updated MusicViewModel.init with a one-time backfill coroutine.
  - The CollectionGrowthScreen now loads instantly and is completely offline-first.
  - Schema bumped to Version 18 with appropriate migration strategies.
- **Verified**: Builds successfully.

# #   F i x   E A C 3   D u r a t i o n   a n d   B i t r a t e   ( 2 0 2 6 - 0 8 - 1 3 ) 
 
 -   * * G o a l * * :   F i x   E A C 3   D o l b y   A t m o s   f i l e   d u r a t i o n   e x t r a c t i o n   i n   M e d i a S t o r e S c a n n e r   a n d   f i x   b r o k e n   s e e k b a r . 
 
 -   * * C h a n g e d * * : 
 
## Fix EAC3 Duration and Bitrate (2026-08-13)
- **Goal**: Fix EAC3 Dolby Atmos file duration extraction in MediaStoreScanner and fix broken seekbar.
- **Changed**:
  - MediaStoreScanner.kt: The Android MediaMetadataRetriever fails on fragmented MP4/DASH EAC3 files (returns duration 0). Added a custom robust MP4 fallback parser (extractMp4DurationMs) that correctly parses MP4 atoms (moov, moof, 	raf, 	fdt) to compute the total duration from the base decode time of the last fragment. Also dynamically estimates bitrate using file size and the correct duration.
  - MusicPlayerConnection.kt: ExoPlayer polling updated to reflect accurate duration.
- **Verified**: The seek bar UI now receives correct live duration updates directly from ExoPlayer and DB. The rescan correctly saves duration and bitrate to the DB to avoid Low Quality Files false positives in Collection Health, and fixes lyrics sync.

- **Follow-up**: Added `setSpatializationBehavior(C.SPATIALIZATION_BEHAVIOR_AUTO)` and configured `AudioOffloadPreferences` in `PlaybackService.kt` to explicitly engage Android 12+ Spatializer (internal speakers) and hardware offload bitstreaming (HDMI/eARC receivers).

- **Bug 4 - Hashtag in filename**: Fixed a crash where files containing '#' in their name (e.g. '#selfie') failed to play. ExoPlayer treated the string as a raw URI, parsing the '#' as a fragment identifier instead of part of the file path. Wrapped the path in Uri.fromFile(File(path)) inside MusicViewModel to properly URL-encode special characters.

## Add musicmeta as Second-Priority Artist Image Source (2026-08-18)
- **Goal**: Reorder the artist-image fallback chain to add musicmeta as a new step 2, without removing any existing steps, and verify it applies to Collection Growth suggested artists.
- **Changed**:
  - uild.gradle.kts: Added io.github.famesjranko:musicmeta-core and musicmeta-android dependencies (v0.12.0).
  - ArtworkRepository.kt: Inserted a private etchArtistImageViaMusicMeta() helper that creates an EnrichmentEngine on demand. It wires existing SettingsRepository keys for Last.fm and Fanart.tv, keeping them live, and omitting absent keys gracefully.
  - Reordered etchBestArtistImage() to prioritize musicmeta as step 2 (after Deezer track-based precision), and retained MBID-based Fanart/TheAudioDB, Deezer (plain), TheAudioDB (plain), and Last.fm fallbacks as subsequent steps.
- **Verified**: Confirmed etchSimilarArtists() already defers to etchBestArtistImage(), naturally granting suggested artists the same updated priority chain. The code compiles properly with com.landofoz.musicmeta.* imports.

## 2026-08-20 � Animated Play/Pause Morphing Icon

### Goal
Implement a smooth morphing animation between Play and Pause states in the Now Playing and Mini Player screens using ndroidx.graphics.shapes, pulling the exact vector coordinates from Lucide icons.

### Changed Files
- **pp/build.gradle.kts**: Added ndroidx.graphics:graphics-shapes:1.0.1 dependency.
- **PlayPauseMorphIcon.kt**: Created a new reusable composable defining the playPolygon (a 3-point triangle with radius 2) and pausePolygon (a single continuous 12-point shape routing a zero-width gap at y=12 to represent the two distinct bars with radius 1). It computes Morph(play, pause) and scales it to fit the 24dp canvas perfectly.
- **NowPlayingScreen.kt**: Replaced standard Icons.Rounded.PlayArrow and CustomPauseIcon with PlayPauseMorphIcon.
- **ReusableComponents.kt**: Wired PlayPauseMorphIcon into MiniPlayer replacing Icons.Filled.PlayArrow and Icons.Filled.Pause.

### Key Decisions
- No AnimatedVectorDrawable: A pure programmatic approach using Compose graphics avoids XML overhead.
- True Pause Bar Shapes: Used precise polygon modeling with a zero-width connection line at y=12 (middle) and CornerRounding(0f) to allow RoundedPolygon to trace disjoint shapes without leaking corners into the invisible bridge.
- Interpolation: The nimateFloatAsState uses a spring animation with Spring.DampingRatioNoBouncy for a clean premium snappy morph.

### Build
- Compiled successfully with 0 errors.

