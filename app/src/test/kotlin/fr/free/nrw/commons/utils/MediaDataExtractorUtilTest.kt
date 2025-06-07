package fr.free.nrw.commons.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class MediaDataExtractorUtilTest {

    @Test
    fun extractCategoriesFromList() {
        val strings = MediaDataExtractorUtil.extractCategoriesFromList("Watercraft 2018|Watercraft|2018")
        assertEquals(3, strings.size)
    }

    @Test
    fun extractCategoriesFromEmptyList() {
        val strings = MediaDataExtractorUtil.extractCategoriesFromList("")
        assertEquals(0, strings.size)
    }

    @Test
    fun extractCategoriesFromNullList() {
        val strings = MediaDataExtractorUtil.extractCategoriesFromList(null)
        assertEquals(0, strings.size)
    }

    @Test
    fun extractCategoriesFromListWithEmptyValues() {
        val strings = MediaDataExtractorUtil.extractCategoriesFromList("Watercraft 2018||")
        assertEquals(1, strings.size)
    }

    @Test
    fun extractCategoriesFromListWithWhitespaces() {
        val strings = MediaDataExtractorUtil.extractCategoriesFromList("Watercraft 2018| | ||")
        assertEquals(1, strings.size)
    }
}
