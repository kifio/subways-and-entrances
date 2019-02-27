package kifio

import kifio.model.Entrance
import kifio.model.Station
import kifio.Common.buildMap
import java.io.*
import java.lang.RuntimeException
import java.util.*

fun main(args: Array<String>) {

    if (args[0] == "odm") {
        writeMapToFile(buildMap(args[1]), args[2])
    } else if (args[0] == "osm") {
        val efis = FileInputStream(args[1])  // "../android/src/main/assets/entrances.osm"
        val sfis = FileInputStream(args[2])  // "../android/src/main/assets/stations.osm"
        writeMapToFile(buildMap(efis, sfis), args[3])
    } else {
        throw RuntimeException("Unknown parser")
    }
}

private fun writeMapToFile(map: Map<Station, List<Entrance>>, outputFile: String) {
    val sb = StringBuilder()
    File(outputFile).bufferedWriter().use { out ->
        for (station in map.keys) {
            map[station]?.sortedWith(compareBy { it.ref })?.forEach {
                val line = sb.append(station)
                        .append(",")
                        .append(it)
                        .append('\n')
                        .toString()
                println(line)
                out.write(line)
                sb.delete(0, line.length)
            }
        }
    }
}
