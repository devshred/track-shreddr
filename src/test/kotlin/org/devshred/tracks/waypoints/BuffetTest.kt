package org.devshred.tracks.waypoints

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BuffetTest {
    @Test
    fun equality() {
        val emptyBuffetObject = EmptyBuffet
        assertThat(emptyBuffetObject.isEmptyBuffet()).isTrue

        val buffetWithSameCoordinatesAsEmptyBuffet = Buffet(coordinatesFromDouble(0.0, 0.0))
        assertThat(buffetWithSameCoordinatesAsEmptyBuffet.isEmptyBuffet()).isFalse
    }

    @Test
    fun `empty row should return EmptyBuffet`() {
        val emptyBuffetFromRow = Buffet.fromRow(listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", ""))
        assertThat(emptyBuffetFromRow.isEmptyBuffet()).isTrue
    }
}
