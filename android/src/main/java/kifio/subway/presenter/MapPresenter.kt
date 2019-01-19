package kifio.subway.presenter

import kifio.geojson.GeoJson
import java.io.InputStream

interface MapPresenter {
    fun loadSubwayMap()
    fun updateMap(stations: GeoJson, entrances: GeoJson)
    fun getEntrancesInputStream(): InputStream
    fun getStationsInputStream(): InputStream
}