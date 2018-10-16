package kifio

import java.io.*
import java.net.*
import java.util.*
import com.google.gson.*
import kifio.model.*
import org.w3c.dom.*
import javax.xml.parsers.*

val token = Common.generateToken(FileInputStream("pkey.json"))
val tmpMap = mutableMapOf<String, String>()
val gson = Gson()

fun main(args: Array<String>) {
	loadFromOsm("stations", ::buildStation)
	loadFromOsm("entrances",::buildEntrance)
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
			sendJsonToFirebase(parse(tmpMap), url)
			tmpMap.clear()
		}
	}
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