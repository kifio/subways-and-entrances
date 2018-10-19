package kifio

import java.io.*
import java.net.*
import java.util.*
import com.google.gson.*
import kifio.model.*
import org.w3c.dom.*
import javax.xml.parsers.*
import java.nio.charset.Charset

val token = Common.generateToken(FileInputStream("pkey.json"))
val tmpMap = mutableMapOf<String, String>()
val gson = Gson()
val colors = mutableSetOf<String>()

fun main(args: Array<String>) {
	// loadFromOsm("stations", ::buildStation)
	// loadFromOsm("entrances",::buildEntrance)
	loadEntrancesDetails("data-397-2018-10-02.json", "Windows-1251")
}

private fun loadFromOsm(type: String, parse: (data: Map<String, String>) -> String?) {
	val factory = DocumentBuilderFactory.newInstance()
	val builder = factory.newDocumentBuilder()
	val url = URL("${Common.baseUrl}/$type.json?access_token=$token")
	val doc = builder.parse(File("$type.osm"))
	doc.documentElement.normalize()
	val nodes = doc.getElementsByTagName("node")
	for (i in 0 until nodes.length) {
		val node = nodes.item(i)
		if (node.nodeType == Node.ELEMENT_NODE) {
			handleNode(node as Element)
			// sendJsonToFirebase(parse(tmpMap), url)
			tmpMap.clear()
		}
	}
	colors.forEach { println(it) }
}

private fun loadEntrancesDetails(path: String, encoding: String) {
	val content = File(path).readText(Charset.forName(encoding))
	val jsonArr = JsonParser().parse(content).asJsonArray
	for (i in 0 until jsonArr.size()) {
		handleEntranceDetails(jsonArr.get(i).asJsonObject)
	}
}

private fun handleEntranceDetails(json: JsonObject) {
	val name = json.get("Name").asString
	val line = json.get("Line").asString
	val arr = name.split(",")
	arr.forEach { 
		val value = it.trim()
		if (value.contains("вход-выход")) {
			println(value.split(" ").last())
		} 

	}
	println("$name\n$line\n")
}

private fun parseColor(color: String, stationName: String): String {
	return when (color) {
		"red" -> "#da322a"	//	Сокольническая линия 1
		"green" ->"#46ae5c"	//	"Замоскворецкая линия" 2
		"blue" -> "#006da8"	//	"Арбатско-Покровская линия" 3
		"lightblue" -> if (isButovskaya(stationName)) "" else ""	//	"Филевская линия" 4, Бутовская 12
		"brown" -> "#794835"	// Кольцевая линия 5
		"orange" -> "#f8c73e"	//	"Калининская линия" 6
		"violet" -> "#804388"	//	"Таганско-Краснопресненская линия" 7
		"yellow" -> "#f8c73e"	// Калининская линия, Солнцевская линия	8, 8A
		"#a0a2a3" -> "9c9c99"	//	Серпуховско-Тимирязевская линия	9
		"b4d445" -> "#adce4b"	//	Люблинско-Дмитровская линия	10
		"darkgreen" -> "#79c5bf"	//	"Каховская линия", Большая кольцевая 11, 11А
		else -> ""
	}
}

private fun isButovskaya(stationName: String): Boolean {
	return stationName == "Битцевский парк"
		|| stationName == "Лесопарковая"
		|| stationName == "Улица Старокачаловская"	
		|| stationName == "Улица Скобелевская"
		|| stationName == "Бульвар Адмирала Ушакова"
		|| stationName == "Улица Горчакова"
		|| stationName == "Бунинская аллея"
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
	if (key == "ref" || key == "name") tmpMap[key] = value
	if (key == "colour") colors.add("$key:$value")
}

private fun buildStation(data: Map<String, String>): String? {
	val lat = data["lat"]?.toDouble() ?: return null
	val lon = data["lon"]?.toDouble() ?: return null
	return gson.toJson(Station(UUID.randomUUID().toString(), data["name"], data["colour"], lat, lon))
}

private fun buildEntrance(data: Map<String, String>): String? {
	val lat = data["lat"]?.toDouble() ?: return null
	val lon = data["lon"]?.toDouble() ?: return null
	val ref = data["ref"]?.toInt() ?: return null
	return gson.toJson(Entrance(UUID.randomUUID().toString(), ref, data["colour"], lat, lon))
}

@Throws(IOException::class)
private fun sendJsonToFirebase(jsonString: String?, url: URL)  {
	if (jsonString == null) return
	val connection = url.openConnection() as HttpURLConnection
	connection.requestMethod = "POST"
	connection.doOutput = true

	val os = OutputStreamWriter(connection.outputStream)
	os.write(jsonString)
	os.flush()
	os.close()

	val responseCode = connection.responseCode
	println("Sending $jsonString to URL : $url")
	println("Response Code : $responseCode")

	val response = connection.inputStream.bufferedReader().use(BufferedReader::readText)
	println(response)
}