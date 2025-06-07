package fr.free.nrw.commons.utils

import org.junit.Assert.assertTrue
import org.junit.Test

class LangCodeUtilsTest {
    @Test
    fun testFixLanguageCodeIw() {
        assertTrue("Expected 'he' as result", LangCodeUtils.fixLanguageCode("iw") == "he")
    }

    @Test
    fun testFixLanguageCodeIn() {
        assertTrue("Expected 'id' as result", LangCodeUtils.fixLanguageCode("in") == "id")
    }

    @Test
    fun testFixLanguageCodeJi() {
        assertTrue("Expected 'yi' as result", LangCodeUtils.fixLanguageCode("ji") == "yi")
    }

    @Test
    fun testFixLanguageCodeDefault() {
        assertTrue("Expected 'en' as result", LangCodeUtils.fixLanguageCode("en") == "en")
    }
}
