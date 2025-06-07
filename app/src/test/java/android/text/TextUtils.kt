package android.text

object TextUtils {
    fun isEmpty(str: CharSequence?): Boolean = str == null || str.isEmpty()

    fun equals(a: CharSequence?, b: CharSequence?): Boolean {
        if (a === b) return true
        if (a != null && b != null && a.length == b.length) {
            if (a is String && b is String) {
                return a == b
            }
            for (i in a.indices) {
                if (a[i] != b[i]) return false
            }
            return true
        }
        return false
    }

    fun isDigitsOnly(str: CharSequence): Boolean {
        var i = 0
        val len = str.length
        while (i < len) {
            val cp = Character.codePointAt(str, i)
            if (!Character.isDigit(cp)) {
                return false
            }
            i += Character.charCount(cp)
        }
        return true
    }

    private fun isNewline(codePoint: Int): Boolean {
        val type = Character.getType(codePoint)
        return type == Character.PARAGRAPH_SEPARATOR ||
            type == Character.LINE_SEPARATOR ||
            codePoint == 10
    }

    fun isGraphic(str: CharSequence): Boolean {
        var i = 0
        val len = str.length
        while (i < len) {
            val cp = Character.codePointAt(str, i)
            val gc = Character.getType(cp)
            if (gc != Character.CONTROL &&
                gc != Character.FORMAT &&
                gc != Character.SURROGATE &&
                gc != Character.UNASSIGNED &&
                gc != Character.LINE_SEPARATOR &&
                gc != Character.PARAGRAPH_SEPARATOR &&
                gc != Character.SPACE_SEPARATOR) {
                return true
            }
            i += Character.charCount(cp)
        }
        return false
    }
}
