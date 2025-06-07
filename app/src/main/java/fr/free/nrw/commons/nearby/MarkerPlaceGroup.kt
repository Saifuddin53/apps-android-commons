package fr.free.nrw.commons.nearby

/**
 * Groups a map marker with its corresponding [Place] and bookmark state.
 */
data class MarkerPlaceGroup(
    var isBookmarked: Boolean,
    var place: Place
)
