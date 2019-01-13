package kifio

import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.turf.TurfClassification
import kifio.OpenStreetMapParser.Companion.buildEntrance
import kifio.OpenStreetMapParser.Companion.buildStation
import kifio.data.Entrance
import kifio.data.Station
import java.io.InputStream
import java.lang.IllegalArgumentException

object MetroMap {

    /**
     * Build map where stations is keys and
     */
    fun buildMap(efis: InputStream, sfis: InputStream): Map<Station, FeatureCollection> {
        val osmParser = OpenStreetMapParser()
        val entrances = osmParser.loadFromOsm(efis, ::buildEntrance)
        val stations = osmParser.loadFromOsm(sfis, ::buildStation)
        return mapEntrancesToStations(entrances, createStationsPointsMap(stations))
    }

    private fun mapEntrancesToStations(
            entrances: MutableSet<Entrance>,
            stationsPoints: Map<Point, Station>): Map<Station, FeatureCollection> {

        val points = stationsPoints.keys.toList()
        val results = mutableMapOf<Station, MutableList<Entrance>>()

        entrances.forEach {
            val stationPoint = nearestStation(Point.fromLngLat(it.lon, it.lat), points)
            val station = stationsPoints[stationPoint] ?: throw IllegalArgumentException("Station with this coordinates must exists!")
            results.getOrPut(station) {mutableListOf()}.add(it)
        }

        return transformResults(results)
    }

    private fun transformResults(stations: MutableMap<Station, MutableList<Entrance>>): Map<Station, FeatureCollection> {
        val results = mutableMapOf<Station, FeatureCollection>()
        stations.forEach { entry -> results[entry.key] = transfornEntrances(entry.value) }
        return results
    }

    private fun transfornEntrances(entrances: MutableList<Entrance>): FeatureCollection {
        return FeatureCollection.fromFeatures(entrances.map { it.toFeature() }.toList())
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
