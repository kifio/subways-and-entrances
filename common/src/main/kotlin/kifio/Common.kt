package kifio

import kifio.OpenStreetMapParser.Companion.buildEntrance
import kifio.OpenStreetMapParser.Companion.buildStation
import kifio.model.Entrance
import kifio.model.Station
import java.io.*
import java.util.*

object Common {

    fun buildMap(efis: FileInputStream, sfis: FileInputStream): Map<String, MutableList<Entrance>> {
        val osmParser = OpenStreetMapParser()
        val entrances = osmParser.loadFromOsm(efis, ::buildEntrance)
        val stations = osmParser.loadFromOsm(sfis, ::buildStation)
        val map = mutableMapOf<String, MutableList<Entrance>>()
        entrances.forEach {
            val station = osmParser.nearestStation(it.lat, it.lon, stations)
            val entrances = map.getOrPut(station, {mutableListOf<Entrance>()})
            entrances.add(it)
        }
        return map
    }
}
