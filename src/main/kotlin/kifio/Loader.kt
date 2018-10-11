package kifio

import java.io.*
import java.net.*
import java.lang.*
import java.util.*
import com.google.gson.*
import com.google.api.client.googleapis.auth.oauth2.*

val baseUrl = "https://subways-and-entrances.firebaseio.com/"
val token = generateToken()

private fun generateToken(): String {
	val serviceAccount = FileInputStream("pkey.json")
	val googleCred = GoogleCredential.fromStream(serviceAccount)
	val scoped = googleCred.createScoped(listOf(
		"https://www.googleapis.com/auth/firebase.database",
	    "https://www.googleapis.com/auth/userinfo.email"))
	scoped.refreshToken()
	return scoped.getAccessToken()
}

fun main(args: Array<String>) {
	loadFromTextFile("stations", ::buildStation)
	loadFromTextFile("entrances", ::buildEntrance)
}

private fun <T> loadFromTextFile(type: String, initializer: (attrs: List<String>) -> T) {
	val path = "$type.csv"
	val url = URL("$baseUrl/$type.json?access_token=$token")
	val objects = mutableListOf<String>()
	File(path).useLines { objects.addAll(it) }

	try {
		objects.forEach { sendJsonToFirebase(Gson().toJson(initializer(it.split(","))), url) }
    } catch (e: IOException) {
    	e.printStackTrace()
    }
}

private fun buildStation(attrs: List<String>): Station {
	val geoPoint = parseGeoPoint(attrs)
	var name: String? = null
	var color: String? = null

	attrs.map { it.split(" => ") }.forEach {
		if (it.size == 2) {
			val key = it[0].replace("\"", "").trim()
			val value = it[1].replace("\"", "")
			if (key == "name") {
				name = value
			} else if (key == "colour") {
				color = value
			}
		}
	}

	return Station(UUID.randomUUID().toString(), name, color, geoPoint.first, geoPoint.second)
}

private fun buildEntrance(attrs: List<String>): Entrance {
	val geoPoint = parseGeoPoint(attrs)
	var ref: String? = null
	var color: String? = null

	attrs.map { it.split(" => ") }.forEach {
		if (it.size == 2) {
			val key = it[0].replace("\"", "").trim()
			val value = it[1].replace("\"", "")
			if (key == "ref") {
				ref = value
			} else if (key == "colour") {
				color = value
			}
		}
	}

	return Entrance(UUID.randomUUID().toString(), ref?.toInt() ?: 0, color, geoPoint.first, geoPoint.second)
}

private fun parseTags(entries: List<String>, tags: List<String>): HashMap<String, String> {
	//TOOD: Create HashMap. Fill it with entries. Search in map by keys,
}

private fun parseGeoPoint(attrs: List<String>): Pair<Double, Double> {
	if (attrs.size < 2) throw IllegalArgumentException("Station data must contains tags, lon and lat")
	return(Pair<Double, Double>(attrs[attrs.size - 1].toDouble(), attrs[attrs.size - 2].toDouble()))
}

private data class Station(val id: String, val name: String?, val color: String?, val lat: Double, val lon: Double)
private data class Entrance(val id: String, val ref: Int, val color: String?, val lat: Double, val lon: Double)

@Throws(IOException::class)
private fun sendJsonToFirebase(jsonString: String, url: URL)  {
	val connection = url.openConnection() as HttpURLConnection
	connection.setRequestMethod("POST")
	connection.setDoOutput(true);

	val os = OutputStreamWriter(connection.getOutputStream());
	os.write(jsonString);
	os.flush();
	os.close();

	val responseCode = connection.getResponseCode();
	println("Sending $jsonString to URL : " + url);
	println("Response Code : " + responseCode);
	
	val response = connection.getInputStream().bufferedReader().use(BufferedReader::readText)
	println(response.toString());
}