package fr.free.nrw.commons

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import fr.free.nrw.commons.location.LatLng
import fr.free.nrw.commons.settings.Prefs
import fr.free.nrw.commons.utils.ViewUtil
import fr.free.nrw.commons.wikidata.model.WikiSite
import fr.free.nrw.commons.wikidata.model.page.PageTitle
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern
import timber.log.Timber

object Utils {

    fun getPageTitle(title: String): PageTitle {
        return PageTitle(title, WikiSite(BuildConfig.COMMONS_URL))
    }

    fun licenseNameFor(license: String): Int = when (license) {
        Prefs.Licenses.CC_BY_3 -> R.string.license_name_cc_by
        Prefs.Licenses.CC_BY_4 -> R.string.license_name_cc_by_four
        Prefs.Licenses.CC_BY_SA_3 -> R.string.license_name_cc_by_sa
        Prefs.Licenses.CC_BY_SA_4 -> R.string.license_name_cc_by_sa_four
        Prefs.Licenses.CC0 -> R.string.license_name_cc0
        else -> throw IllegalStateException("Unrecognized license value: $license")
    }

    @NonNull
    fun licenseUrlFor(license: String): String = when (license) {
        Prefs.Licenses.CC_BY_3 -> "https://creativecommons.org/licenses/by/3.0/"
        Prefs.Licenses.CC_BY_4 -> "https://creativecommons.org/licenses/by/4.0/"
        Prefs.Licenses.CC_BY_SA_3 -> "https://creativecommons.org/licenses/by-sa/3.0/"
        Prefs.Licenses.CC_BY_SA_4 -> "https://creativecommons.org/licenses/by-sa/4.0/"
        Prefs.Licenses.CC0 -> "https://creativecommons.org/publicdomain/zero/1.0/"
        else -> throw IllegalStateException("Unrecognized license value: $license")
    }

    fun fixExtension(title: String, extensionParam: String?): String {
        var ext = extensionParam
        val jpegPattern = Pattern.compile("\\.jpeg$", Pattern.CASE_INSENSITIVE)
        if (ext != null && ext.lowercase(Locale.ENGLISH) == "jpeg") {
            ext = "jpg"
        }
        var newTitle = jpegPattern.matcher(title).replaceFirst(".jpg")
        if (ext != null && !newTitle.lowercase(Locale.getDefault()).endsWith("." + ext.lowercase(Locale.ENGLISH))) {
            newTitle += "." + ext
        }
        if (ext == null && newTitle.lastIndexOf('.') <= 0) {
            ext = "jpg"
            newTitle += "." + ext
        }
        return newTitle
    }

    fun rateApp(context: Context) {
        val appPackageName = context.packageName
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Urls.PLAY_STORE_PREFIX + appPackageName)))
        } catch (anfe: android.content.ActivityNotFoundException) {
            handleWebUrl(context, Uri.parse(Urls.PLAY_STORE_URL_PREFIX + appPackageName))
        }
    }

    fun handleWebUrl(context: Context, url: Uri) {
        Timber.d("Launching web url %s", url.toString())
        val color = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(ContextCompat.getColor(context, R.color.primaryColor))
            .setSecondaryToolbarColor(ContextCompat.getColor(context, R.color.primaryDarkColor))
            .build()
        val builder = CustomTabsIntent.Builder()
        builder.setDefaultColorSchemeParams(color)
        builder.setExitAnimations(context, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        val customTabsIntent = builder.build()
        customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        customTabsIntent.launchUrl(context, url)
    }

    fun handleGeoCoordinates(context: Context, latLng: LatLng) {
        handleGeoCoordinates(context, latLng, 16.0)
    }

    fun handleGeoCoordinates(context: Context, latLng: LatLng, zoomLevel: Double) {
        val mapIntent = Intent(Intent.ACTION_VIEW, latLng.getGmmIntentUri(zoomLevel))
        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            ViewUtil.showShortToast(context, context.getString(R.string.map_application_missing))
        }
    }

    fun getScreenShot(view: View): Bitmap? {
        val screenView = view.rootView
        screenView.isDrawingCacheEnabled = true
        val drawingCache = screenView.drawingCache
        return if (drawingCache != null) {
            val bitmap = Bitmap.createBitmap(drawingCache)
            screenView.isDrawingCacheEnabled = false
            bitmap
        } else {
            null
        }
    }

    fun copy(label: String, text: String, context: Context) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
    }

    fun setUnderlinedText(textView: TextView, stringResourceName: Int, context: Context) {
        val content = SpannableString(context.getString(stringResourceName))
        content.setSpan(UnderlineSpan(), 0, content.length, 0)
        textView.text = content
    }

    fun isMonumentsEnabled(date: Date): Boolean {
        return date.month == 8
    }

    fun getWLMStartDate(): String = "1 Sep"

    fun getWLMEndDate(): String = "30 Sep"

    fun getWikiLovesMonumentsYear(calendar: Calendar): Int {
        var year = calendar.get(Calendar.YEAR)
        if (calendar.get(Calendar.MONTH) < Calendar.SEPTEMBER) {
            year -= 1
        }
        return year
    }
}

