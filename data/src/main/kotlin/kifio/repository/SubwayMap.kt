package kifio.repository

import kifio.data.Entrance
import kifio.data.Station
import kifio.geojson.GeoJson
import java.io.InputStream

interface SubwayMap {
    /**
     * Return Map, where keys is stations and lists of entrances is values.
     */
    fun buildMap(efis: InputStream, sfis: InputStream): Map<Station, List<Entrance>>

    /**
     * Return GeoJson with only stations.
     */
    fun getStationsJson(geoData: Map<Station, List<Entrance>>): GeoJson

    /**
     * Return GeoJson with only entrances.
     */
    fun getEntrancesJson(geoData: Map<Station, List<Entrance>>): GeoJson
}