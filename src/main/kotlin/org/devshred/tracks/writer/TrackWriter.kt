package org.devshred.tracks.writer

import arrow.core.Either
import io.jenetics.jpx.*
import org.apache.commons.io.FileUtils
import org.devshred.tracks.tcx.writeTcx
import org.devshred.tracks.utils.Config
import org.devshred.tracks.utils.distance
import org.devshred.tracks.utils.findNearestWayPointTo
import org.devshred.tracks.waypoints.CustomPointOfInterest
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar
import java.util.Optional

fun writeTrack(
    prefix: String,
    komootPage: String,
    poIs: Set<CustomPointOfInterest>
) {
    if (komootPage.isEmpty()) return

    val track = downloadTrack(komootPage)
    if (track.isEmpty) return

    val trackName = String.format("%s %03dkm", prefix, track.get().distance())

    val gpx = createGpx(track.get(), poIs, trackName, komootPage)

    writeGpx(gpx, trackName)
    writeTcx(gpx, trackName)
}

private fun downloadTrack(komootPage: String): Optional<GPX> {
    val trackKomootId = komootPage.removePrefix("https://www.komoot.de/tour/")
    val komootCache: String = Config.getProp("outputDir") + "/komoot/"
    val cacheDir = File(komootCache)
    if (!cacheDir.exists()) {
        cacheDir.mkdir()
    }
    val fileCache = File("$komootCache$trackKomootId.gpx")
    if (!fileCache.exists()) {
        val con = URL("https://www.komoot.de/api/v007/tours/$trackKomootId.gpx").openConnection() as HttpURLConnection
        con.requestMethod = "GET"
        con.addRequestProperty("Cookie", Config.getProp("komootCookie"))
        try {
            FileUtils.copyInputStreamToFile(con.inputStream, fileCache)
        } catch (e: IOException) {
            println("an error occurred: ${e.message}")
            println("  -> did you set a valid Komoot-cookie?")
            return Optional.empty()
        } finally {
            con.disconnect()
        }
    }
    return Optional.of(GPX.read(fileCache.toPath()))
}

fun createGpx(
    gpxIn: GPX,
    poIs: Set<CustomPointOfInterest>,
    trackName: String,
    komootPage: String,
    tolerance: Int = 300
): GPX {
    val trackLink =
        Link.of(komootPage, Config.getProp("tourDescription") + "; " + trackName, "trackOnWeb")
    val author = Person.of(
        Config.getProp("copyrightAuthor"),
        Email.of(Config.getProp("autorEmail")),
        Link.of(Config.getProp("autorLink"), Config.getProp("copyrightAuthor"), "KomootUserOnWeb")
    )
    val builder = gpxIn.toBuilder()
        .metadata(
            Metadata.builder()
                .name(trackName)
                .desc(Config.getProp("tourDescription"))
                .author(author)
                .addLink(trackLink)
                .copyright(
                    Copyright.of(
                        Config.getProp("copyrightAuthor"),
                        Calendar.getInstance()[Calendar.YEAR]
                    )
                )
                .build()
        )
    poIs
        .mapNotNull { poi ->
            gpxIn.findNearestWayPointTo(poi, tolerance)
            when (val nearestToPoi = gpxIn.findNearestWayPointTo(poi, tolerance)) {
                is Either.Left -> null
                is Either.Right -> WayPoint.builder()
                    .lat(nearestToPoi.value.latitude)
                    .lon(nearestToPoi.value.longitude)
                    .time(nearestToPoi.value.time.get())
                    .name(poi.getName())
                    .sym(poi.getGpxSym())
                    .type(poi.getTcxType())
                    .build()
            }
        }
        .sortedWith(compareBy { it.time.get() })
        .forEach { builder.addWayPoint(it) }

    builder.trackFilter()
        .map { track: Track ->
            track.toBuilder()
                .name(trackName)
                .addLink(trackLink)
                .build()
        }
        .build()
    return builder.build()
}

private fun writeGpx(gpx: GPX, trackName: String) {
    val gpxDir: String = Config.getProp("outputDir") + "/gpx/"
    if (!File(gpxDir).exists()) {
        File(gpxDir).mkdir()
    }
    val filenameGpx = gpxDir + trackName.replace(" ", "_") + ".gpx"
    GPX.Writer.of(GPX.Writer.Indent(" "), 20).write(gpx, filenameGpx)
    println("wrote file $filenameGpx")
}
