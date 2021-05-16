package org.devshred.tracks.waypoints

import org.devshred.tracks.utils.getCoordinatesFromGoogleMapsLink
import org.devshred.tracks.waypoints.PoiType.RESIDENCE

class Hotel(coordinates: Coordinates, name: String) :
    CustomPointOfInterest(coordinates, name, type = RESIDENCE) {
    companion object Factory {
        fun fromRow(row: List<Any>): Hotel {
            return Hotel(
                coordinates = getCoordinatesFromGoogleMapsLink(row[13] as String),
                name = row[11] as String
            )
        }
    }
}
