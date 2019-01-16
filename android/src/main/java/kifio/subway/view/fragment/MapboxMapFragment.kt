package kifio.subway.view.fragment

import android.graphics.Bitmap
import android.os.Bundle
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.MapboxMapOptions
import com.mapbox.mapboxsdk.maps.SupportMapFragment
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.MapFragmentUtils
import kifio.subway.R
import kifio.subway.data.sources.LocalOpenStreetMapManager
import kifio.subway.presenter.MapPresenter
import kifio.subway.view.MapView
import kifio.subway.view.activity.MapsActivity
import timber.log.Timber
import java.lang.IllegalStateException

class MapboxMapFragment : SupportMapFragment(), MapView {

    private lateinit var map: MapboxMap
    private val presenter = MapPresenter(this, LocalOpenStreetMapManager())

    companion object {

        private const val STATIONS = "STATIONS"
        private const val ENTRANCES = "ENTRANCES"
        private const val INITIAL_ZOOM = 9.0

        fun newInstance(): MapboxMapFragment {
            val mapFragment = MapboxMapFragment()
            val options = MapboxMapOptions().camera(getCameraPosition())
            mapFragment.arguments = MapFragmentUtils.createFragmentArgs(options)
            return mapFragment
        }

        private fun getCameraPosition() = CameraPosition.Builder()
                .target(LatLng(MapsActivity.INITIAL_LAT, MapsActivity.INITIAL_LON))
                .zoom(INITIAL_ZOOM).build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.d("onCreate")
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(requireActivity(), getString(R.string.mapbox_access_token))
    }

    override fun onDestroy() {
        Timber.d("onDestroy")
        presenter.onDestroy()
        super.onDestroy()
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        super.onMapReady(mapboxMap)
        map = mapboxMap
        mapboxMap.uiSettings.isRotateGesturesEnabled = false
        presenter.loadEntrancesOffline()
    }

    override fun addStationsLayer(geoJsonData: String) {
        addLayer(geoJsonData, STATIONS)
    }

    override fun addEntrancesLayer(geoJsonData: String) {
        addLayer(geoJsonData, ENTRANCES)
    }

    private fun addImage(iconId: String, icons: Map<String, Bitmap>) {
        map.addImage(iconId, icons[iconId] ?: throw IllegalStateException())
    }

    override fun getContext() = activity

    private fun addLayer(geoJsonData: String, layer: String) {
        activity?.runOnUiThread {
            val features = FeatureCollection.fromJson(geoJsonData)
            val icons = presenter.getIcons()
            icons.keys.forEach { addImage(it, icons) }
            map.addSource(GeoJsonSource("$layer-source", features))
            map.addLayer(SymbolLayer("$layer-layer", "$layer-source")
                    .withProperties(PropertyFactory.iconImage("{icon}")))
        }
    }
}