package kifio

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kifio.model.Entrance
import java.io.File
import java.io.InputStream
import java.lang.IllegalArgumentException
import java.nio.charset.Charset
import java.util.*

class OpenDataMosParser {

    fun parseOpenDataMosEntrances(inputStream: InputStream, encoding: String): Set<Entrance> {
        val entrances = mutableSetOf<Entrance>()
        inputStream.bufferedReader(Charset.forName(encoding)).use {
            val content = it.readText()
            val jsonArr = JsonParser().parse(content).asJsonArray
            for (i in 0 until jsonArr.size()) {
                val entrance = buildEntrance(jsonArr.get(i).asJsonObject)
                if (entrance != null) {
                    entrances.add(entrance)
                }
            }
            return entrances
        }
    }

    fun mapOsmData(inputStream: InputStream, encoding: String, entrances: MutableSet<Entrance>) {
        inputStream.bufferedReader(Charset.forName(encoding)).use {
            val content = it.readText()
            val jsonArr = JsonParser().parse(content).asJsonArray
            for (i in 0 until jsonArr.size()) {
                mapWithOsmEntrance(jsonArr.get(i).asJsonObject, entrances)
            }
        }
    }

    private fun mapWithOsmEntrance(json: JsonObject, entrances: MutableSet<Entrance>) {
        val entrance = ODMEntrance(json)

        val nearestOsmEntrance = nearest(entrance.coordinates, entrances)

        /* if (nearestOsmEntrance != null) {
            nearestOsmEntrance.station = "${entrance.nameOfStation}:${entrance.line}"
            println("${nearestOsmEntrance.station} : ${Arrays.toString(entrance.coordinates)} is $entrance")
        } else {
            println("${entrance.name} is not founded")
        } */
    }

    private fun buildEntrance(json: JsonObject): Entrance? {
        val entrance = ODMEntrance(json)
        val arr = entrance.name.split(",")
        if (arr.size > 1) {
            val number = parseNumber(arr[1].trim())
            return Entrance(UUID.randomUUID().toString(),
                    number,
                    null,
                    entrance.coordinates[0],
                    entrance.coordinates[1])
        } else {
            throw IllegalArgumentException("Invalid ")
        }
    }

    private fun parseNumber(entranceNumberString: String): Int? {
        var components = entranceNumberString.split(" ")
        if (components.size >= 2) {
            components = components.subList(0, 2)
            return components[1].trim().toIntOrNull()
        } else {
            println(entranceNumberString)
            return null
        }
    }


    private fun nearest(latLon: DoubleArray, entrances: MutableSet<Entrance>): Entrance? {
        var nearest: Entrance? = null
        var distance = Double.POSITIVE_INFINITY
        for (entrance in entrances) {

            val latSqr = (entrance.lat - latLon[0]) * (entrance.lat - latLon[0])
            val lonSqr = (entrance.lon - latLon[1]) * (entrance.lon - latLon[1])
            val d = latSqr + lonSqr

            if (d < distance) {
                distance = d
                nearest = entrance
            }

        }
        return nearest
    }

    private data class ODMEntrance(val name: String,
                                   val line: String,
                                   val nameOfStation: String,
                                   val coordinates: DoubleArray) {

        constructor(json: JsonObject) : this(json.get("Name").asString,
                        json.get("NameOfStation").asString,
                        json.get("Line").asString,
                        getCoordinates(json.get("geoData").asJsonObject))

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ODMEntrance

            if (name != other.name) return false
            if (line != other.line) return false
            if (nameOfStation != other.nameOfStation) return false
            if (!Arrays.equals(coordinates, other.coordinates)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + line.hashCode()
            result = 31 * result + nameOfStation.hashCode()
            result = 31 * result + Arrays.hashCode(coordinates)
            return result
        }
    }

    companion object {
        private fun getCoordinates(geoData: JsonObject): DoubleArray {
            val coordinates = geoData.get("coordinates").asJsonArray
            return doubleArrayOf(coordinates.get(1).asDouble, coordinates.get(0).asDouble)
        }
    }
}
