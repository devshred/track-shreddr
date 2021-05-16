package org.devshred.tracks.waypoints

import io.jenetics.jpx.WayPoint

open class PointOfInterest(open val coordinates: Coordinates) {
    fun wayPoint(): WayPoint {
        return WayPoint.of(coordinates.latitude, coordinates.longitude)
    }
}
