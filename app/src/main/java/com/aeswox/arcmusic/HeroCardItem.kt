package com.aeswox.arcmusic

import com.aeswox.arcmusic.db.entities.Album
import com.aeswox.arcmusic.db.entities.Artist
import com.aeswox.arcmusic.db.entities.Track

sealed class HeroCardItem {
    data class TrackItem(val track: Track) : HeroCardItem()
    data class AlbumItem(val album: Album) : HeroCardItem()
    data class ArtistItem(val artist: Artist) : HeroCardItem()
}
