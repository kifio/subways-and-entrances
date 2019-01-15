package kifio.view.activity

import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.MapboxMapOptions
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kifio.R
import kifio.data.sources.LocalOpenStreetMapManager
import kifio.view.fragment.MapFragment
import kifio.presenter.MapPresenter
import kifio.view.MapView
import java.lang.IllegalStateException

/**
 * Host activity for all MapFragments
 */
class MapsActivity : AppCompatActivity(), MapView {

    private lateinit var map: MapboxMap
    private val presenter = MapPresenter(this, LocalOpenStreetMapManager())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        if (savedInstanceState == null) setMapBoxFragment()
    }

    override fun onDestroy() {
        presenter.onDestroy()
        super.onDestroy()
    }

    override fun addStationsLayer(geoJsonData: String) {
        addLayer(geoJsonData, STATIONS)
    }

    override fun addEntrancesLayer(geoJsonData: String) {
        addLayer(geoJsonData, ENTRANCES)
    }

    private fun addLayer(geoJsonData: String, layer: String) {
        this.runOnUiThread {
            val features = FeatureCollection.fromJson(geoJsonData)
            val icons = presenter.getIcons()
            icons.keys.forEach { addImage(it, icons) }
            map.addSource(GeoJsonSource("$layer-source", features))
            map.addLayer(SymbolLayer("$layer-layer", "$layer-source")
                    .withProperties(PropertyFactory.iconImage("{icon}"),
                            PropertyFactory.iconSize(0.5f)))
        }
    }

    private fun setMapBoxFragment() {
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        val fragment = getFragment()
        supportFragmentManager.beginTransaction()
                .add(R.id.content, fragment, MapFragment::class.java.simpleName).commit()
        fragment.getMapAsync {
            this.map = it
            presenter.loadEntrancesOffline()
        }
    }

    private fun getFragment() = MapFragment.newInstance(MapboxMapOptions()
            .camera(getCameraPosition(LatLng(55.7558, 37.6173))))

    private fun getCameraPosition(postion: LatLng) = CameraPosition.Builder()
            .target(postion)
            .zoom(9.0)
            .build()

    private fun addImage(iconId: String, icons: Map<String, Bitmap>) {
        map.addImage(iconId, icons[iconId] ?: throw IllegalStateException())
    }

    override fun getContext() = this

    companion object {
        private const val STATIONS = "STATIONS"
        private const val ENTRANCES = "ENTRANCES"
    }
}
