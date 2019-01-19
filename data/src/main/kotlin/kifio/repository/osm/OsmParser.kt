package kifio.repository.osm

import kifio.data.Entrance
import kifio.data.MapData
import kifio.data.Station
import kifio.repository.SubwayParser
import org.w3c.dom.Element
import org.w3c.dom.NamedNodeMap
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory


/**
 * Simple parser for .osm files.
 * .osm files use xml format for data saving.
 * Each object in this files wrapped in <node/>.
 * Each property of object, wrapped in <tag/>.
 * <tag/> - is key/value pair, with attributes k and v.
 *
 * Parser extract from node lat, lon and data required for mapping entrances to stations. For now it is color property.
 */
internal class OsmParser: SubwayParser {

    private val tmpMap = mutableMapOf<String, String>()
    private val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()

    override fun <T: MapData> loadFromOsm(inputStream: InputStream, parse: (data: Map<String, String>) -> T): MutableSet<T> {
        return parse(inputStream, parse)
    }

    private fun <T: MapData> parse(inputStream: InputStream, parse: (data: Map<String, String>) -> T): MutableSet<T> {
        val doc = builder.parse(inputStream)
        doc.documentElement.normalize()
        return handleNodes(doc.getElementsByTagName("node"), parse)
    }

    private fun <T: MapData> handleNodes(nodes: NodeList, parse: (data: Map<String, String>) -> T): MutableSet<T> {
        val results = mutableSetOf<T>()
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

    /**
     * Transform node to Map<String, String> of object properties.
     * This map must contains lat, lon, colour, name and other properties.
     */
    private fun handleNode(element: Element) {
        handleAttrs(element.attributes)
        handleTags(element.getElementsByTagName("tag"))
    }

    /**
     * Extract lat and lone from <node/> attributes
     */
    private fun handleAttrs(attrs: NamedNodeMap) {
        tmpMap["lat"] = attrs.getNamedItem("lat").nodeValue
        tmpMap["lon"] = attrs.getNamedItem("lon").nodeValue
    }

    /**
     * Extract data from node tags
     */
    private fun handleTags(tags: NodeList) {
        for (i in 0 until tags.length) {
            handleTag(tags.item(i))
        }
    }

    /**
     * Transform <tag/> to key/value pair and save it
     */
    private fun handleTag(tag: Node) {
        val key = tag.attributes.getNamedItem("k").nodeValue
        val value = tag.attributes.getNamedItem("v").nodeValue
        if (key == "ref" || key == "name" || key == "colour") tmpMap[key] = value
    }

    companion object {

        /**
         * Create Station without nullable fields
         */
        fun buildStation(data: Map<String, String>): Station {
            val lat = data["lat"]?.toDouble()
                    ?: throw IllegalArgumentException("Station without lat")
            val lon = data["lon"]?.toDouble()
                    ?: throw IllegalArgumentException("Station without lon")
            val name = data["name"] ?: throw IllegalArgumentException("Station without name")
            val color = data["colour"]
            return Station(name, parseColor(color, name), lat, lon)
        }

        /**
         * Create Entrance without nullable fields
         */
        fun buildEntrance(data: Map<String, String>): Entrance {
            val lat = data["lat"]?.toDouble()
                    ?: throw IllegalArgumentException("Entrance without lat")
            val lon = data["lon"]?.toDouble()
                    ?: throw IllegalArgumentException("Entrance without lon")
            val ref = data["ref"]?.toInt() ?: -1
            return Entrance(ref, data["colour"] ?: "#000000", lat, lon)
        }

        /**
         * Moscow Metro color converter.
         * Moscow subway growth unstoppable.
         *
         * Ih has 11 primary lines and few short lines which goes through city, and few small lines,
         * which will be transformed to biggest lines.
         *
         * Our parser does not handle Moscow Central Circle and Moscow Monorail.
         */
        private fun parseColor(color: String?, stationName: String): String {
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
                "darkgreen" -> "#79c5bf"    //	Большая кольцевая 11, 11А
                else -> "#000000"
            }
        }

        /**
         * Unfortunately, stations of 12 line in osm files, have similar color with 4's line.
         */
        private fun isButovskaya(stationName: String): Boolean {
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
