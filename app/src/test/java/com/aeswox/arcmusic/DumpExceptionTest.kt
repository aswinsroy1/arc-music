package com.aeswox.arcmusic

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Robolectric
import java.io.File

@RunWith(RobolectricTestRunner::class)
class DumpExceptionTest {
    @Test
    fun testLaunch() {
        try {
            Robolectric.buildActivity(MainActivity::class.java).create().start().resume().visible()
            File("crash_log.txt").writeText("No crash during launch.")
        } catch (e: Throwable) {
            File("crash_log.txt").writeText(e.stackTraceToString())
        }
    }
}
