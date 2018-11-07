package kifio

import kifio.model.Entrance
import kifio.model.Station
import kifio.Common.buildMap
import java.io.*
import java.util.*

fun main(args: Array<String>) {
    val efis = FileInputStream(args[0])  // "../android/src/main/assets/entrances.osm"
    val sfis = FileInputStream( args[1])  // "../android/src/main/assets/stations.osm"
    writeMapToFile(buildMap(efis, sfis), args[2])
}

private fun writeMapToFile(map: Map<Station, MutableList<Entrance>>, outputFile: String) {
    val sb = StringBuilder()
    File(outputFile).bufferedWriter().use { out ->
        for (station in map.keys) {
            map[station]?.sortedWith(compareBy {it.ref})?.forEach {
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
