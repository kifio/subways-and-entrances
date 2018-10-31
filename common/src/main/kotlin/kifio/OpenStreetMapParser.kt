package kifio

import kifio.model.Entrance
import kifio.model.Station
import org.w3c.dom.Element
import org.w3c.dom.NamedNodeMap
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import java.io.InputStream
import java.lang.IllegalArgumentException
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory

class OpenStreetMapParser {

    private val tmpMap = mutableMapOf<String, String>()

    fun <T> loadFromOsm(inputStream: InputStream, parse: (data: Map<String, String>) -> T): MutableSet<T> {
        val results = mutableSetOf<T>()
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val doc = builder.parse(inputStream)
        doc.documentElement.normalize()
        val nodes = doc.getElementsByTagName("node")
        for (i in 0 until nodes.length) {
            val node = nodes.item(i)
            if (node.nodeType == Node.ELEMENT_NODE) {
                handleNode(node as Element)
                results.add(parse(tmpMap))
                tmpMap.clear()
            }
        }
        return results
    }

    private fun handleNode(element: Element) {
        handleAttrs(element.attributes)
        handleTags(element.getElementsByTagName("tag"))
    }

    private fun handleAttrs(attrs: NamedNodeMap) {
        tmpMap["lat"] = attrs.getNamedItem("lat").nodeValue
        tmpMap["lon"] = attrs.getNamedItem("lon").nodeValue
    }

    private fun handleTags(tags: NodeList) {
        for (i in 0 until tags.length) {
            handleTag(tags.item(i))
        }
    }

    private fun handleTag(tag: Node) {
        val key = tag.attributes.getNamedItem("k").nodeValue
        val value = tag.attributes.getNamedItem("v").nodeValue
        if (key == "ref" || key == "name" || key == "colour") tmpMap[key] = value
    }

    fun nearestStation(lat: Double, lon: Double, stations: MutableSet<Station>): Station {
        var nearest: Station? = null
        var distance = Double.POSITIVE_INFINITY
        for (station in stations) {

            val latSqr = (station.lat - lat) * (station.lat - lat)
            val lonSqr = (station.lon - lon) * (station.lon - lon)
            val d = latSqr + lonSqr

            if (d < distance) {
                distance = d
                nearest = station
            }

        }
        if (nearest == null) throw IllegalArgumentException("Nearest station does not exists")
        return nearest
    }

    companion object {

        fun buildStation(data: Map<String, String>): Station {
            val lat = data["lat"]?.toDouble()
                    ?: throw IllegalArgumentException("Station without lat")
            val lon = data["lon"]?.toDouble()
                    ?: throw IllegalArgumentException("Station without lon")
            val name = data["name"] ?: throw IllegalArgumentException("Station without name")
            val color = data["colour"]
            return Station(name, parseColor(color, name), lat, lon)
        }

        fun buildEntrance(data: Map<String, String>): Entrance {
            val lat = data["lat"]?.toDouble()
                    ?: throw IllegalArgumentException("Entrance without lat")
            val lon = data["lon"]?.toDouble()
                    ?: throw IllegalArgumentException("Entrance without lon")
            val ref = data["ref"]?.toInt() ?: -1
            return Entrance(ref, data["colour"], lat, lon)
        }

        fun parseColor(color: String?, stationName: String): String? {
            return when (color) {
                "red" -> "#da322a"    //	Сокольническая линия 1
                "green" -> "#46ae5c"    //	"Замоскворецкая линия" 2
                "blue" -> "#006da8"    //	"Арбатско-Покровская линия" 3
                "lightblue" -> if (isButovskaya(stationName)) "#a7b9d4" else "#00b8e0"    //	Бутовская линия 12, "Филевская линия" 4
                "brown" -> "#794835"    // Кольцевая линия 5
                "orange" -> "#e77a38"    //	Калужская линия" 6
                "violet" -> "#804388"    //	"Таганско-Краснопресненская линия" 7
                "yellow" -> "#f8c73e"     // Калининская линия, Солнцевская линия	8, 8A
                "#a0a2a3" -> "#9c9c99"    //	Серпуховско-Тимирязевская линия	9
                "#b4d445" -> "#adce4b"    //	Люблинско-Дмитровская линия	10
                "darkgreen" -> "#79c5bf"    //	"Каховская линия", Большая кольцевая 11, 11А
                else -> null
            }
        }

        fun isButovskaya(stationName: String): Boolean {
            return stationName == "Битцевский парк"
                    || stationName == "Лесопарковая"
                    || stationName == "Улица Старокачаловская"
                    || stationName == "Улица Скобелевская"
                    || stationName == "Бульвар Адмирала Ушакова"
                    || stationName == "Улица Горчакова"
                    || stationName == "Бунинская аллея"
        }
    }
}
