package kifio

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.MapboxMapOptions
import com.mapbox.mapboxsdk.maps.SupportMapFragment
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kifio.MetroMap.buildMap
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*


// TODO: MVVM
class MapsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        setMapBoxFragment()
    }

    private fun setMapBoxFragment() {
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        val fragment = SupportMapFragment.newInstance(MapboxMapOptions()
                .camera(getCameraPosition(LatLng(55.7558, 37.6173))))

        supportFragmentManager.beginTransaction()
                .add(R.id.content, fragment, MapFragment::class.java.simpleName).commit()
        fragment.getMapAsync {
//            initIcons(it)
            loadData(it)
        }
    }

    private fun getCameraPosition(postion: LatLng) = CameraPosition.Builder()
            .target(postion)
            .zoom(9.0)
            .build()

    private fun loadData(mapboxMap: MapboxMap) {
        GlobalScope.launch {
            loadEntrancesOffline(mapboxMap, "entrances")
        }
    }

    private fun initIcons(mapboxMap: MapboxMap) {
        // "metro" - icon for station
        // "metro{n}" - is drawable for entrance, where n - is number for entrance.
        for (i in 0..16) {
            val sb = StringBuilder("metro")
            if (i > 0) sb.append(i)
            val name = sb.toString()
            var icon: Drawable
            icon = try {
                getIcon(name)
            } catch (e: Resources.NotFoundException) {
                getIcon("metro")
            }
            mapboxMap.addImage(name, getBitmap(icon))
        }
    }

    private fun loadEntrancesOffline(mapboxMap: MapboxMap, layer: String) {
        val entrances = buildMap(assets.open("entrances.osm"), assets.open("stations.osm"))
        val featuresList = mutableListOf<Feature>()
        val bitmapMaker = BitmapMaker()

        // TODO: Refactoring
        entrances.keys.forEach { station ->

            val stationName = "${station.color}_$STATION_SIZE"
            val entranceName = "${station.color}_$ENTRANCE_SIZE"

            this.runOnUiThread {
                mapboxMap.addImage(stationName, bitmapMaker.getSubwayIcon(this, stationName, station.color, STATION_SIZE))
                mapboxMap.addImage(entranceName, bitmapMaker.getSubwayIcon(this, entranceName, station.color, STATION_SIZE))
            }

            featuresList.add(buildFeature(station.lat, station.lon, stationName, null))
            for (entrance in entrances[station]?.features() ?: Collections.emptyList()) {
                entrance.addStringProperty("icon", entranceName)
                featuresList.add(entrance)
            }
        }

        val features = FeatureCollection.fromFeatures(featuresList)
        runOnUiThread{ drawEntities(mapboxMap, features, layer) }
    }

    private fun drawEntities(mapboxMap: MapboxMap, features: FeatureCollection, layer: String) {
        mapboxMap.addSource(GeoJsonSource("$layer-source", features))
        mapboxMap.addLayer(SymbolLayer("$layer-layer", "$layer-source")
                .withProperties(PropertyFactory.iconImage("{icon}"),
                        PropertyFactory.iconSize(0.5f)))
    }

    private fun buildFeature(lat: Double, lon: Double, iconName: String, ref: String?): Feature {
        val feature = Feature.fromGeometry(Point.fromLngLat(lon, lat))
//        val iconName = if (ref != null) "metro$ref" else "metro"
        feature.addStringProperty("icon", iconName)
        return feature
    }

    private fun getIcon(name: String) = getDrawable(
            resources.getIdentifier(name, "drawable", packageName))

    private fun getBitmap(vectorDrawable: Drawable): Bitmap {
        val width = vectorDrawable.intrinsicWidth
        val height = vectorDrawable.intrinsicHeight
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable.draw(canvas)
        return bitmap
    }

    companion object {
        private const val STATION_SIZE = 36
        private const val ENTRANCE_SIZE = 24
    }
}
