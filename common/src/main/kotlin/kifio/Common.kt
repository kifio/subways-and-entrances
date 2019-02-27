package kifio

import kifio.OpenStreetMapParser.Companion.buildEntrance
import kifio.OpenStreetMapParser.Companion.buildStation
import kifio.model.Entrance
import kifio.model.Station
import java.io.*
import java.util.*

object Common {

    fun buildMap(efis: InputStream, sfis: InputStream): Map<Station, List<Entrance>> {
        val osmParser = OpenStreetMapParser()
        val entrances = osmParser.loadFromOsm(efis, ::buildEntrance)
        val stations = osmParser.loadFromOsm(sfis, ::buildStation)
        val map = mutableMapOf<Station, MutableList<Entrance>>()
        entrances.forEach {
            val station = osmParser.nearestStation(it.lat, it.lon, stations)
            map.getOrPut(station) { mutableListOf() }.add(it)
        }
        return map
    }

    fun buildMap(pathToJson: String): Map<Station, List<Entrance>> {
        val odmParser = OpenDataMosParser()
        return odmParser.parse(pathToJson)
    }
}
