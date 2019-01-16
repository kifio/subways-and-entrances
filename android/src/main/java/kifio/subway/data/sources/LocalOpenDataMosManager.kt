package kifio.subway.data.sources

import kifio.subway.data.Entrance
import kifio.subway.data.Station

/**
 * DataManager for getting data from OpenDataMos resource.
 */
class LocalOpenDataMosManager : DataManager {
    override fun getGeoData(): Map<Station, List<Entrance>> = emptyMap()
}