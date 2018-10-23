package kifio

import kifio.OpenStreetMapParser.Companion.buildEntrance
import kifio.OpenStreetMapParser.Companion.buildStation
import kifio.model.Entrance
import kifio.model.Station
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

val token = Common.generateToken(FileInputStream("pkey.json"))
val entrances = mutableSetOf<Entrance>()
val stations = mutableSetOf<Station>()
val osmParser = OpenStreetMapParser()
val odmParser = OpenDataMosParser()
val DB_URL = System.getenv("DB_URL")?: throw Error("DB_URL not defined")
val DB_USER = System.getenv("DB_USER")?: throw Error("DB_USER not defined")
val DB_PASSWORD = System.getenv("DB_PASSWORD")?: throw Error("DB_PASSWORD path not defined")

fun main(args: Array<String>) {

    stations.addAll(osmParser.loadFromOsm(FileInputStream("../android/src/main/assets$stations.osm"), ::buildStation))
    entrances.addAll(osmParser.loadFromOsm(FileInputStream("../android/src/main/assets$entrances.osm"), ::buildEntrance))
    odmParser.mapOsmData(FileInputStream("../android/src/main/assets/data-397-2018-10-02.json"), "Windows-1251", entrances)

//    sendJsonToFirebase(parse(tmpMap), URL("${Common.baseUrl}/stations.json?access_token=$token"))
//    sendJsonToFirebase(parse(tmpMap), URL("${Common.baseUrl}/entrances.json?access_token=$token"))
}

@Throws(IOException::class)
private fun sendJsonToFirebase(jsonString: String?, url: URL) {
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