package kifio

import kifio.data.Station
import kifio.repository.osm.OsmMap
import kifio.repository.osm.OsmMap.getEntrancesJson
import kifio.repository.osm.OsmMap.getStationsJson
import java.io.InputStream

class GetGeoJsonAndPureData(efis: InputStream, sfis: InputStream): GetGeoJson(efis, sfis) {
    fun getStations() = geoData.keys
    fun getEntrancesForStation(station: Station) = geoData[station]
}
