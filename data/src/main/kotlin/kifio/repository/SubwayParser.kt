package kifio.repository

import kifio.data.MapData
import java.io.InputStream

interface SubwayParser {
    fun <T: MapData> loadFromOsm(inputStream: InputStream, parse: (data: Map<String, String>) -> T): MutableSet<T>
}