package org.devshred.tracks.writer

import io.jenetics.jpx.*
import org.assertj.core.api.Assertions.assertThat
import org.devshred.tracks.waypoints.Coordinates
import org.devshred.tracks.waypoints.CustomPointOfInterest
import org.junit.jupiter.api.Test
import java.time.Instant

internal class TrackWriterKtTest {
    @Test
    fun `create tracks and filter out pois not in tolerance`() {
        val track = GPX.builder()
            .addTrack { track: Track.Builder ->
                track
                    .addSegment { segment: TrackSegment.Builder ->
                        segment
                            .addPoint { p ->
                                p.lat(0.0).lon(0.0).time(Instant.now())
                            }
                    }
            }
            .build()
        val pointOfInterests = setOf(
            CustomPointOfInterest(Coordinates(Latitude.ofDegrees(1.0), Longitude.ofDegrees(0.0)), "near by"),
            CustomPointOfInterest(Coordinates(Latitude.ofDegrees(2.0), Longitude.ofDegrees(0.0)), "too far away")
        )

        val result = createGpx(track, pointOfInterests, "trackName", "komootPage", 150_000)
        assertThat(result.wayPoints().count()).isEqualTo(1)
        assertThat(result.wayPoints().findFirst().get().name.get()).isEqualTo("near by")
    }
}