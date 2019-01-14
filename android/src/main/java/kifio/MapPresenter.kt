package kifio

import android.content.Context
import android.graphics.Color
import com.google.gson.Gson
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import kifio.data.Entrance
import kifio.data.Station
import kifio.interactors.Interactor
import kifio.utils.BitmapManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class MapPresenter {

    private var mapView: MapView? = null
    private var dataInteractor: Interactor? = null
    private val bitmapManager: BitmapManager = BitmapManager()

    private fun loadSubwayLayer() {
        GlobalScope.launch {
            loadEntrancesOffline()
        }
    }

    fun getIcons() = bitmapManager.getIcons()

    fun loadEntrancesOffline() {
        val ctx = mapView?.getContext() ?: throw IllegalStateException()
        val geoData = (dataInteractor?.getGeoData(ctx)) ?: throw IllegalStateException()
        mapView?.addLayer(buildGeoJson(ctx, geoData))
    }

    private fun buildGeoJson(ctx: Context, geoData: Map<Station, List<Entrance>>) =
        Gson().toJson(geoData.keys.map { transform(ctx, it, getEntrances(it, geoData))}.flatten())

    private fun getEntrances(station: Station, geoData: Map<Station, List<Entrance>>) =
            geoData[station] ?: Collections.emptyList()

    private fun transform(ctx: Context, station: Station, entrances: List<Entrance>): List<Feature> {
        val features = mutableListOf<Feature>()
        val stationColor = Color.parseColor(station.color)
        bitmapManager.getStationIcon(ctx, stationColor)
        features.add(buildStation(station.lat, station.lon, station.color))
        entrances.forEach {
            val entranceNumber = it.ref ?: 0
            bitmapManager.getEntranceIcon(entranceNumber)
            features.add(buildEntrance(station.lat, station.lon, entranceNumber))
        }
        return features
    }

    private fun buildStation(lat: Double, lon: Double, iconName: String): Feature {
        val feature = Feature.fromGeometry(Point.fromLngLat(lon, lat))
        feature.addStringProperty("icon", iconName)
        return feature
    }

    private fun buildEntrance(lat: Double, lon: Double, ref: Int): Feature {
        val feature = Feature.fromGeometry(Point.fromLngLat(lon, lat))
        feature.addStringProperty("icon", ref.toString())
        return feature
    }

    fun onDestroy() {
        mapView = null
        dataInteractor = null
    }
}