package kifio.data.sources

import android.content.Context
import kifio.MetroMap
import kifio.data.Entrance
import kifio.data.Station

/**
 * DataManager for getting data from Fireabase.
 */
class FirebaseOpenStreetMapManager : DataManager {
    override fun getGeoData(ctx: Context): Map<Station, List<Entrance>> = emptyMap()
}