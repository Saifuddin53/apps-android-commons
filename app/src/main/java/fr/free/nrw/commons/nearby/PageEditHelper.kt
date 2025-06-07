package fr.free.nrw.commons.nearby

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AlertDialog
import fr.free.nrw.commons.BuildConfig
import fr.free.nrw.commons.R
import fr.free.nrw.commons.actions.PageEditClient
import fr.free.nrw.commons.auth.csrf.InvalidLoginTokenException
import fr.free.nrw.commons.notification.NotificationHelper
import fr.free.nrw.commons.utils.ViewUtilWrapper
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Named

class PageEditHelper @Inject constructor(
    private val pageEditClient: PageEditClient,
    private val notificationHelper: NotificationHelper,
    @Named("direct_nearby_upload_prefs") private val directPrefs: ViewUtilWrapper
) {

    private var dialog: AlertDialog? = null
    private var listener: DialogInterface.OnMultiChoiceClickListener? = null

    fun askUserToAddDescription(context: Context, title: String, wikidataLink: String): AlertDialog {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
            .setMultiChoiceItems(arrayOf(context.getString(R.string.nearby_wikidata_add_description)), booleanArrayOf(false)) { _, which, isChecked ->
                listener?.onClick(null, which, isChecked)
            }
            .setPositiveButton(android.R.string.ok) { _, _ -> addDescription(context, wikidataLink) }
            .setNegativeButton(android.R.string.cancel, null)
        dialog = builder.create()
        dialog!!.show()
        return dialog!!
    }

    private fun addDescription(context: Context, wikidataLink: String) {
        val entityId = wikidataLink.substringAfterLast('/')
        val url = "${BuildConfig.WIKIDATA_URL}/wiki/$entityId"
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    fun getDialog(): AlertDialog? = dialog
    fun getListener(): DialogInterface.OnMultiChoiceClickListener? = listener
}

