package fr.free.nrw.commons.nearby

/**
 * Holds current filter selections for the Nearby screen
 */
class NearbyFilterState private constructor() {
    var existsSelected: Boolean = true
    var needPhotoSelected: Boolean = true
    var wlmSelected: Boolean = true
    var checkBoxTriState: Int = -1
    var selectedLabels: ArrayList<Label> = ArrayList()

    companion object {
        private var nearbyFilterStateInstance: NearbyFilterState? = null
        fun getInstance(): NearbyFilterState {
            if (nearbyFilterStateInstance == null) {
                nearbyFilterStateInstance = NearbyFilterState()
            }
            return nearbyFilterStateInstance!!
        }

        fun setSelectedLabels(selectedLabels: ArrayList<Label>) {
            getInstance().selectedLabels = selectedLabels
        }

        fun setExistsSelected(existsSelected: Boolean) {
            getInstance().existsSelected = existsSelected
        }

        fun setNeedPhotoSelected(needPhotoSelected: Boolean) {
            getInstance().needPhotoSelected = needPhotoSelected
        }

        fun setWlmSelected(wlmSelected: Boolean) {
            getInstance().wlmSelected = wlmSelected
        }
    }
}
