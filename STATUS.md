# Status

**Current State**:
- Initial setup and structural tasks completed.
- Renamed package to `com.aeswox.arcmusic`.
- Updated `minSdk` to 31.
- Updated app name to "Arc Music" and base theme to `Theme.ArcMusic`.
- Cleaned up README.md to remove AI Studio references.
- Added Hilt for Dependency Injection, registered ArcMusicApplication, and converted MusicViewModel.
- Fixed missing content algorithm to also show full singles in a dedicated section on the Missing Content Screen.
- Re-architected background gap fetches to run fully concurrently without dropping results on the floor.
- Added Fanart.tv API key integration and updated Dolby Atmos icon tinting.
- Repo hygiene: Cleaned up 160+ root-level scratch scripts (.py, .ps1, .txt, .log, .kt, .sh) and updated .gitignore to prevent recurrence.
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
- Built `MissingContentScreen.kt` with "Partial Albums" and "Entire Albums" tabs. Tapping a Partial Album card expands it to show the exact missing track names fetched from MusicBrainz.
- Fixed core bug: MusicBrainz's `/ws/2/release` search endpoint never returns inline `recordings`, so track names were always empty. Fixed by adding a `getReleaseById()` endpoint and doing a per-release lookup for any album that needs its tracklist.
- Added real local caching via `CachedMissingContent` Room entity: stores track names as JSON, `cachedAt` timestamp, and uses delete-before-insert to avoid duplication on re-fetch. Staleness policy: 7 days, matching the original plan.
- Auto-triggers discography scan the moment an artist is favorited via a structured `CoroutineScope(SupervisorJob())` in `MusicRepository`, replacing the previous fragile `GlobalScope.launch`.
- Fixed `scanMediaStore()` to preserve the `hasScannedMissingContent` flag, preventing the DB scan from wiping the cache on every app restart.
  - **Wired `CollectionGrowthScreen.kt` with real data across all four card types:**
  - **Reorganized UI**: Grouped into expandable/collapsible sections by type.
  - **Complete Collection** — reads from `CachedMissingContent` (same data as Missing Content screen). Shows when ≤2 albums missing and artist is ≥50% complete. Expandable.
  - **New Release** — queries MusicBrainz `release-group` endpoint for releases in the last 90 days not in local library. Cached 7 days. Throttled at 1.2s per artist. Expandable.
  - **Discovery** — calls Last.fm `artist.getSimilar` + `artist.getTopTags`. Horizontal chip row + Bottom Sheet. Gated entirely on Last.fm API key presence in Settings.
  - **Missing Tracks** — reads partial-album entries from `CachedMissingContent`. Shows progress bar (owned / total tracks). Expandable.
  - **Dismiss** — tapping Dismiss removes the card immediately and persists the dismissal to a new `dismissed_growth_cards` Room table. Dismissed cards never reappear.
  - **Fixed broken buttons** — "Search Jellyfin" replaced with real "Open Spotify" intents; "Mark as Downloaded" replaced with "Dismiss".
  - **Empty state** — handled gracefully: shows icon + message if no favorited artists.
  - **Optimized Loading** — Moved "New Release" and "Discovery" data fetches into the background, caching them locally via Room (`cached_new_releases` and `cached_discoveries` tables). The screen now loads instantly from DB. 
  - **Background Backfill** — Triggers updates for favorited artists automatically via `refreshArtistGrowthData`, enforcing staleness thresholds (7 days).
  - DB bumped to version 18.
- **Extended Collection Growth pipeline to include top-listened artists**: An artist now qualifies for New Release / Discovery / Missing Tracks fetches if they are either favorited **or** in the top 10 most-listened artists (by accumulated listening time from `PlayHistory`). The two sets are merged and deduplicated by artist id so no artist is fetched twice. The top-listened set is recomputed on each backfill cycle (same 7-day staleness cadence). No DB schema changes, no UI changes, no throttling changes. `CollectionGrowthData.hasFavoritedArtists` renamed to `hasQualifyingArtists` to reflect the broader scope.


**What's Next**:
- Wire Home screen "Recently Played" section to `getRecentlyPlayedTracks()` — real data now exists.
- Wire Listening Stats screen to real `play_history` data.
- Wire `PlaylistDetailsScreen.kt` to real data.
- Verify functionality of the Library tab in the actual app instance.

**Known Issues**:
- Project is missing the `arc-music-build-plan.md` referenced in global guidelines.
- Acceptance check (DB query post-playback) requires a connected device — run `check_play_history.ps1` after playing tracks.
- Last.fm API Key must be set in Settings → Last.fm for Discovery cards to appear. Without a key, the Discovery section is silently skipped (no error).
- Missing content scan takes longer now (correct behaviour) because it does a per-album release lookup to MusicBrainz to get actual track names. This is only done once per artist (cached for 7 days).
- New Release cards are similarly slow on first load (per-artist MBID resolution + MusicBrainz query). Subsequent loads use the 7-day cache and are instant.
