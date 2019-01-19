package kifio

/**
 * Interface for View.
 * View is actually know nothing about presenter implementation.
 */
interface MapView {
    fun addStationsLayer(geoJsonData: String?)
    fun addEntrancesLayer(geoJsonData: String?)
}