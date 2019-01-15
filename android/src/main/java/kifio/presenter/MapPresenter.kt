package kifio.presenter

import com.mapbox.geojson.Feature
import kifio.data.*
import kifio.data.geojson.GeoJsonImpl
import kifio.data.sources.DataManager
import kifio.utils.BitmapManager
import kifio.view.MapView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class MapPresenter(mapView: MapView, dataManager: DataManager) {

    private val bitmapManager: BitmapManager = BitmapManager(mapView.getContext())
    private var mapView: MapView? = mapView
    private var dataManager: DataManager? = dataManager

    fun getIcons() = bitmapManager.getIcons()

    fun loadEntrancesOffline() {
        GlobalScope.launch {
            val ctx = mapView?.getContext() ?: throw IllegalStateException()
            parseData((dataManager?.getGeoData(ctx)) ?: throw IllegalStateException())
        }
    }

    private fun parseData(geoData: Map<Station, List<Entrance>>) {
        mapView?.addStationsLayer(buildStationsJson(geoData))
        mapView?.addEntrancesLayer(buildEntrancesJson(geoData))
    }

    private fun buildStationsJson(geoData: Map<Station, List<Entrance>>): String {
        val geoJson = GeoJsonImpl()
        geoJson.setFeatures(buildFeatures(geoData.keys, bitmapManager::addStationIcon))
        return geoJson.toJson()
    }

    private fun buildEntrancesJson(geoData: Map<Station, List<Entrance>>): String {
        val geoJson = GeoJsonImpl()
        geoJson.setFeatures(buildFeatures(geoData.keys.map {
            geoData[it] ?: Collections.emptyList()
        }.flatten(), bitmapManager::addEntranceIcon))
        return geoJson.toJson()
    }

    private fun buildFeatures(mapObjects: Collection<MapData>, addIcon: (key: String) -> Unit): List<Feature> {
        return mapObjects.map {
            addIcon(it.getIconId())
            it.toFeature()
        }.toList()
    }

    fun onDestroy() {
        mapView = null
        dataManager = null
    }
}