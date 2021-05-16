package org.devshred.tracks.waypoints

import org.devshred.tracks.utils.getCoordinatesFromGoogleMapsLink
import org.devshred.tracks.waypoints.PoiType.FOOD

open class Buffet(coordinates: Coordinates) : CustomPointOfInterest(coordinates, name = "Buffet", type = FOOD) {
    companion object {
        fun fromRow(row: List<Any>): Buffet {
            if ((row[10] as String).isEmpty()) return EmptyBuffet
            return Buffet(getCoordinatesFromGoogleMapsLink(row[10] as String))
        }
    }

    fun isEmptyBuffet() = this === EmptyBuffet
}

object EmptyBuffet : Buffet(coordinatesFromDouble(0.0, 0.0))
