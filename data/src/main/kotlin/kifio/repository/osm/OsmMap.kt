package kifio.repository.osm

import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.turf.TurfClassification
import kifio.repository.osm.OsmParser.Companion.buildEntrance
import kifio.repository.osm.OsmParser.Companion.buildStation
import kifio.data.Entrance
import kifio.data.MapData
import kifio.data.Station
import kifio.geojson.GeoJson
import kifio.repository.SubwayMap
import java.io.InputStream
import java.util.*

object OsmMap: SubwayMap {
    /**
     * Return Map, where keys is stations and lists of entrances is values.
     */
    override fun buildMap(efis: InputStream, sfis: InputStream): Map<Station, List<Entrance>> {
        val osmParser = OsmParser()
        val entrances = osmParser.loadFromOsm(efis, ::buildEntrance)
        val stations = osmParser.loadFromOsm(sfis, ::buildStation)
        return mapEntrancesToStations(entrances, createStationsPointsMap(stations))
    }

    /**
     * Return GeoJson with only stations.
     */
    override fun getStationsJson(geoData: Map<Station, List<Entrance>>): GeoJson {
        return GeoJson().apply {
            setFeatures(buildFeatures(geoData.keys))
        }
    }

    /**
     * Return GeoJson with only entrances.
     */
    override fun getEntrancesJson(geoData: Map<Station, List<Entrance>>): GeoJson {
        return GeoJson().apply {
            setFeatures(buildFeatures(geoData.keys.map {
                geoData[it] ?: Collections.emptyList()
            }.flatten()))
        }
    }

    private fun buildFeatures(mapObjects: Collection<MapData>): List<Feature> {
        return mapObjects.map { it.toFeature() }.toList()
    }

    private fun mapEntrancesToStations(
            entrances: MutableSet<Entrance>,
            stationsPoints: Map<Point, Station>): Map<Station, List<Entrance>> {

        val points = stationsPoints.keys.toList()
        val results = mutableMapOf<Station, MutableList<Entrance>>()

        entrances.forEach {
            val stationPoint = nearestStation(Point.fromLngLat(it.getLon(), it.getLat()), points)
            val station = stationsPoints[stationPoint]
                    ?: throw IllegalArgumentException("Station with this coordinates must exists!")
            results.getOrPut(station) { mutableListOf() }.add(it)
        }

        return results
    }

    private fun createStationsPointsMap(stations: MutableSet<Station>): Map<Point, Station> {
        val stationsPoints = mutableMapOf<Point, Station>()
        stations.map { stationsPoints[Point.fromLngLat(it.getLon(), it.getLat())] = it }
        return stationsPoints
    }

    private fun nearestStation(entrancePoint: Point, stations: List<Point>): Point {
        return TurfClassification.nearestPoint(entrancePoint, stations)
    }
}