package org.devshred.tracks.tcx

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator
import io.jenetics.jpx.GPX
import io.jenetics.jpx.Length
import io.jenetics.jpx.WayPoint
import io.jenetics.jpx.geom.Geoid
import org.apache.commons.lang3.StringUtils
import org.devshred.tracks.utils.Config
import java.io.File
import java.time.temporal.ChronoUnit.SECONDS
import java.util.function.Consumer
import java.util.stream.Stream

object TcxApp {
    val XML_MAPPER: XmlMapper = XmlMapper.Builder(XmlMapper())
        .enable(SerializationFeature.INDENT_OUTPUT)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .build()

    init {
        XML_MAPPER.factory.enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION)
    }
}

fun writeTcx(gpx: GPX, trackName: String) {
    val trainingCenterDatabase: TrainingCenterDatabase = createTcxFromGpx(gpx)
    writeTcxToFile(trackName, trainingCenterDatabase)
}

private fun writeTcxToFile(trackName: String, trainingCenterDatabase: TrainingCenterDatabase) {
    val tcxDir: String = Config.getProp("outputDir") + "/tcx/"
    if (!File(tcxDir).exists()) {
        File(tcxDir).mkdir()
    }
    val filenameTcx = StringUtils.replace(trackName, " ", "_") + ".tcx"
    TcxApp.XML_MAPPER.writeValue(File(tcxDir + filenameTcx), trainingCenterDatabase)
    println("wrote file $tcxDir$filenameTcx")
}

fun createTcxFromGpx(gpx: GPX): TrainingCenterDatabase {
    val gpxPoints = gpx.tracks[0].segments[0].points
    val length = calculateLength(gpx)
    val trainingCenterDatabase = TrainingCenterDatabase()
    val course = Course(gpx.metadata.get().name.get())
    val lap = Lap(
        totalTimeSeconds =
        SECONDS.between(gpxPoints[0].time.get(), gpxPoints[gpxPoints.size - 1].time.get()).toDouble(),
        distanceMeters = length.toDouble(),
        beginPosition = Position(gpxPoints[0].latitude.toDegrees(), gpxPoints[0].longitude.toDegrees()),
        endPosition = Position(
            gpxPoints[gpxPoints.size - 1].latitude.toDegrees(),
            gpxPoints[gpxPoints.size - 1].longitude.toDegrees()
        ),
        intensity = "Active"
    )
    course.setLap(lap)
    val track = Track()
    var distance = 0.0
    var previous: WayPoint? = null
    for (point in gpxPoints) {
        if (previous != null) {
            distance += Geoid.WGS84.distance(previous, point).toDouble()
            track.addTrackpoint(
                Trackpoint(
                    point.time.get(),
                    Position(point.latitude.toDegrees(), point.longitude.toDegrees()),
                    point.elevation.get().toDouble(),
                    distance
                )
            )
        }
        previous = point
    }
    course.setTrack(track)
    gpx.wayPoints.forEach(
        Consumer { wayPoint: WayPoint ->
            course.addCoursePoint(
                CoursePoint(
                    wayPoint.name.get(),
                    wayPoint.time.get(),
                    Position(wayPoint.latitude.toDouble(), wayPoint.longitude.toDouble()),
                    wayPoint.type.get()
                )
            )
        }
    )
    trainingCenterDatabase.addCourse(course)
    return trainingCenterDatabase
}

private fun calculateLength(gpx: GPX): Length {
    return gpx.tracks()
        .flatMap(io.jenetics.jpx.Track::segments)
        .findFirst()
        .map { it.points() }.orElse(Stream.empty())
        .collect(Geoid.WGS84.toPathLength())
}
