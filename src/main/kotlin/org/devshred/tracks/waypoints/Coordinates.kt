package org.devshred.tracks.waypoints

import io.jenetics.jpx.Latitude
import io.jenetics.jpx.Longitude

data class Coordinates(val latitude: Latitude, val longitude: Longitude)

fun coordinatesFromString(latitude: String, longitude: String) =
    coordinatesFromDouble(latitude.toDouble(), longitude.toDouble())

fun coordinatesFromDouble(latitude: Double, longitude: Double) =
    Coordinates(Latitude.ofDegrees(latitude), Longitude.ofDegrees(longitude))