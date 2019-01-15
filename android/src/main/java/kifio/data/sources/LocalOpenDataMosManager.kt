package kifio.data.sources

import android.content.Context
import kifio.MetroMap
import kifio.data.Entrance
import kifio.data.Station

/**
 * DataManager for getting data from OpenDataMos resource.
 */
class LocalOpenDataMosManager : DataManager {
    override fun getGeoData(): Map<Station, List<Entrance>> = emptyMap()
}