package kifio.data.sources

import android.content.Context
import kifio.MetroMap
import kifio.data.Entrance
import kifio.data.Station

/**
 * DataManager for getting data parsed from .pbf files.
 * Android App does not actually works with .pbf files, required data must be ejected with parser module.
 */
class LocalOpenStreetMapManager : DataManager {
    override fun getGeoData(ctx: Context): Map<Station, List<Entrance>> =
            MetroMap.buildMap(ctx.assets.open("entrances.osm"), ctx.assets.open("stations.osm"))
}