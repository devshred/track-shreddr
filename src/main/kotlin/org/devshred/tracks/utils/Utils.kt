package org.devshred.tracks.utils

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.jenetics.jpx.GPX
import io.jenetics.jpx.Track
import io.jenetics.jpx.TrackSegment
import io.jenetics.jpx.WayPoint
import io.jenetics.jpx.geom.Geoid.WGS84
import org.devshred.tracks.waypoints.Coordinates
import org.devshred.tracks.waypoints.PointOfInterest
import org.devshred.tracks.waypoints.coordinatesFromDouble
import org.devshred.tracks.waypoints.coordinatesFromString
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.stream.Stream
import kotlin.math.roundToInt

private val MAPS_COORDINATES_PATTERN = Pattern.compile("@(.*?),(.*?),")
private val MAPS_ISO_6709_PATTERN = Pattern.compile("place/(.*?)%C2%B0(.*?)'(.*?)%22N\\+(.*?)%C2%B0(.*?)'(.*?)%22E/@")
private val MAPS_SHORT_LINK_PATTERN = Pattern.compile("^https://goo.gl/maps/(\\p{Alnum}{17}\$)")

fun getCoordinatesFromGoogleMapsLink(link: String): Coordinates {
    val redirectedLink = if (MAPS_SHORT_LINK_PATTERN.matcher(link).find()) getRedirect(link) else link

    val isoMatcher: Matcher = MAPS_ISO_6709_PATTERN.matcher(redirectedLink)
    if (isoMatcher.find()) {
        val latDeg = isoMatcher.group(1).toInt()
        val latMin = isoMatcher.group(2).toInt()
        val latSec = isoMatcher.group(3).toDouble()
        val longDeg = isoMatcher.group(4).toInt()
        val longMin = isoMatcher.group(5).toInt()
        val longSec = isoMatcher.group(6).toDouble()
        val latitude = latDeg + (latMin * 60 + latSec) / 3600
        val longitude = longDeg + (longMin * 60 + longSec) / 3600
        return coordinatesFromDouble(latitude, longitude)
    }
    val matcher: Matcher = MAPS_COORDINATES_PATTERN.matcher(redirectedLink)
    if (!matcher.find()) throw RuntimeException("coordinates not found")
    return coordinatesFromString(matcher.group(1), matcher.group(2))
}

private fun getRedirect(link: String): String {
    Connection(link).use {
        if (it.connection.responseCode == 302) {
            return it.connection.getHeaderField("Location")
        }
    }
    throw RuntimeException("no redirect found")
}

fun GPX.distance(): Int {
    val length = this.tracks()
        .flatMap { obj: Track -> obj.segments() }
        .findFirst()
        .map { obj: TrackSegment -> obj.points() }.orElse(Stream.empty())
        .collect(WGS84.toPathLength())
    return (length.toFloat() / 1000).roundToInt()
}

fun GPX.findNearestWayPointTo(poi: PointOfInterest, tolerance: Int): Either<BadState, WayPoint> {
    val nearestWayPoint = findNearestWayPointTo(poi)
    return if (WGS84.distance(nearestWayPoint, poi.wayPoint()).toInt() > tolerance) BadState.NotOnTrackError.left()
    else nearestWayPoint.right()
}

fun GPX.findNearestWayPointTo(poi: PointOfInterest): WayPoint {
    return this.tracks().findFirst().get().segments[0].points.stream()
        .reduce { result: WayPoint, current: WayPoint ->
            if (WGS84.distance(current, poi.wayPoint()).toInt()
                < WGS84.distance(result, poi.wayPoint()).toInt()
            ) current else result
        }.get()
}

class Connection(linkAsString: String) : AutoCloseable {
    val connection = URL(linkAsString).openConnection() as HttpURLConnection

    init {
        connection.instanceFollowRedirects = false
        connection.connect()
    }

    override fun close() {
        connection.disconnect()
    }
}

interface Logging

inline fun <reified T : Logging> T.logger(): Logger = LoggerFactory.getLogger(T::class.java)

sealed class BadState {
    object NotOnTrackError : BadState()
}