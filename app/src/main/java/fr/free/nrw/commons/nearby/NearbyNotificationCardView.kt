package fr.free.nrw.commons.nearby

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import fr.free.nrw.commons.R
import fr.free.nrw.commons.contributions.MainActivity
import fr.free.nrw.commons.utils.SwipableCardView
import fr.free.nrw.commons.utils.ViewUtil
import timber.log.Timber

class NearbyNotificationCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SwipableCardView(context, attrs) {

    private val openMapButton: Button
    private val permissionButton: Button
    private val progressBar: ProgressBar
    private val notificationText: TextView
    private val notificationImage: ImageView

    init {
        View.inflate(context, R.layout.layout_nearby_notification, this)
        openMapButton = findViewById(R.id.nearby_open_map)
        permissionButton = findViewById(R.id.nearby_permission)
        progressBar = findViewById(R.id.nearby_progress_bar)
        notificationText = findViewById(R.id.notification_text)
        notificationImage = findViewById(R.id.notification_image)
    }

    fun setOpenMapClickListener(listener: OnClickListener) { openMapButton.setOnClickListener(listener) }
    fun setPermissionButtonClickListener(listener: OnClickListener) { permissionButton.setOnClickListener(listener) }

    fun showProgress() { progressBar.visibility = View.VISIBLE }
    fun hideProgress() { progressBar.visibility = View.GONE }

    fun setNotificationText(text: String) { notificationText.text = text }
    fun setNotificationImage(res: Int) { notificationImage.setImageResource(res) }

    enum class PermissionType { ENABLE_GPS, ENABLE_LOCATION_PERMISSION, NO_PERMISSION_NEEDED }

    fun rotateCompass(rotateDegree: Float, direction: Float) {
        notificationImage.rotation = -(rotateDegree - direction)
    }
}

