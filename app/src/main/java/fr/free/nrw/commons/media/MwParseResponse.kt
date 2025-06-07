package fr.free.nrw.commons.media

import androidx.annotation.Nullable
import androidx.annotation.VisibleForTesting
import fr.free.nrw.commons.wikidata.mwapi.MwResponse

class MwParseResponse : MwResponse() {
    @Nullable
    private var parse: MwParseResult? = null

    @Nullable
    fun parse(): MwParseResult? = parse

    fun success(): Boolean = parse != null

    @VisibleForTesting
    protected fun setParse(parse: MwParseResult?) {
        this.parse = parse
    }
}

