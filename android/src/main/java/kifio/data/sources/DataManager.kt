package kifio.data.sources

import android.content.Context
import kifio.data.Entrance
import kifio.data.Station

/**
 * Base interface for all sources, which prefers stations and entrances.
 */
interface DataManager {
    fun getGeoData(): Map<Station, List<Entrance>>
}