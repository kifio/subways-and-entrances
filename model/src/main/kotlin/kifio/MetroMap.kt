package kifio

import com.mapbox.geojson.Point
import com.mapbox.turf.TurfClassification
import kifio.OpenStreetMapParser.Companion.buildEntrance
import kifio.OpenStreetMapParser.Companion.buildStation
import kifio.subway.data.Entrance
import kifio.subway.data.Station
import java.io.InputStream

object MetroMap {

    /**
     * Build map where stations is keys and
     */
    fun buildMap(efis: InputStream, sfis: InputStream): Map<Station, List<Entrance>> {
        val osmParser = OpenStreetMapParser()
        val entrances = osmParser.loadFromOsm(efis, ::buildEntrance)
        val stations = osmParser.loadFromOsm(sfis, ::buildStation)
        return mapEntrancesToStations(entrances, createStationsPointsMap(stations))
    }

    private fun mapEntrancesToStations(
            entrances: MutableSet<Entrance>,
            stationsPoints: Map<Point, Station>): Map<Station, List<Entrance>> {

        val points = stationsPoints.keys.toList()
        val results = mutableMapOf<Station, MutableList<Entrance>>()

        entrances.forEach {
            val stationPoint = nearestStation(Point.fromLngLat(it.lon, it.lat), points)
            val station = stationsPoints[stationPoint] ?: throw IllegalArgumentException("Station with this coordinates must exists!")
            results.getOrPut(station) {mutableListOf()}.add(it)
        }

        return results
    }

    private fun createStationsPointsMap(stations: MutableSet<Station>): Map<Point, Station> {
        val stationsPoints = mutableMapOf<Point, Station>()
        stations.map { stationsPoints[Point.fromLngLat(it.lon, it.lat)] = it }
        return stationsPoints
    }

    private fun nearestStation(entrancePoint: Point, stations: List<Point>): Point {
        return TurfClassification.nearestPoint(entrancePoint, stations)
    }
}
