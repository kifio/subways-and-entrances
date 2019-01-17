package kifio

import kifio.MetroMap.buildMap
import kifio.subway.data.Entrance
import kifio.subway.data.Station
import java.io.FileInputStream

fun readMetroMap(args: Array<String>): Map<Station, List<Entrance>> {

    val efis: FileInputStream
    val sfis: FileInputStream
    val outputFile: String

    when {
        args.isEmpty() -> {
            efis = FileInputStream("../android/src/main/assets/entrances.osm")
            sfis = FileInputStream("../android/src/main/assets/stations.osm")
            outputFile = "entrances.json"
        }
        args.size == 3 -> {
            efis = FileInputStream(args[0])
            sfis = FileInputStream(args[1])
            outputFile = args[2]
        }
        else -> throw IllegalArgumentException("You must specifies files with stations and entrances, " +
                "or use default version of files")
    }

    return buildMap(efis, sfis)
}

//private fun writeMapToFile(map: Map<Station, FeatureCollection>, outputFile: String) {
//    val sb = StringBuilder()
//    File(outputFile).bufferedWriter().use { out ->
//        for (station in map.keys) {
//            val entrances = map[station]?.features() ?: throw java.lang.IllegalArgumentException("Features list cannot be empty")
//            for (entrance in entrances) {
//
//            }
//        }
//    }
//}
