package com.aeswox.arcmusic.utils

import android.content.Context
import android.media.MediaScannerConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.io.File
import java.net.URL
import java.nio.ByteBuffer
import java.nio.ByteOrder

object TaggingHelper {
    suspend fun downloadImageBytes(urlString: String): ByteArray? = withContext(Dispatchers.IO) {
        try {
            URL(urlString).readBytes()
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Embeds artwork into an audio file.
     *
     * jaudiotagger internally uses javax.imageio.ImageIO when setting artwork on FLAC files,
     * which does not exist on Android. To work around this, for FLAC files we manually encode
     * the METADATA_BLOCK_PICTURE binary block (per FLAC/Vorbis spec) as Base64, then inject
     * it as a raw tag field — completely bypassing jaudiotagger's broken image processing.
     *
     * For MP3 and other formats, we use AndroidArtwork via jaudiotagger normally.
     */
    suspend fun embedArtworkBytes(
        context: Context,
        filePath: String,
        imageBytes: ByteArray,
        mimeType: String = "image/jpeg"
    ): Boolean = withContext(Dispatchers.IO) {
        android.util.Log.d("TaggingHelper", "embedArtworkBytes: filePath=$filePath, bytes=${imageBytes.size}, mime=$mimeType")
        try {
            val file = File(filePath)
            if (!file.exists()) {
                android.util.Log.e("TaggingHelper", "FILE NOT FOUND: $filePath")
                return@withContext false
            }
            if (!file.canWrite()) {
                android.util.Log.e("TaggingHelper", "FILE NOT WRITABLE: $filePath")
                return@withContext false
            }

            val isFlac = filePath.endsWith(".flac", ignoreCase = true)

            if (isFlac) {
                embedArtworkFlac(context, file, imageBytes, mimeType)
            } else {
                embedArtworkMp3OrOther(context, file, imageBytes, mimeType)
            }
        } catch (e: Throwable) {
            android.util.Log.e("TaggingHelper", "EXCEPTION: ${e.javaClass.simpleName}: ${e.message}", e)
            false
        }
    }

    /**
     * For FLAC files: manually encode a METADATA_BLOCK_PICTURE binary block and set it
     * as a raw Vorbis comment field. This completely avoids jaudiotagger's broken
     * javax.imageio.ImageIO call.
     *
     * FLAC METADATA_BLOCK_PICTURE format (all big-endian):
     *   4 bytes: picture type (3 = cover art)
     *   4 bytes: mime type string length
     *   N bytes: mime type string (ASCII)
     *   4 bytes: description string length
     *   N bytes: description string (UTF-8)
     *   4 bytes: width (0 if unknown)
     *   4 bytes: height (0 if unknown)
     *   4 bytes: color depth (0 if unknown)
     *   4 bytes: color count (0 for non-indexed)
     *   4 bytes: image data length
     *   N bytes: image data
     */
    private fun embedArtworkFlac(context: Context, file: File, imageBytes: ByteArray, mimeType: String): Boolean {
        return try {
            val audioFile = AudioFileIO.read(file)
            val tag = audioFile.tagOrCreateAndSetDefault as org.jaudiotagger.tag.flac.FlacTag

            try {
                tag.deleteArtworkField()
            } catch (e: Exception) {
                android.util.Log.w("TaggingHelper", "deleteArtworkField failed (non-fatal): ${e.message}")
            }

            // Decode dimensions without fully loading the bitmap to pass to createArtworkField
            val opts = android.graphics.BitmapFactory.Options().also { it.inJustDecodeBounds = true }
            android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, opts)
            val width = opts.outWidth.takeIf { it > 0 } ?: 0
            val height = opts.outHeight.takeIf { it > 0 } ?: 0

            // Instantiate MetadataBlockDataPicture directly to bypass FlacTag's routing which throws UnsupportedOperationException
            val pictureField = org.jaudiotagger.audio.flac.metadatablock.MetadataBlockDataPicture(
                imageBytes,
                org.jaudiotagger.tag.reference.PictureTypes.DEFAULT_ID,
                mimeType,
                "Cover",
                width,
                height,
                24, // Assuming 24-bit color depth (RGB)
                0   // 0 for non-indexed
            )
            
            tag.setField(pictureField)
            AudioFileIO.write(audioFile)

            android.util.Log.d("TaggingHelper", "embedArtworkFlac: SUCCESS for ${file.name}")

            MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), null) { path, uri ->
                android.util.Log.d("TaggingHelper", "MediaScanner done: $path -> $uri")
            }
            true
        } catch (e: Throwable) {
            android.util.Log.e("TaggingHelper", "embedArtworkFlac EXCEPTION: ${e.javaClass.simpleName}: ${e.message}", e)
            false
        }
    }

    private fun embedArtworkMp3OrOther(context: Context, file: File, imageBytes: ByteArray, mimeType: String): Boolean {
        return try {
            val audioFile = AudioFileIO.read(file)
            val tag = audioFile.tagOrCreateAndSetDefault
            tag.deleteArtworkField()

            val artwork = org.jaudiotagger.tag.images.AndroidArtwork()
            artwork.binaryData = imageBytes
            artwork.mimeType = mimeType
            artwork.pictureType = org.jaudiotagger.tag.reference.PictureTypes.DEFAULT_ID
            artwork.description = "Cover"

            val opts = android.graphics.BitmapFactory.Options().also { it.inJustDecodeBounds = true }
            android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, opts)
            artwork.width = opts.outWidth
            artwork.height = opts.outHeight

            tag.setField(artwork)
            AudioFileIO.write(audioFile)

            android.util.Log.d("TaggingHelper", "embedArtworkMp3OrOther: SUCCESS for ${file.name}")

            MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), null) { path, uri ->
                android.util.Log.d("TaggingHelper", "MediaScanner done: $path -> $uri")
            }
            true
        } catch (e: Throwable) {
            android.util.Log.e("TaggingHelper", "embedArtworkMp3OrOther EXCEPTION: ${e.javaClass.simpleName}: ${e.message}", e)
            false
        }
    }

    suspend fun updateMetadata(
        context: Context,
        filePath: String,
        metadata: Map<FieldKey, String>
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val audioFile = AudioFileIO.read(File(filePath))
            val tag = audioFile.tagOrCreateAndSetDefault
            
            for ((key, value) in metadata) {
                tag.setField(key, value)
            }
            
            AudioFileIO.write(audioFile)
            
            MediaScannerConnection.scanFile(
                context,
                arrayOf(filePath),
                null
            ) { _, _ -> }
            
            true
        } catch (e: Throwable) {
            android.util.Log.e("TaggingHelper", "updateMetadata EXCEPTION: ${e.javaClass.simpleName}: ${e.message}", e)
            false
        }
    }
}
