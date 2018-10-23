package kifio

import kifio.OpenStreetMapParser.Companion.buildEntrance
import kifio.OpenStreetMapParser.Companion.buildStation
import kifio.model.Entrance
import kifio.model.Station
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.sql.SQLException
import java.util.*

val token = Common.generateToken(FileInputStream("../android/src/main/assets/pkey.json"))
val entrances = mutableSetOf<Entrance>()
val stations = mutableSetOf<Station>()
val osmParser = OpenStreetMapParser()
val odmParser = OpenDataMosParser()

val DB_NAME = "db_name"     // used just as key in map
val DB_URL = System.getenv("DB_URL")?: throw Error("DB_URL not defined")
val DB_USER = System.getenv("DB_USER")?: throw Error("DB_USER not defined")
val DB_PASSWORD = System.getenv("DB_PASSWORD")?: throw Error("DB_PASSWORD path not defined")

val SELECT_STOP_QUERY = "select distinct stop.id as stopId, stop.name as stopName, route.id, route.name, stop.lat as lat, stop.lon as lon " +
        "from stop " +
        "  inner join path_stop on stop.id = path_stop.stop_id " +
        "  inner join route_path on path_stop.route_path_id = route_path.id " +
        "  inner join route on route_path.route_id = route.id " +
        "where stop.type = 'subway' " +
        "      and transport_type = 'subway' " +
        "      and stop.active = true " +
        "      and route.active = true " +
        "      and route_path.active = true " +
        "      and lower(stop.name) like '%s%%' and route.color = '%s'"

val UPDATE_STOP_LAT_LON = "UPDATE stop " +
        "SET lat = %f, lon = %f, updated_at = now() " +
        "WHERE id = '%s';"

val DELETE_ENTRANCES_QUERY = "DELETE FROM subway_hall WHERE stop_id = '%s';"

val DELETE_SUBWAY_HALLS_LINK = "DELETE FROM subway_hall_link WHERE stop_id = '%s';"

val INSERT_ENTRANCES_QUERY = "INSERT INTO subway_hall (id, name, stop_id, active, lat, lon, geopoint, type, number, entrance_type, created_at, updated_at)\n" +
        "VALUES ('%s', '%s', '%s', 1, %f, %f, st_point(%f, %f), 'subway', %d, 'all', now(), now());"

val INSERT_SUBWAY_HALLS_LINK = "INSERT INTO subway_hall_link (hall_id, stop_id)\n" +
        "VALUES ('%s', '%s');"

fun main(args: Array<String>) {
    stations.addAll(osmParser.loadFromOsm(FileInputStream("../android/src/main/assets/stations.osm"), ::buildStation))
    entrances.addAll(osmParser.loadFromOsm(FileInputStream("../android/src/main/assets/entrances.osm"), ::buildEntrance))
    entrances.forEach { it.station = osmParser.nearestStation(it.lat, it.lon, stations) }
    updateStationsInDd()
}

fun updateStationsInDd() {
    SQLManager.registerDataSource(DB_NAME, DB_URL, DB_USER, DB_PASSWORD)
    val stops = mutableListOf<String>()
    for (station in stations) {
        val query = String.format(Locale.ENGLISH, SELECT_STOP_QUERY, station.name?.toLowerCase(), station.color)
        val entrances = entrances.filter { it.station == "${station.name}:${station.color}" }
        println(query)
        SQLManager.executeQuery(DB_NAME, query) {
            val stopId = it.getString("stopId")
            val stopName = it.getString("stopName")
            println("$stopId $stopName")
            val updateQuery = String.format(Locale.ENGLISH, UPDATE_STOP_LAT_LON, station.lat, station.lon, stopId)
            SQLManager.executeUpdate(DB_NAME, updateQuery)
            updateEntrances(stopId, stopName, entrances)
            stops.add(stopId)
        }
    }

    println("${stops.size} stations were updated")
}

fun updateEntrances(stopId: String, stopName: String, entrances: List<Entrance>) {
    SQLManager.executeUpdate(DB_NAME, String.format(Locale.ENGLISH, DELETE_SUBWAY_HALLS_LINK, stopId))
    var query = String.format(Locale.ENGLISH, DELETE_ENTRANCES_QUERY, stopId)
    SQLManager.executeUpdate(DB_NAME, query)
    for (entrance in entrances) {
        SQLManager.executeQuery(DB_NAME, "select count(*) as count from subway_hall where id ='${entrance.id}'") {
            if (it.getInt("count") == 0) {
                query = String.format(Locale.ENGLISH, INSERT_ENTRANCES_QUERY,
                        entrance.id, stopName, stopId, entrance.lat, entrance.lon, entrance.lon, entrance.lat, entrance.ref)
                println(query)
                try {
                    SQLManager.executeUpdate(DB_NAME, query)
                    SQLManager.executeUpdate(DB_NAME, String.format(Locale.ENGLISH, INSERT_SUBWAY_HALLS_LINK, entrance.id, stopId))
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }
        }
    }
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