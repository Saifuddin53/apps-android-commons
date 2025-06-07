package fr.free.nrw.commons.explore.map

import fr.free.nrw.commons.kvstore.JsonKvStore
import fr.free.nrw.commons.location.LocationServiceManager
import javax.inject.Inject

class ExploreMapPresenter @Inject constructor() : ExploreMapContract.UserActions {

    private var view: ExploreMapContract.View? = null

    override fun updateMap(locationChangeType: LocationServiceManager.LocationChangeType) {}

    override fun lockUnlockNearby(isNearbyLocked: Boolean) {}

    override fun attachView(view: ExploreMapContract.View) {
        this.view = view
    }

    override fun detachView() {
        view = null
    }

    override fun setActionListeners(applicationKvStore: JsonKvStore) {}

    override fun backButtonClicked(): Boolean = false
}

