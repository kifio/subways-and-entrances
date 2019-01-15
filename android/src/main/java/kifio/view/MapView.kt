package kifio.view

import android.content.Context

/**
 * Interface for View.
 * View is actually know nothing about presenter implementation.
 */
interface MapView {
    fun addStationsLayer(geoJsonData: String)
    fun addEntrancesLayer(geoJsonData: String)
}