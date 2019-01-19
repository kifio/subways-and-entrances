package kifio.subway.view.fragment

import android.graphics.Bitmap
import android.os.Bundle
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Feature
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.MapboxMapOptions
import com.mapbox.mapboxsdk.maps.SupportMapFragment
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility
import com.mapbox.mapboxsdk.utils.MapFragmentUtils
import kifio.DataConstants
import kifio.data.Entrance
import kifio.data.Station
import kifio.subway.presenter.MapPresenterImpl
import kifio.subway.view.MapView
import kifio.subway.view.activity.MapsActivity
import kifio.subway.utils.BitmapManager
import timber.log.Timber
import java.lang.IllegalStateException
import kifio.subway.R

class MapboxMapFragment : SupportMapFragment(), MapView {

    private lateinit var map: MapboxMap
    private val presenter = MapPresenterImpl(this)

    companion object {

        private const val STATIONS = "STATIONS"
        private const val ENTRANCES = "ENTRANCES"
        private const val INITIAL_ZOOM = 9.0
        private const val ENTRANCES_ZOOM_THRESHOLD = 13.0

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
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(requireActivity(), getString(R.string.mapbox_access_token))
    }

    override fun onDestroy() {
        presenter.onDestroy()
        super.onDestroy()
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        super.onMapReady(mapboxMap)
        map = mapboxMap
        mapboxMap.uiSettings.isRotateGesturesEnabled = false
        presenter.loadSubwayMap()
        map.addOnCameraMoveListener {
            if (mapboxMap.getCameraPosition().zoom > ENTRANCES_ZOOM_THRESHOLD) {
                mapboxMap.getLayer(ENTRANCES)?.setProperties(visibility(Property.VISIBLE))
            } else {
                mapboxMap.getLayer(ENTRANCES)?.setProperties(visibility(Property.NONE))
            }
        }
    }

    override fun addStationsLayer(geoJsonData: String?) {
        if (geoJsonData != null) {
            addLayer(geoJsonData, STATIONS, Property.VISIBLE)
        } else {
            Timber.e("Stations layer is empty!")
        }
    }

    override fun addEntrancesLayer(geoJsonData: String?) {
        if (geoJsonData != null) {
            addLayer(geoJsonData, ENTRANCES, Property.NONE)
        } else {
            Timber.e("Stations layer is empty!")
        }
    }

    override fun getContext() = activity

    private fun addLayer(geoJsonData: String, layer: String, visibility: String) {
        activity?.runOnUiThread {
            val features = FeatureCollection.fromJson(geoJsonData)
            features.features()?.forEach { addImage(it) } ?: throw IllegalStateException("Features can't be null")
            map.addSource(GeoJsonSource("$layer", features))
            map.addLayer(SymbolLayer("$layer", "$layer")
                    .withProperties(PropertyFactory.iconImage("{icon}"), visibility(visibility)))
        }
    }

    private fun addImage(feature: Feature) {
        if (feature.getStringProperty(DataConstants.CLASS) == Station::class.simpleName) {
            addImage(feature.getStringProperty(DataConstants.ICON), BitmapManager.instance::getStationIcon)
        } else if (feature.getStringProperty(DataConstants.CLASS) == Entrance::class.simpleName) {
            addImage(feature.getStringProperty(DataConstants.ICON), BitmapManager.instance::getEntranceIcon)
        }
    }

    private fun addImage(iconId: String, getIcon: (key: String) -> Bitmap) {
        if (map.getImage(iconId) == null) {
            map.addImage(iconId, getIcon(iconId))
        }
    }
}
