package kifio.subway.data.model

import com.mapbox.geojson.Feature
import kifio.subway.App
import kifio.MetroMap
import kifio.subway.data.MapData
import kifio.subway.data.geojson.GeoJsonImpl
import kifio.subway.utils.BitmapManager
import java.util.*

/**
 * DataManager for getting data parsed from .pbf files.
 * Android App does not actually works with .pbf files, required data must be ejected with parser module.
 */
class LocalOpenStreetMapManager private constructor() : DataManager {

    companion object {
        var instanse = LocalOpenStreetMapManager()
            private set
    }

    private val geoData by lazy {
        MetroMap.buildMap(
                App.instance.assets.open("entrances.osm"),
                App.instance.assets.open("stations.osm"))
    }

    override fun getStationsJson(): String {
        val geoJson = GeoJsonImpl()
        geoJson.setFeatures(buildFeatures(geoData.keys, BitmapManager.instanse::addStationIcon))
        return geoJson.toJson()
    }

    override fun getEntrancesJson(): String {
        val geoJson = GeoJsonImpl()
        geoJson.setFeatures(buildFeatures(geoData.keys.map {
            geoData[it] ?: Collections.emptyList()
        }.flatten(), BitmapManager.instanse::addEntranceIcon))
        return geoJson.toJson()
    }

    private fun buildFeatures(mapObjects: Collection<MapData>, addIcon: (key: String) -> Unit): List<Feature> {
        return mapObjects.map {
            addIcon(it.getIconId())
            it.toFeature()
        }.toList()
    }
}