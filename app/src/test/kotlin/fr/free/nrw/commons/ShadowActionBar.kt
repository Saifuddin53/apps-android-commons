package fr.free.nrw.commons

import androidx.appcompat.app.ActionBar
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements

@Implements(ActionBar::class)
class ShadowActionBar {
    var showHomeAsUp: Boolean = false
        private set

    @Implementation
    fun setDisplayHomeAsUpEnabled(showHomeAsUp: Boolean) {
        this.showHomeAsUp = showHomeAsUp
    }
}
