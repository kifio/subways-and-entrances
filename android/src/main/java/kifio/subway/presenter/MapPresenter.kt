package kifio.subway.presenter

import kifio.subway.data.model.DataManager
import kifio.subway.utils.BitmapManager
import kifio.subway.view.MapView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MapPresenter(mapView: MapView, dataManager: DataManager) {

    private val dataManager: DataManager? = dataManager
    private var mapView: MapView? = mapView

    fun getIcons() = BitmapManager.instanse.getIcons()

    fun loadEntrancesOffline() {
        GlobalScope.launch {
            mapView?.addStationsLayer(dataManager?.getStationsJson())
            mapView?.addEntrancesLayer(dataManager?.getEntrancesJson())
        }
    }

    fun onDestroy() {
        mapView = null
    }
}