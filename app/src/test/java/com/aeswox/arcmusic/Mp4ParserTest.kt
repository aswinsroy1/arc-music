package com.aeswox.arcmusic

import org.junit.Test
import org.junit.Assert.*
import java.io.RandomAccessFile

class Mp4ParserTest {

    fun extractMp4DurationMs(filePath: String): Long {
        try {
            val file = RandomAccessFile(filePath, "r")
            var pos = 0L
            val length = file.length()
            var moovFound = false
            while (pos < length) {
                file.seek(pos)
                if (length - pos < 8) break
                var size = file.readInt().toLong() and 0xFFFFFFFFL
                val type = ByteArray(4)
                file.readFully(type)
                val typeStr = String(type)
                println("Box: $typeStr, size: $size, pos: $pos")
                
                var headerLen = 8L
                if (size == 1L) {
                    if (length - pos < 16) break
                    size = file.readLong()
                    headerLen = 16L
                } else if (size == 0L) {
                    size = length - pos
                }
                
                if (size < headerLen) break

                if (typeStr == "moov") {
                    pos += headerLen
                    moovFound = true
                    continue
                } else if (moovFound && typeStr == "mvhd") {
                    val version = file.read()
                    file.read(ByteArray(3))
                    if (version == 1) {
                        file.readLong()
                        file.readLong()
                        val timescale = file.readInt().toLong() and 0xFFFFFFFFL
                        val duration = file.readLong()
                        println("mvhd v1, timescale=$timescale, duration=$duration")
                        // do not return yet, dump others
                    } else {
                        file.readInt()
                        file.readInt()
                        val timescale = file.readInt().toLong() and 0xFFFFFFFFL
                        val duration = file.readInt().toLong() and 0xFFFFFFFFL
                        println("mvhd v0, timescale=$timescale, duration=$duration")
                        // do not return yet
                    }
                    pos += size
                } else if (typeStr == "trak" || typeStr == "mdia") {
                    println("Entering $typeStr")
                    pos += headerLen
                } else if (typeStr == "mdhd") {
                    val version = file.read()
                    file.read(ByteArray(3))
                    if (version == 1) {
                        file.readLong()
                        file.readLong()
                        val timescale = file.readInt().toLong() and 0xFFFFFFFFL
                        val duration = file.readLong()
                        println("mdhd v1, timescale=$timescale, duration=$duration")
                    } else {
                        file.readInt()
                        file.readInt()
                        val timescale = file.readInt().toLong() and 0xFFFFFFFFL
                        val duration = file.readInt().toLong() and 0xFFFFFFFFL
                        println("mdhd v0, timescale=$timescale, duration=$duration")
                    }
                    pos += size
                } else if (typeStr == "moof" || typeStr == "traf") {
                    println("Entering $typeStr")
                    pos += headerLen
                } else if (typeStr == "tfdt") {
                    val version = file.read()
                    file.read(ByteArray(3))
                    val baseDecodeTime = if (version == 1) file.readLong() else file.readInt().toLong() and 0xFFFFFFFFL
                    println("tfdt baseDecodeTime=$baseDecodeTime")
                    pos += size
                } else if (typeStr == "trun") {
                    val version = file.read()
                    val flags = ByteArray(3)
                    file.readFully(flags)
                    val sampleCount = file.readInt()
                    println("trun sampleCount=$sampleCount")
                    pos += size
                } else {
                    pos += size
                }
            }
            file.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0L
    }

    @Test
    fun testMp4Parser() {
        val duration = extractMp4DurationMs("c:/Users/aswin/OneDrive/Desktop/Arc Music/stay.m4a")
        println("Duration: $duration")
        assertTrue("Duration should be > 0", duration > 0)
    }
}
