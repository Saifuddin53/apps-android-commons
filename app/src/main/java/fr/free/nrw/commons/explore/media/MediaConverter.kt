package fr.free.nrw.commons.explore.media

import fr.free.nrw.commons.Media
import fr.free.nrw.commons.location.LatLng
import fr.free.nrw.commons.upload.structure.depictions.get
import fr.free.nrw.commons.utils.CommonsDateUtil
import fr.free.nrw.commons.utils.MediaDataExtractorUtil
import fr.free.nrw.commons.wikidata.WikidataProperties
import fr.free.nrw.commons.wikidata.model.DataValue
import fr.free.nrw.commons.wikidata.model.Entities
import fr.free.nrw.commons.wikidata.model.gallery.ExtMetadata
import fr.free.nrw.commons.wikidata.model.gallery.ImageInfo
import fr.free.nrw.commons.wikidata.mwapi.MwQueryPage
import java.text.ParseException
import java.util.Date
import javax.inject.Inject

class MediaConverter
    @Inject
    constructor() {
        fun convert(
            page: MwQueryPage,
            entity: Entities.Entity,
            imageInfo: ImageInfo,
        ): Media {
            val metadata = imageInfo.getMetadata()
            requireNotNull(metadata) { "No metadata" }
            // Stores mapping of title attribute to hidden attribute of each category
            val myMap = mutableMapOf<String, Boolean>()
            page.categories()?.forEach { myMap[it.title()] = (it.hidden()) }

            return Media(
                page.pageId().toString(),
                imageInfo.getThumbUrl().takeIf { it.isNotBlank() } ?: imageInfo.getOriginalUrl(),
                imageInfo.getOriginalUrl(),
                page.title(),
                metadata.imageDescription(),
                safeParseDate(metadata.dateTime()),
                metadata.licenseShortName(),
                metadata.prefixedLicenseUrl,
                getAuthor(metadata),
                imageInfo.getUser(),
                MediaDataExtractorUtil.extractCategoriesFromList(metadata.categories()),
                metadata.latLng,
                entity.labels().mapValues { it.value.value() },
                entity.descriptions().mapValues { it.value.value() },
                entity.depictionIds(),
                myMap,
            )
        }

        /**
         * Creating Media object from MWQueryPage.
         * Earlier only basic details were set for the media object but going forward,
         * a full media object(with categories, descriptions, coordinates etc) can be constructed using this method
         *
         * @param page response from the API
         * @return Media object
         */

        private fun safeParseDate(dateStr: String): Date? =
            try {
                CommonsDateUtil.getMediaSimpleDateFormat().parse(dateStr)
            } catch (e: ParseException) {
                null
            }

        /**
         * This method extracts the Commons Username from the artist HTML information
         * @param metadata
         * @return
         */
        private fun getAuthor(metadata: ExtMetadata): String? {
            return try {
                val authorHtml = metadata.artist()
                val anchorStartTagTerminalChars = "\">"
                val anchorCloseTag = "</a>"

                return authorHtml.substring(
                    authorHtml.indexOf(anchorStartTagTerminalChars) +
                        anchorStartTagTerminalChars
                            .length,
                    authorHtml.indexOf(anchorCloseTag),
                )
            } catch (ex: java.lang.Exception) {
                ""
            }
        }
    }

private fun Entities.Entity.depictionIds() =
    this[WikidataProperties.DEPICTS]?.mapNotNull { (it.mainSnak.dataValue as? DataValue.EntityId)?.value?.id }
        ?: emptyList()

private val ExtMetadata.prefixedLicenseUrl: String
    get() =
        licenseUrl().let {
            if (!it.startsWith("http://") && !it.startsWith("https://")) {
                "https://$it"
            } else {
                it
            }
        }

private val ExtMetadata.latLng: LatLng?
    get() = LatLng.latLongOrNull(gpsLatitude(), gpsLongitude())

