package kifio

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kifio.model.Entrance
import java.io.File
import java.nio.charset.Charset
import java.util.*

class OpenDataMosParser {

    fun loadEntrancesDetails(path: String, encoding: String) {
        val content = File(path).readText(Charset.forName(encoding))
        val jsonArr = JsonParser().parse(content).asJsonArray
        for (i in 0 until jsonArr.size()) {
            handleEntranceDetails(jsonArr.get(i).asJsonObject)
        }
    }

    private fun handleEntranceDetails(json: JsonObject) {
        var name = json.get("Name").asString.toLowerCase().replace("ё", "е")
        val nameOfStation = json.get("NameOfStation").asString.toLowerCase().replace("ё", "е")
        val line = json.get("Line").asString

        if (!name.contains("$nameOfStation,")) {
            name = name.replace(nameOfStation, "$nameOfStation,")
        }

        val coordinates = getCoordinates(json.get("geoData").asJsonObject)
        val entrance = nearest(coordinates)

        if (entrance != null) {
            entrance.station = "$name:$line"
            println("$name:$line${Arrays.toString(coordinates)} is $entrance")
        } else {
            println("$name is unknown")
        }
    }

    private fun getCoordinates(geoData: JsonObject): DoubleArray {
        val coordinates = geoData.get("coordinates").asJsonArray
        return doubleArrayOf(coordinates.get(1).asDouble, coordinates.get(0).asDouble)
    }

    fun nearest(latLon: DoubleArray): Entrance? {
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
}