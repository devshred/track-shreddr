package org.devshred.tracks

import org.devshred.tracks.utils.Config
import org.devshred.tracks.utils.findCustomPointOfInterests
import org.devshred.tracks.utils.getCoordinatesFromGoogleMapsLink
import org.devshred.tracks.utils.readEtappenplan
import org.devshred.tracks.waypoints.Buffet
import org.devshred.tracks.waypoints.CustomPointOfInterest
import org.devshred.tracks.waypoints.Hotel
import org.devshred.tracks.waypoints.PoiType
import org.devshred.tracks.writer.writeTrack

fun main(args: Array<String>) {
    val spreadsheetId: String = Config.getProp("spreadsheetId")
    val rows: List<List<Any>> = readEtappenplan(spreadsheetId)

    if (rows.isEmpty()) {
        System.err.println("No data found.")
    } else {
        for (row in rows) {
            if (isEmptyRow(row) || hasNoTracks(row)) continue
            val day = row[0] as String
            val buffet = Buffet.fromRow(row)
            val hotel = Hotel.fromRow(row)
            val shortTrackHasBuffet = (row[7] as String).isNotEmpty()
            val longTrackHasBuffet = (row[8] as String).isNotEmpty()
            val shortTrackKomootPage = row[14] as String
            val longTrackKomootPage = row[15] as String

            val additionalWayPointsShortTrack = additionalWayPoints(spreadsheetId, "WP" + day + "K")
            val wayPointsShortTrack =
                if (shortTrackHasBuffet) additionalWayPointsShortTrack.plus(buffet).plus(hotel)
                else additionalWayPointsShortTrack.plus(hotel)

            val additionalWayPointsLongTrack = additionalWayPoints(spreadsheetId, "WP" + day + "L")
            val wayPointsLongTrack =
                if (longTrackHasBuffet) additionalWayPointsLongTrack.plus(buffet).plus(hotel)
                else additionalWayPointsShortTrack.plus(hotel)

            writeTrack(
                Config.getProp("tourPrefix") + day,
                shortTrackKomootPage,
                wayPointsShortTrack,
            )
            writeTrack(
                Config.getProp("tourPrefix") + day,
                longTrackKomootPage,
                wayPointsLongTrack
            )
        }
    }
}

fun additionalWayPoints(spreadsheetId: String, sheetName: String): Set<CustomPointOfInterest> {
    val rows: List<List<Any>> = findCustomPointOfInterests(spreadsheetId, sheetName)
    val pointsOfInterest: MutableSet<CustomPointOfInterest> = mutableSetOf()
    for (row in rows) {
        if (hasNoMapsLink(row)) continue
        val poi = CustomPointOfInterest(
            coordinates = getCoordinatesFromGoogleMapsLink(row[2].toString()),
            name = row[1] as String,
            type = PoiType.valueOf((row[0] as String).uppercase())
        )
        pointsOfInterest.add(poi)
    }
    return pointsOfInterest
}

private fun hasNoMapsLink(row: List<Any>): Boolean {
    val value = row[2] as String
    return value.isEmpty() || !value.startsWith("https://goo.gl/maps/")
}

private fun hasNoTracks(row: List<*>) = row.size < 15 || (row[14] as String).isEmpty() && (row[15] as String).isEmpty()

private fun isEmptyRow(row: List<*>?) = row == null || row.size < 3 || (row[2] as String).isEmpty()
