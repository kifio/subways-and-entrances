package kifio

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import kifio.model.Entrance
import kifio.model.Station
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.charset.Charset


class OpenDataMosParser {

    fun parse(pathToJson: String): Map<Station, List<Entrance>> {
        val isReader = InputStreamReader(FileInputStream(pathToJson), Charset.forName("windows-1251"))
        val entrances = Gson().fromJson<Array<OpenDataMosEntrance>>(JsonReader(isReader),
                object : TypeToken<Array<OpenDataMosEntrance>>() {

                }.type)

        val map = mutableMapOf<Station, MutableList<Entrance>>()
        entrances.forEach {
            val station = Station(it.nameOfStation, it.line, 0.0, 0.0)
            val ref = getRef(it.name)
            val lat = it.geoData.coordinates[0]
            val lon = it.geoData.coordinates[1]
            map.getOrPut(station) { mutableListOf() }.add(Entrance(ref, it.nameOfStation, lat, lon))
        }

        return map
    }

    // ref - номер вестибюля в терминах osm.
    private fun getRef(str: String): Int? {
        val chars = str.toCharArray()
                .map { it.toInt() }
                .filter { it in 48..57 }
                .map { it.toChar() }
                .toCharArray()
        if (chars.isEmpty()) return null
        return Integer.parseInt(String(chars))

    }

    class OpenDataMosEntrance(
            @SerializedName("NameOfStation") val nameOfStation: String,
            @SerializedName("Name") val name: String,
            @SerializedName("Line") val line: String,
            @SerializedName("geoData") val geoData: GeoData)

    class GeoData(val coordinates: DoubleArray)
}