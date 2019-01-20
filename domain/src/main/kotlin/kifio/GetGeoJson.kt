package kifio

import kifio.repository.osm.OsmMap
import kifio.repository.osm.OsmMap.getEntrancesJson
import kifio.repository.osm.OsmMap.getStationsJson
import java.io.InputStream

open class GetGeoJson(efis: InputStream, sfis: InputStream) {

    protected val geoData = OsmMap.buildMap(efis, sfis)

    fun getEntrancesGeoJson() = getEntrancesJson(geoData)

    fun getStationsGeoJson() = getStationsJson(geoData)
}
