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
import com.mchange.v2.c3p0.ComboPooledDataSource
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException

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

private val dataSources = HashMap<String, DataSource>()

fun main(args: Array<String>) {
    if (args.size >= 1) {
        updateStationsInDd(File(args[0]))
    } else {
        println("Unknown input file!")
        return
    }
}

fun updateStationsInDd(inputFile: File) {
    // Читаем данные из .csv файла
    val entrances = mutableMapOf<Station, MutableList<Entrance>>()
    inputFile.bufferedReader().readLines().map({it.split(",")}).forEach {
        val station = Station(it[0], it[1], it[2], it[3])
        val entrances = map.getOrPut(station, {mutableListOf<Entrance>()})
        entrances.add(Entrance(it[4], it[5], it[6], it[7]))
    }

    registerDataSource(DB_NAME, DB_URL, DB_USER, DB_PASSWORD)
    val stops = mutableListOf<String>()
    for (station in stations) {
        // Запрашиваем станцию
        val query = String.format(Locale.ENGLISH, SELECT_STOP_QUERY, station.name?.toLowerCase(), station.color)
        val entrances = entrances.filter { it.station == "${station.name},${station.color}" }
        println(query)
        executeQuery(DB_NAME, query) {
            val stopId = it.getString("stopId")
            val stopName = it.getString("stopName")
            val updateQuery = String.format(Locale.ENGLISH, UPDATE_STOP_LAT_LON, station.lat, station.lon, stopId)
            // Обновляем координаты станции
            executeUpdate(DB_NAME, updateQuery)
            // Обновляем данные о вестибюлях
            updateEntrances(stopId, stopName, entrances)
            stops.add(stopId)
        }
    }

    println("${stops.size} stations were updated")
}

fun updateEntrances(stopId: String, stopName: String, entrances: List<Entrance>) {
    executeUpdate(DB_NAME, String.format(Locale.ENGLISH, DELETE_SUBWAY_HALLS_LINK, stopId))
    var query = String.format(Locale.ENGLISH, DELETE_ENTRANCES_QUERY, stopId)
    executeUpdate(DB_NAME, query)
    for (entrance in entrances) {
        executeQuery(DB_NAME, "select count(*) as count from subway_hall where id ='${entrance.id}'") {
            if (it.getInt("count") == 0) {
                query = String.format(Locale.ENGLISH, INSERT_ENTRANCES_QUERY,
                        entrance.id, stopName, stopId, entrance.lat, entrance.lon, entrance.lon, entrance.lat, entrance.ref)
                println(query)
                try {
                    executeUpdate(DB_NAME, query)
                    executeUpdate(DB_NAME, String.format(Locale.ENGLISH, INSERT_SUBWAY_HALLS_LINK, entrance.id, stopId))
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }
        }
    }
}

@JvmStatic
fun registerDataSource(table: String, url: String, user: String, password: String) {
    if (dataSources.containsKey(table)) return
    dataSources[table] = DataSource(url, user, password)
}

@JvmStatic
private fun getDataSource(table: String): DataSource {
    return dataSources[table] ?: throw IllegalStateException("DataSource not registered")
}

@Throws(Exception::class)
fun executeQuery(db: String, query: String, func: (rs: ResultSet) -> Unit) {
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
}
