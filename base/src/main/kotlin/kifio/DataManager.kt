package kifio

/**
 * Base interface for all sources, which prefers stations and entrances.
 */
interface DataManager {
    fun getStationsJson(): String
    fun getEntrancesJson(): String
}