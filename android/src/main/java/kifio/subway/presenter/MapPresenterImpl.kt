package kifio.subway.presenter

import kifio.GetGeoJson
import kifio.geojson.GeoJson
import kifio.subway.App
import kifio.subway.view.MapView
import java.io.InputStream
import java.util.concurrent.Executors

class MapPresenterImpl(mapView: MapView) : MapPresenter {

    private var mapView: MapView? = mapView

    override fun loadSubwayMap() {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            val useCase = GetGeoJson(getEntrancesInputStream(), getStationsInputStream())
            updateMap(useCase.getStationsGeoJson(), useCase.getEntrancesGeoJson())
        }
    }

    override fun updateMap(stations: GeoJson, entrances: GeoJson) {
        mapView?.addStationsLayer(stations.toJson())
        mapView?.addEntrancesLayer(entrances.toJson())
    }

    override fun getStationsInputStream(): InputStream = App.instance.assets.open("stations.osm")

    override fun getEntrancesInputStream(): InputStream = App.instance.assets.open("entrances.osm")

    fun onDestroy() {
        mapView = null
    }
}
