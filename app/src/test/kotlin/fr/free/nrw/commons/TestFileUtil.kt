package fr.free.nrw.commons

import android.annotation.TargetApi
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.StringWriter
import java.nio.charset.StandardCharsets
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils

object TestFileUtil {
    private const val RAW_DIR = "src/test/res/raw/"

    fun getRawFile(rawFileName: String): File = File(RAW_DIR + rawFileName)

    @Throws(IOException::class)
    fun readRawFile(basename: String): String = readFile(getRawFile(basename))

    @TargetApi(19)
    @Throws(IOException::class)
    private fun readFile(file: File): String = FileUtils.readFileToString(file, StandardCharsets.UTF_8)

    @TargetApi(19)
    @Throws(IOException::class)
    fun readStream(stream: InputStream): String {
        val writer = StringWriter()
        IOUtils.copy(stream, writer, StandardCharsets.UTF_8)
        return writer.toString()
    }
}
