package com.aeswox.arcmusic.playback.extractor

import android.net.Uri
import androidx.media3.common.C
import androidx.media3.extractor.Extractor
import androidx.media3.extractor.ExtractorInput
import androidx.media3.extractor.ExtractorOutput
import androidx.media3.extractor.ExtractorsFactory
import androidx.media3.extractor.PositionHolder
import androidx.media3.extractor.SeekMap
import androidx.media3.extractor.SeekPoint
import androidx.media3.extractor.TrackOutput
import androidx.media3.extractor.mp4.FragmentedMp4Extractor
import androidx.media3.extractor.DefaultExtractorsFactory
import java.io.RandomAccessFile

class CustomExtractorsFactory(private val filePath: String? = null) : ExtractorsFactory {
    private val defaultFactory = DefaultExtractorsFactory()
        .setConstantBitrateSeekingEnabled(true)

    override fun createExtractors(): Array<Extractor> {
        return createExtractors(Uri.EMPTY, mutableMapOf())
    }

    override fun createExtractors(uri: Uri, responseHeaders: MutableMap<String, MutableList<String>>): Array<Extractor> {
        val extractors = defaultFactory.createExtractors(uri, responseHeaders)
        val path = filePath ?: uri.path
        if (path != null && (path.endsWith(".m4a", true) || path.endsWith(".mp4", true))) {
            for (i in extractors.indices) {
                if (extractors[i].javaClass.simpleName == "FragmentedMp4Extractor") {
                    extractors[i] = SeekableFragmentedMp4Extractor(extractors[i], path)
                }
            }
        }
        return extractors
    }
}

class SeekableFragmentedMp4Extractor(
    private val delegate: Extractor,
    private val filePath: String
) : Extractor {

    override fun sniff(input: ExtractorInput): Boolean = delegate.sniff(input)

    override fun init(output: ExtractorOutput) {
        delegate.init(object : ExtractorOutput {
            override fun track(id: Int, type: Int): TrackOutput = output.track(id, type)
            override fun endTracks() = output.endTracks()

            override fun seekMap(sm: SeekMap) {
                if (!sm.isSeekable) {
                    val customMap = buildSeekMap(filePath)
                    if (customMap != null) {
                        output.seekMap(customMap)
                        return
                    }
                }
                output.seekMap(sm)
            }
        })
    }

    override fun read(input: ExtractorInput, seekPosition: PositionHolder): Int = delegate.read(input, seekPosition)

    override fun seek(position: Long, timeUs: Long) = delegate.seek(position, timeUs)

    override fun release() = delegate.release()

    private fun buildSeekMap(path: String): SeekMap? {
        try {
            val file = RandomAccessFile(path, "r")
            val length = file.length()
            var pos = 0L
            var timescale = 0L
            val offsets = mutableListOf<Long>()
            val timesUs = mutableListOf<Long>()

            while (pos < length) {
                file.seek(pos)
                if (length - pos < 8) break
                var size = file.readInt().toLong() and 0xFFFFFFFFL
                val type = ByteArray(4)
                file.readFully(type)
                val typeStr = String(type)

                var headerLen = 8L
                if (size == 1L) {
                    if (length - pos < 16) break
                    size = file.readLong()
                    headerLen = 16L
                } else if (size == 0L) {
                    size = length - pos
                }
                if (size < headerLen) break

                when (typeStr) {
                    "moov", "trak", "mdia", "traf" -> {
                        pos += headerLen
                    }
                    "mdhd" -> {
                        val version = file.read()
                        file.read(ByteArray(3))
                        if (version == 1) {
                            file.readLong(); file.readLong()
                            timescale = file.readInt().toLong() and 0xFFFFFFFFL
                        } else {
                            file.readInt(); file.readInt()
                            timescale = file.readInt().toLong() and 0xFFFFFFFFL
                        }
                        pos += size
                    }
                    "moof" -> {
                        // Store the byte offset of the moof box. This is where FragmentedMp4Extractor can seek to.
                        val moofPos = pos
                        pos += headerLen // dive in to find tfdt
                        var foundTfdt = false
                        while (pos < moofPos + size && !foundTfdt) {
                            file.seek(pos)
                            if (length - pos < 8) break
                            var innerSize = file.readInt().toLong() and 0xFFFFFFFFL
                            val innerType = ByteArray(4)
                            file.readFully(innerType)
                            val innerTypeStr = String(innerType)
                            
                            var innerHeaderLen = 8L
                            if (innerSize == 1L) {
                                innerSize = file.readLong()
                                innerHeaderLen = 16L
                            }
                            
                            if (innerTypeStr == "traf") {
                                pos += innerHeaderLen
                            } else if (innerTypeStr == "tfdt") {
                                val v = file.read()
                                file.read(ByteArray(3))
                                val baseDecodeTime = if (v == 1) file.readLong() else file.readInt().toLong() and 0xFFFFFFFFL
                                if (timescale > 0) {
                                    val timeUs = (baseDecodeTime * 1000000L) / timescale
                                    offsets.add(moofPos)
                                    timesUs.add(timeUs)
                                }
                                foundTfdt = true
                                pos = moofPos + size // skip rest of moof
                            } else {
                                pos += innerSize
                            }
                        }
                        if (!foundTfdt) {
                            pos = moofPos + size
                        }
                    }
                    else -> {
                        pos += size
                    }
                }
            }
            file.close()

            if (offsets.isNotEmpty() && timesUs.isNotEmpty()) {
                val totalDurationUs = timesUs.last() // We will use the last tfdt as approximate duration
                return object : SeekMap {
                    override fun isSeekable(): Boolean = true
                    override fun getDurationUs(): Long = totalDurationUs
                    
                    override fun getSeekPoints(timeUs: Long): SeekMap.SeekPoints {
                        // Binary search to find the closest timeUs
                        var index = timesUs.binarySearch(timeUs)
                        if (index < 0) {
                            index = -(index + 1) - 1
                        }
                        if (index < 0) index = 0
                        if (index >= timesUs.size) index = timesUs.size - 1
                        
                        val point = SeekPoint(timesUs[index], offsets[index])
                        return SeekMap.SeekPoints(point)
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("SeekableMp4", "Failed to build seek map for $path", e)
        }
        return null
    }
}
