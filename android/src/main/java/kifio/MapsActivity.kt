package kifio

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.gson.JsonObject
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.MapboxMapOptions
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource

class MapsActivity : AppCompatActivity(), MapView {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        if (savedInstanceState == null) setMapBoxFragment()
    }

    private fun setMapBoxFragment() {
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        val fragment = getFragment()
        val presenter = MapPresenter()
        supportFragmentManager.beginTransaction()
                .add(R.id.content, fragment, MapFragment::class.java.simpleName).commit()
        fragment.getMapAsync {

        }
    }

    private fun getFragment() = MapFragment.newInstance(MapboxMapOptions()
            .camera(getCameraPosition(LatLng(55.7558, 37.6173))))

    private fun getCameraPosition(postion: LatLng) = CameraPosition.Builder()
            .target(postion)
            .zoom(9.0)
            .build()

    override fun addLayer(geoJsonData: String) {

    }

    override fun getContext() = this

    private fun drawEntities(mapboxMap: MapboxMap, features: FeatureCollection, layer: String) {
        mapboxMap.addSource(GeoJsonSource("$layer-source", features))
        mapboxMap.addLayer(SymbolLayer("$layer-layer", "$layer-source")
                .withProperties(PropertyFactory.iconImage("{icon}"),
                        PropertyFactory.iconSize(0.5f)))
    }
}
