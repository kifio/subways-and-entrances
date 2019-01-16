package kifio.subway.view.fragment

import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnCameraMoveCanceledListener
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.data.geojson.GeoJsonFeature
import com.google.maps.android.data.geojson.GeoJsonLayer
import com.google.maps.android.data.geojson.GeoJsonPointStyle
import kifio.DataConstants
import kifio.subway.data.model.LocalOpenStreetMapManager
import kifio.subway.presenter.MapPresenter
import kifio.subway.view.MapView
import kifio.subway.view.activity.MapsActivity
import org.json.JSONObject
import timber.log.Timber


class GoogleMapsFragment: SupportMapFragment(), OnMapReadyCallback, MapView {

    private lateinit var map: GoogleMap
    private var stationsLayer: GeoJsonLayer? = null
    private var entrancesLayer: GeoJsonLayer? = null
    private val presenter = MapPresenter(this, LocalOpenStreetMapManager.instanse)

    companion object {

        private const val INITIAL_ZOOM = 10.0
        private const val ENTRANCES_ZOOM_THRESHOLD = 12.0
        fun newInstance() = GoogleMapsFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        this.map = map
        presenter.loadEntrancesOffline()
        map.moveCamera(CameraUpdateFactory
                .newLatLngZoom(LatLng(MapsActivity.INITIAL_LAT, MapsActivity.INITIAL_LON), INITIAL_ZOOM.toFloat()))
        map.setOnCameraIdleListener(object: OnCameraIdleListener {
            override fun onCameraIdle() {
                Timber.d("onCameraIdle, zoom: ${map.cameraPosition.zoom}")
                entrancesLayer?.features?.forEach {
                    /* Timber.d("onCameraIdle, feature.visibility: ${map.cameraPosition.zoom > ENTRANCES_ZOOM_THRESHOLD}") */
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
            addLayer(stationsLayer)
        } else {
            Timber.e("Stations layer is empty!")
        }
    }

    override fun addEntrancesLayer(geoJsonData: String?) {
        if (geoJsonData != null) {
            entrancesLayer = GeoJsonLayer(map, JSONObject(geoJsonData))
            addLayer(GeoJsonLayer(map, JSONObject(geoJsonData)))
        } else {
            Timber.e("Entrances layer is empty!")
        }
    }

    private fun getPointStyle(feature: GeoJsonFeature): GeoJsonPointStyle {
        val pointStyle = GeoJsonPointStyle()
        pointStyle.icon = BitmapDescriptorFactory
                .fromBitmap(presenter.getIcons()[feature.getProperty(DataConstants.ICON)])
        return pointStyle
    }

    private fun addLayer(layer: GeoJsonLayer?) {
        if (layer != null) {
            layer.features.forEach { it.pointStyle = getPointStyle(it) }
            activity?.runOnUiThread { layer.addLayerToMap() }
        }
    }
}
