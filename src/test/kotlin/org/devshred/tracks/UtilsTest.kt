package org.devshred.tracks

import io.jenetics.jpx.*
import org.assertj.core.api.Assertions.assertThat
import org.devshred.tracks.utils.distance
import org.devshred.tracks.utils.findNearestWayPointTo
import org.devshred.tracks.utils.getCoordinatesFromGoogleMapsLink
import org.devshred.tracks.waypoints.Coordinates
import org.devshred.tracks.waypoints.PointOfInterest
import org.devshred.tracks.waypoints.coordinatesFromDouble
import org.junit.jupiter.api.Test
import java.nio.file.Path
import io.jenetics.jpx.WayPoint.builder as WayPointBuilder

internal class UtilsTest {
    @Test
    fun `calculate distance from GPX file`() {
        val pathToGpxFile = this::class.java.classLoader.getResource("track.gpx").path
        val gpx = GPX.read(Path.of(pathToGpxFile))
        val distance = gpx.distance()
        assertThat(distance).isEqualTo(72)
    }

    @Test
    fun `find nearest waypoint`() {
        val gpx = GPX.builder()
            .addTrack { track: Track.Builder ->
                track
                    .addSegment { segment: TrackSegment.Builder ->
                        segment
                            .addPoint { p ->
                                p.lat(1.0).lon(1.0)
                            }
                            .addPoint { p ->
                                p.lat(3.0).lon(3.0)
                            }
                            .addPoint { p ->
                                p.lat(3.0).lon(3.0)
                            }
                    }
            }
            .build()

        val nearestWayPoint = gpx.findNearestWayPointTo(
            PointOfInterest(
                Coordinates(
                    Latitude.ofDegrees(2.0),
                    Longitude.ofDegrees(3.0)
                )
            )
        )

        assertThat(nearestWayPoint).isEqualTo(WayPointBuilder().lat(3.0).lon(3.0).build())
    }

    @Test
    fun `should find nearest waypoint if difference to track is in tolerance`() {
        val gpx = GPX.builder()
            .addTrack { track: Track.Builder ->
                track
                    .addSegment { segment: TrackSegment.Builder ->
                        segment
                            .addPoint { p ->
                                p.lat(0.0).lon(0.0)
                            }
                    }
            }
            .build()

        val nearestWayPoint = gpx.findNearestWayPointTo(
            PointOfInterest(
                Coordinates(
                    Latitude.ofDegrees(1.0),
                    Longitude.ofDegrees(0.0)
                )
            ), 111_000
        )

        assertThat(nearestWayPoint.isRight()).isTrue
    }

    @Test
    fun `filter out pois too far away from any waypoint of a given track`() {
        val gpx = GPX.builder()
            .addTrack { track: Track.Builder ->
                track
                    .addSegment { segment: TrackSegment.Builder ->
                        segment
                            .addPoint { p ->
                                p.lat(0.0).lon(0.0)
                            }
                    }
            }
            .build()

        val nearestWayPoint = gpx.findNearestWayPointTo(
            PointOfInterest(
                Coordinates(
                    Latitude.ofDegrees(1.0),
                    Longitude.ofDegrees(0.0)
                )
            ), 110_000
        )

        assertThat(nearestWayPoint.isLeft()).isTrue
    }

    @Test
    fun `get coordinates using link with MAPS_COORDINATES_PATTERN`() {
        val link =
            "https://www.google.de/maps/place/He%C5%99manice+v+Podje%C5%A1t%C4%9Bd%C3%AD+280,+471+25+Jablonn%C3%A9+v+Podje%C5%A1t%C4%9Bd%C3%AD,+Tschechien/@50.7994483,14.7130303,19z/data=!4m5!3m4!1s0x47091586c48ffee1:0x575c11f51d137430!8m2!3d50.7993257!4d14.7136927"
        val coordinates = getCoordinatesFromGoogleMapsLink(link)
        assertThat(coordinates).isEqualTo(coordinatesFromDouble(50.7994483, 14.7130303))
    }

    @Test
    fun `get coordinates using link with MAPS_ISO_6709_PATTERN`() {
        val link =
            "https://www.google.de/maps/place/50%C2%B049'45.0%22N+14%C2%B033'57.2%22E/@50.8291667,14.5637002,17z/data=!3m1!4b1!4m6!3m5!1s0x0:0x0!7e2!8m2!3d50.8291579!4d14.5658915"
        val coordinates = getCoordinatesFromGoogleMapsLink(link)
        assertThat(coordinates).isEqualTo(coordinatesFromDouble(50.829166666666666, 14.565888888888889))
    }

    @Test
    fun `get coordinates following redirect`() {
        val link = "https://goo.gl/maps/YwWoPZ4t3vBb1vBPA"
        val coordinates = getCoordinatesFromGoogleMapsLink(link)
        assertThat(coordinates).isEqualTo(coordinatesFromDouble(50.7994483, 14.7130303))
    }
}