package kifio.subway.view.fragment

import android.graphics.Bitmap
import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.data.geojson.GeoJsonFeature
import com.google.maps.android.data.geojson.GeoJsonLayer
import com.google.maps.android.data.geojson.GeoJsonPointStyle
import kifio.DataConstants
import kifio.data.Entrance
import kifio.data.Station
import kifio.subway.utils.BitmapManager
import kifio.subway.presenter.MapPresenterImpl
import kifio.subway.view.MapView
import kifio.subway.view.activity.MapsActivity
import org.json.JSONObject
import timber.log.Timber

class GoogleMapsFragment: SupportMapFragment(), OnMapReadyCallback, MapView {

    private lateinit var map: GoogleMap
    private var stationsLayer: GeoJsonLayer? = null
    private var entrancesLayer: GeoJsonLayer? = null
    private val presenter = MapPresenterImpl(this)

    companion object {
        private const val INITIAL_ZOOM = 10.0
        private const val ENTRANCES_ZOOM_THRESHOLD = 14.0
        fun newInstance() = GoogleMapsFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        this.map = map
        presenter.loadSubwayMap()
        map.moveCamera(CameraUpdateFactory
                .newLatLngZoom(LatLng(MapsActivity.INITIAL_LAT, MapsActivity.INITIAL_LON), INITIAL_ZOOM.toFloat()))
        map.setOnCameraMoveListener(object: GoogleMap.OnCameraMoveListener{
            override fun onCameraMove() {
                val layer = entrancesLayer ?: return
                if (!layer.isLayerOnMap && map.cameraPosition.zoom >= ENTRANCES_ZOOM_THRESHOLD) {
                    layer.addLayerToMap()
                } else if (layer.isLayerOnMap && map.cameraPosition.zoom < ENTRANCES_ZOOM_THRESHOLD) {
                    layer.removeLayerFromMap()
                }
            }
        })
    }

    override fun onDestroy() {
        Timber.d("onDestroy")
        presenter.onDestroy()
        super.onDestroy()
    }

    override fun addStationsLayer(geoJsonData: String?) {
        if (geoJsonData != null) {
            stationsLayer = GeoJsonLayer(map, JSONObject(geoJsonData))
            stationsLayer?.features?.forEach {
                it.pointStyle = getPointStyle(it)
            }
            addLayer(stationsLayer)
        } else {
            Timber.e("Stations layer is empty!")
        }
    }

    override fun addEntrancesLayer(geoJsonData: String?) {
        if (geoJsonData != null) {
            entrancesLayer = GeoJsonLayer(map, JSONObject(geoJsonData))
            entrancesLayer?.features?.forEach {
                it.pointStyle = getPointStyle(it)
            }
        } else {
            Timber.e("Entrances layer is empty!")
        }
    }

    private fun getPointStyle(feature: GeoJsonFeature): GeoJsonPointStyle {
        val pointStyle = GeoJsonPointStyle()
        pointStyle.icon = BitmapDescriptorFactory
                .fromBitmap(getImage(feature))
        return pointStyle
    }

    private fun getImage(feature: GeoJsonFeature): Bitmap {
        if (feature.getProperty(DataConstants.CLASS) == Station::class.simpleName) {
            return BitmapManager.instance.getStationIcon(feature.getProperty(DataConstants.ICON))
        } else if (feature.getProperty(DataConstants.CLASS) == Entrance::class.simpleName) {
            return BitmapManager.instance.getEntranceIcon(feature.getProperty(DataConstants.ICON))
        } else {
            throw IllegalStateException("Unknown feature")
        }
    }

    private fun addLayer(layer: GeoJsonLayer?) {
        if (layer != null) {
            activity?.runOnUiThread { layer.addLayerToMap() }
        }
    }
}
