package kifio

import com.mchange.v2.c3p0.ComboPooledDataSource
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException

object SQLManager {

    private val dataSources = HashMap<String, DataSource>()

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