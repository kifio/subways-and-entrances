package kifio.subway.data.sources

import kifio.subway.data.Entrance
import kifio.subway.data.Station

/**
 * Base interface for all sources, which prefers stations and entrances.
 */
interface DataManager {
    fun getGeoData(): Map<Station, List<Entrance>>
}