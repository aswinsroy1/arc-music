package com.aeswox.arcmusic.utils

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.images.ArtworkFactory
import java.io.File
import java.net.URL

object TaggingHelper {
    suspend fun downloadImageBytes(urlString: String): ByteArray? = withContext(Dispatchers.IO) {
        try {
            URL(urlString).readBytes()
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }
    }

    suspend fun embedArtworkBytes(
        context: Context,
        filePath: String,
        imageBytes: ByteArray,
        mimeType: String = "image/jpeg"
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val audioFile = AudioFileIO.read(File(filePath))
            val tag = audioFile.tagOrCreateAndSetDefault
            
            // Delete existing artwork
            tag.deleteArtworkField()
            
            // Create new artwork
            val artwork = org.jaudiotagger.tag.images.AndroidArtwork()
            artwork.binaryData = imageBytes
            artwork.mimeType = mimeType
            artwork.description = "Cover"
            artwork.pictureType = org.jaudiotagger.tag.reference.PictureTypes.DEFAULT_ID
            
            tag.setField(artwork)
            AudioFileIO.write(audioFile)
            
            // Trigger MediaScanner to pick up the new artwork
            MediaScannerConnection.scanFile(
                context,
                arrayOf(filePath),
                null
            ) { path, uri ->
                // Scan completed
            }
            
            true
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        }
    }
}
