package org.devshred.tracks.waypoints

import org.devshred.tracks.utils.getCoordinatesFromGoogleMapsLink

open class CustomPointOfInterest(
    override val coordinates: Coordinates,
    private val name: String = "",
    private val type: PoiType = PoiType.GENERIC
) : PointOfInterest(coordinates) {
    companion object Factory {
        fun fromMapsLink(link: String, name: String, type: PoiType) =
            CustomPointOfInterest(getCoordinatesFromGoogleMapsLink(link), name, type)
    }

    fun getName(): String = name

    fun getTcxType(): String = type.tcxType

    fun getGpxSym(): String = type.gpxSym
}

enum class PoiType(val tcxType: String, val gpxSym: String) {
    GENERIC("Generic", "generic"),
    SUMMIT("Summit", "summit"),
    VALLEY("Valley", "valley"),
    WATTER("Water", "water"),
    FOOD("Food", "food"),
    DANGER("Danger", "danger"),
    LEFT("Left", "left"),
    RIGHT("Right", "right"),
    STRAIGHT("Straight", "straight"),
    FIRST_AID("First Aid", "generic"),
    FOURTH_CATEGORY("4th Category", "summit"),
    THIRD_CATEGORY("3rd Category", "summit"),
    SECOND_CATEGORY("2nd Category", "summit"),
    FIRST_CATEGORY("1st Category", "summit"),
    HORS_CATEGORY("Hors Category", "summit"),
    RESIDENCE(tcxType = "Residence", gpxSym = "residence"),
    SPRINT("Sprint", "sprint")
}