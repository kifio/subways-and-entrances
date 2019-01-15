package kifio.data.sources

import kifio.App
import kifio.MetroMap
import kifio.data.Entrance
import kifio.data.Station

/**
 * DataManager for getting data parsed from .pbf files.
 * Android App does not actually works with .pbf files, required data must be ejected with parser module.
 */
class LocalOpenStreetMapManager : DataManager {
    override fun getGeoData(): Map<Station, List<Entrance>> =
            MetroMap.buildMap(App.instance.assets.open("entrances.osm"),
                    App.instance.assets.open("stations.osm"))
}