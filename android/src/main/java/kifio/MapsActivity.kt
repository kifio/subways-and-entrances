package kifio

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.*
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import android.graphics.*
import android.graphics.drawable.*
import java.io.*
import java.net.*
import com.google.gson.*
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import timber.log.*
import kotlinx.coroutines.experimental.*
import com.mapbox.mapboxsdk.style.sources.*
import com.mapbox.mapboxsdk.style.layers.*
import kifio.Common.generateToken
import java.lang.StringBuilder

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
                .add(R.id.content, fragment, SupportMapFragment::class.java.simpleName).commit()
        fragment.getMapAsync {
            initIcons(it)
            loadData(it)
        }
    }

    private fun getCameraPosition(postion: LatLng) = CameraPosition.Builder()
            .target(postion)
            .zoom(9.0)
            .build()

    private fun loadData(mapboxMap: MapboxMap) {
        GlobalScope.launch {
            val token = generateToken(assets.open("pkey.json"))
            loadEntities(mapboxMap, token, "stations")
            loadEntities(mapboxMap, token, "entrances")
        }
    }

    private fun initIcons(mapboxMap: MapboxMap) {
        // "metro" - icon for station
        // "metro{n}" - is drawable for entrance, where n - is number for entrance.
        for (i in 0..16) {
            val sb = StringBuilder("metro")
            if (i > 0) sb.append(i)
            val name = sb.toString()
            mapboxMap.addImage(name, getBitmap(getIcon(name)))
        }
    }

    private fun loadEntities(mapboxMap: MapboxMap, token: String, layer: String) {
        Timber.d("${Common.baseUrl}/$layer.json?access_token=$token")
        val url = URL("${Common.baseUrl}/$layer.json?access_token=$token")
        val connection = url.openConnection() as HttpURLConnection
        connection.connect()
        if (connection.responseCode == 200) {
            val response = connection.inputStream.bufferedReader().use(BufferedReader::readText)
            runOnUiThread{ drawEntities(mapboxMap, response, layer) }
        }
    }

    private fun drawEntities(mapboxMap: MapboxMap, responseString: String, layer: String) {
        val json = JsonParser().parse(responseString).asJsonObject
        val features = FeatureCollection.fromFeatures(
                json.entrySet().asSequence()
                        .map { entry -> buildFeature(entry.value.asJsonObject) }.toList())

        mapboxMap.addSource(GeoJsonSource("$layer-source", features))
        mapboxMap.addLayer(SymbolLayer("$layer-layer", "$layer-source")
                .withProperties(PropertyFactory.iconImage("{icon-name}"),
                        PropertyFactory.iconSize(0.5f)))
    }

    private fun buildFeature(jsonElement: JsonObject): Feature {
        Timber.d(jsonElement.toString())
        val lat: Double = jsonElement.get("lat").asDouble
        val lon: Double = jsonElement.get("lon").asDouble
        val feature = Feature.fromGeometry(Point.fromLngLat(lon, lat))
        val hasRef = jsonElement.has("ref")
        val iconName = if (hasRef) "metro${jsonElement.get("ref")}" else "metro"
        feature.addStringProperty("icon-name", iconName)
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

}
