package fr.free.nrw.commons.nearby

import androidx.annotation.DrawableRes
import fr.free.nrw.commons.R

/**
 * Most common types of description for Nearby places
 */
enum class Label(val text: String, @DrawableRes val icon: Int) {
    BOOKMARKS("BOOKMARK", R.drawable.ic_filled_star_24dp),
    BUILDING("Q41176", R.drawable.round_icon_generic_building),
    HOUSE("Q3947", R.drawable.round_icon_house),
    COTTAGE("Q5783996", R.drawable.round_icon_house),
    FARMHOUSE("Q489357", R.drawable.round_icon_house),
    CHURCH("Q16970", R.drawable.round_icon_church),
    RAILWAY_STATION("Q55488", R.drawable.round_icon_railway_station),
    GATEHOUSE("Q277760", R.drawable.round_icon_gatehouse),
    MILESTONE("Q10145", R.drawable.round_icon_milestone),
    INN("Q256020", R.drawable.round_icon_house),
    HOTEL("Q27686", R.drawable.round_icon_house),
    CITY("Q515", R.drawable.round_icon_city),
    UNIVERSITY("Q3918", R.drawable.round_icon_school),
    SCHOOL("Q3914", R.drawable.round_icon_school),
    EDUCATION("Q8434", R.drawable.round_icon_school),
    ISLE("Q23442", R.drawable.round_icon_island),
    MOUNTAIN("Q8502", R.drawable.round_icon_mountain),
    AIRPORT("Q1248784", R.drawable.round_icon_airport),
    BRIDGE("Q12280", R.drawable.round_icon_bridge),
    ROAD("Q34442", R.drawable.round_icon_road),
    FOREST("Q4421", R.drawable.round_icon_forest),
    PARK("Q22698", R.drawable.round_icon_park),
    RIVER("Q4022", R.drawable.round_icon_river),
    WATERFALL("Q34038", R.drawable.round_icon_waterfall),
    TEMPLE("Q44539", R.drawable.round_icon_church),
    UNKNOWN("?", R.drawable.round_icon_unknown);

    var selected: Boolean = false

    companion object {
        val TEXT_TO_DESCRIPTION: Map<String, Label> = values().associateBy { it.text }
        fun fromText(text: String): Label = TEXT_TO_DESCRIPTION[text] ?: UNKNOWN
        fun valuesAsList(): List<Label> = values().toList()
    }
}
