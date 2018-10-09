package kifio

import java.io.*
import java.lang.*
import java.util.*
import com.google.api.client.googleapis.auth.oauth2.*

fun main(args: Array<String>) {
	loadFromTextFile("stations.csv")
	println(generateToken())
}

private fun generateToken(): String {
	val serviceAccount = FileInputStream("pkey.json")
	val googleCred = GoogleCredential.fromStream(serviceAccount)
	val scoped = googleCred.createScoped(listOf(
		"https://www.googleapis.com/auth/firebase.database",
	    "https://www.googleapis.com/auth/userinfo.email"))
	scoped.refreshToken()
	return scoped.getAccessToken()
}

private fun loadFromTextFile(path: String) {
	val stations = mutableListOf<String>()
    File(path).useLines { lines -> stations.addAll(lines) }
    stations.forEachIndexed { i, line -> println("$i: ${buildStation(line)}") }
}

private fun buildStation(stationData: String): Station {
	val attrs = stationData.split(",")
	if (attrs.size < 2) throw IllegalArgumentException("Station data must contains tags, lon and lat")
	var lat: Double = attrs[attrs.size - 1].toDouble()
	var lon: Double = attrs[attrs.size - 2].toDouble()
	var name: String = findName(attrs)
	return Station(UUID.randomUUID().toString(), name, lat, lon)
}

private fun findName(tags: List<String>): String {
	val tags: List<String> =  tags.map { tag -> tag.split(" => ") }
		.filter { pair -> pair[0].trim() == "name" }
		.map { pair -> pair[1] }
	if (tags.size != 1) throw IllegalArgumentException("Multiple names is not allowed!")
	return tags[0].replace("\"", "")
} 

private data class Station(val id: String, val name: String, val lat: Double, val lon: Double)