package kifio

import com.mchange.v2.c3p0.ComboPooledDataSource
import kifio.model.Entrance
import kifio.model.Station
import java.io.File
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*
import kotlin.collections.ArrayList

val DB_NAME = "db_name"     // used just as key in map
val DB_URL = System.getenv("DB_URL") ?: throw Error("DB_URL not defined")
val DB_USER = System.getenv("DB_USER") ?: throw Error("DB_USER not defined")
val DB_PASSWORD = System.getenv("DB_PASSWORD") ?: throw Error("DB_PASSWORD path not defined")

const val SELECT_STOP_QUERY = "select\n" +
        "  stop.name as name,\n" +
        "  stop.id as id,\n" +
        "  route.color as color\n" +
        "from stop\n" +
        "  inner join path_stop on stop.id = path_stop.stop_id\n" +
        "  inner join route_path on path_stop.route_path_id = route_path.id\n" +
        "  inner join route on route_path.route_id = route.id\n" +
        "where stop.active = true and stop.type = 'subway'"
const val UPDATE_STOP_LAT_LON = "UPDATE stop SET lat = %f, lon = %f, updated_at = now() WHERE id = '%s';"
const val DELETE_ENTRANCES_QUERY = "DELETE FROM subway_hall WHERE stop_id = '%s';"
const val DELETE_SUBWAY_HALLS_LINK = "DELETE FROM subway_hall_link WHERE stop_id = '%s';"
const val INSERT_ENTRANCES_QUERY = "INSERT INTO subway_hall (id, name, stop_id, active, lat, lon, geopoint, type, number, entrance_type, created_at, updated_at)\n" +
        "VALUES ('%s', '%s', '%s', 1, %f, %f, st_point(%f, %f), 'subway', %d, 'all', now(), now());"
//const val INSERT_SUBWAY_HALLS_LINK = "INSERT INTO subway_hall_link (hall_id, stop_id)\n" +
//        "VALUES ('%s', '%s');"

private val dataSources = HashMap<String, DataSource>()

data class Stop(val id: String, val name: String, val color: String)

fun main(args: Array<String>) {
    if (args.isNotEmpty()) {
        registerDataSource(DB_NAME, DB_URL, DB_USER, DB_PASSWORD)
        launch(File(args[0]))
    } else {
        println("Unknown input file!")
        return
    }
}

fun launch(inputFile: File) {
    // Читаем данные из .csv файла
    val osmStations = mutableMapOf<Station, MutableList<Entrance>>()
    inputFile.bufferedReader().readLines().map { it.split(",") }.forEach {
        val station = Station(it[0], it[1], it[2].toDouble(), it[3].toDouble())
        val entrances = osmStations.getOrPut(station) { mutableListOf() }
        entrances.add(Entrance(it[4].toInt(), it[5], it[6].toDouble(), it[7].toDouble()))
    }

    // Загружаем остановки из базы данных
    val stops = loadStationFromDb()

    // Обновляем остановик в базе данных
    updateStops(stops, osmStations)
}


fun loadStationFromDb(): List<Stop> {
    val query = String.format(Locale.ENGLISH, SELECT_STOP_QUERY)
    val stops = mutableListOf<Stop>()
    executeQuery(DB_NAME, query) {
        stops.add(Stop(it.getString("id"),
                it.getString("name"),
                it.getString("color")))
    }
    return stops
}

fun updateStops(stops: List<Stop>, stations: Map<Station, List<Entrance>>) {
    for (stop in stops) {
        val station = searchStation(stop, stations)
        if (station != null) {
            val updateQuery = String.format(Locale.ENGLISH, UPDATE_STOP_LAT_LON, station.lat, station.lon, stop.id)

            // Обновляем координаты станции
            executeUpdate(DB_NAME, updateQuery)

            // Обновляем данные о вестибюлях
            updateEntrances(stop.id, stop.name, stations[station] ?: emptyList())
        }
    }
}

fun searchStation(stop: Stop, stations: Map<Station, List<Entrance>>): Station? {
    val stopName = transformName(stop.name)
    val stopColor = stop.color
    for (station in stations.keys) {
        val stationName = transformName(station.name)
        val stationColor = station.color
        if (stationName == stopName && stationColor == stopColor) {
            return station
        }
    }
    println("Не удалось нормально смапить данные о станции $stopName $stopColor")
    return null
}

fun transformName(name: String): String {
    var result = name
    if (result.contains("(")) {
        result = result.substring(0, result.indexOf('(') - 1)
    }
    return result.trim().replace('ё', 'е').toLowerCase()
}


fun updateEntrances(stopId: String, stopName: String, entrances: List<Entrance>) {
    var query: String

    // Удаляем записи из таблицы связей станций метро и входов
//    query = String.format(Locale.ENGLISH, DELETE_SUBWAY_HALLS_LINK, stopId)
//    executeUpdate(DB_NAME, query)

    // Удаляем записи из таблицы входов
    query = String.format(Locale.ENGLISH, DELETE_ENTRANCES_QUERY, stopId)
    executeUpdate(DB_NAME, query)

    for (entrance in entrances) {
        val entranceId = UUID.randomUUID().toString()

        // Вставляем запись о вестибюле
        query = String.format(Locale.ENGLISH, INSERT_ENTRANCES_QUERY,
                entranceId, stopName, stopId, entrance.lat, entrance.lon, entrance.lon, entrance.lat, entrance.ref)
        executeUpdate(DB_NAME, query)

        // Вставляем запись в таблицу связей
//        query = String.format(Locale.ENGLISH, INSERT_SUBWAY_HALLS_LINK, entranceId, stopId)
//        executeUpdate(DB_NAME, query)
    }
}

fun registerDataSource(table: String, url: String, user: String, password: String) {
    if (dataSources.containsKey(table)) return
    dataSources[table] = DataSource(url, user, password)
}

fun getDataSource(table: String): DataSource {
    return dataSources[table] ?: throw IllegalStateException("DataSource not registered")
}

@Throws(Exception::class)
fun executeQuery(db: String, query: String, func: (rs: ResultSet) -> Unit) {
    println(query)
    getConnection(db).use {
        it.createStatement().use {
            it.executeQuery(query).use {
                try {
                    while (it.next()) {
                        func(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    throw Exception(e)
                }
            }
        }
    }
}

@Throws(Exception::class)
fun executeUpdate(db: String, sql: String) {
    println(sql)
    getConnection(db).use {
        it.createStatement().use {
            try {
                val value = it.executeUpdate(sql)
            } catch (e: Exception) {
                e.printStackTrace()
                throw Exception(e)
            }
        }
    }
}

fun getConnection(db: String): Connection {
    return getDataSource(db).getConnection()
}

class DataSource(url: String, user: String, password: String) {

    private val cpds: ComboPooledDataSource = ComboPooledDataSource()

    init {
        cpds.driverClass = "org.postgresql.Driver" //loads the jdbc driver
        cpds.jdbcUrl = url
        cpds.user = user
        cpds.password = password
        cpds.initialPoolSize = 5
        cpds.minPoolSize = 5
        cpds.acquireIncrement = 5
        cpds.maxPoolSize = 10
        cpds.maxStatements = 180
    }

    @Throws(SQLException::class)
    fun getConnection(): Connection {
        return this.cpds.connection
    }
}
