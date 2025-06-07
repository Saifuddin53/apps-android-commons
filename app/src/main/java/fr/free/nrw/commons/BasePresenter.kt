package fr.free.nrw.commons

import androidx.annotation.NonNull

/**
 * Base presenter, enforcing contracts to attach and detach view
 */
interface BasePresenter<T> {
    /** Until a view is attached, it is open to listen events from the presenter */
    fun onAttachView(view: T)

    /** Detaching a view makes sure that the view no more receives events */
    fun onDetachView()
}
